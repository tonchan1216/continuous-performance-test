package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.codepipeline.model.ApprovalResult;
import com.amazonaws.services.codepipeline.model.ApprovalStatus;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.batch.helper.ParseHelper;
import org.maca.continuous.perftest.app.batch.helper.PipelineHelper;
import org.maca.continuous.perftest.app.model.*;
import org.maca.continuous.perftest.common.app.model.Approval;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.*;

@Slf4j
public class ResultTasklet implements Tasklet {
    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    @Value("#{jobExecutionContext['testId']}")
    private String testId;

    @Value("#{jobExecutionContext['startTime']}")
    private Date startTime;

    @Value("#{jobExecutionContext['approval']}")
    private Approval approval;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private ResourcePatternResolver resolver;

    @Autowired
    RunnerStatusService runnerStatusService;

    @Autowired
    PipelineHelper pipelineHelper;

    @Autowired
    ParseHelper parseHelper;

    private static final String DELIMITER = "/";

    @Autowired
    public void setupResolver(ApplicationContext applicationContext, AmazonS3 amazonS3){
        this.resolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        //S3 Download and parse XML
        Resource[] results = fileDownload("results.xml");
        List<FinalStatus> finalStatusList = parseHelper.parseXml(results, FinalStatus.class);

        //Calculate Result
        Result result = parseHelper.calculateResult(finalStatusList);
        ObjectMapper mapper = new ObjectMapper();
        String resultJson = mapper.writeValueAsString(result);
        log.info(resultJson);

        // Download and parse pass-fail files
        Resource[] passFails = fileDownload("pass-fail.xml");
        List<JUnitTestSuites> testSuiteList = parseHelper.parseXml(passFails, JUnitTestSuites.class);
        Map<String, Long> errorList = parseHelper.calculateCriteria(testSuiteList);
        log.info(errorList.toString());

        // DynamoDB Update
        RunnerStatus runnerStatus = runnerStatusService.getRunnerStatus(testId, startTime);
        runnerStatus.setStatus("complete");
        runnerStatus.setResult(resultJson);
        runnerStatus.setCriteria(errorList.isEmpty() ? "pass" : "fail");
        runnerStatusService.updateRunnerStatus(runnerStatus);

        // Update Codepipeline
        if (Objects.nonNull(approval)) {
            ApprovalResult approvalResult = new ApprovalResult();

            if (errorList.isEmpty()){
                approvalResult.withStatus(ApprovalStatus.Approved)
                        .withSummary("Test has completed, and passed criteria.");
            } else {
                approvalResult.withStatus(ApprovalStatus.Rejected)
                        .withSummary("Test has completed, but failed criteria. (" + errorList + ")");
            }

            pipelineHelper.approvalPipeline(approval, approvalResult);
        }

        return RepeatStatus.FINISHED;
    }

    //S3 Download
    private Resource[] fileDownload(String fileName) throws Exception {
        String S3Path = "s3://" + bucketName + DELIMITER +
                "results" + DELIMITER + testId + DELIMITER + "artifacts/*/" + fileName;

        Resource[] results = resolver.getResources(S3Path);

        if (results.length == 0) {
            throw new Exception("Not Found result Resources in S3 Bucket");
        }
        return results;
    }

}