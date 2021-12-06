package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.model.AWSCodePipelineException;
import com.amazonaws.services.codepipeline.model.ApprovalResult;
import com.amazonaws.services.codepipeline.model.ApprovalStatus;
import com.amazonaws.services.codepipeline.model.PutApprovalResultRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.maca.continuous.perftest.app.model.*;
import org.maca.continuous.perftest.common.app.model.Approval;
import org.maca.continuous.perftest.domain.model.PrimaryKey;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    AWSCodePipeline awsCodePipeline;

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
        List<FinalStatus> finalStatusList = parseXml(results, FinalStatus.class);

        //Calculate Result
        List<Group> grpByLabel = finalStatusList.stream()
                .flatMap(g -> g.getGroupList().stream())
                .collect(Collectors.groupingBy(Group::getLabel)).get("");

        if (grpByLabel.isEmpty()) {
            throw new Exception("finalStatus list is empty");
        }

        Result result = Result.builder()
                .testDuration(finalStatusList.stream()
                        .map(FinalStatus::getTestDuration)
                        .mapToDouble(v -> v)
                        .average().orElse(0.0))
                .throughput(grpByLabel.stream()
                        .map(Group::getThroughput)
                        .mapToInt(CountMetrics::getValue).sum())
                .concurrency(grpByLabel.stream()
                        .map(Group::getConcurrency)
                        .mapToInt(CountMetrics::getValue).sum())
                .success(grpByLabel.stream()
                        .map(Group::getSuccess)
                        .mapToInt(CountMetrics::getValue).sum())
                .fail(grpByLabel.stream()
                        .map(Group::getFail)
                        .mapToInt(CountMetrics::getValue).sum())
                .avgResponseTime(grpByLabel.stream()
                        .map(Group::getAvgResponseTime)
                        .mapToDouble(TimeMetrics::getValue)
                        .average().orElse(0.0))
                .perc90(grpByLabel.stream()
                        .map(Group::getPercentiles)
                        .flatMap(Collection::stream)
                        .filter(p -> p.getParam().equals("90.0"))
                        .collect(Collectors.averagingDouble(Percentile::getValue)))
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String resultJson = mapper.writeValueAsString(result);
        log.info(resultJson);

        // Download and parse pass-fail files
        Resource[] passFails = fileDownload("pass-fail.xml");
        List<JUnitTestSuites> testSuiteList = parseXml(passFails, JUnitTestSuites.class);
        Map<String, Long> errorList = testSuiteList.stream()
                .flatMap(t -> t.getTestSuites().stream())
                .flatMap(t -> t.getTestCases().stream())
                .map(JUnitTestCase::getError)
                .collect(Collectors.groupingBy(JUnitError::getMessage, Collectors.counting()));
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
                        .withSummary("Performance Test has success: " + resultJson);
            } else {
                approvalResult.withStatus(ApprovalStatus.Rejected)
                        .withSummary("Performance Test has failed: " + resultJson);
            }

            PutApprovalResultRequest putApprovalResultRequest = new PutApprovalResultRequest()
                    .withActionName(approval.actionName)
                    .withPipelineName(approval.pipelineName)
                    .withStageName(approval.stageName)
                    .withToken(approval.token)
                    .withResult(approvalResult);

            try {
                awsCodePipeline.putApprovalResult(putApprovalResultRequest);
            } catch (AWSCodePipelineException e) {
                log.error(e.getMessage());
            }
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

    private <T> List<T> parseXml(Resource[] resources, Class<T> mapperClass) {
        XmlMapper xmlMapper = new XmlMapper();

        List<T> arrayList = new ArrayList<>();
        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                arrayList.add(xmlMapper.readValue(
                        IOUtils.toString(inputStream, StandardCharsets.UTF_8),
                        mapperClass
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }
}