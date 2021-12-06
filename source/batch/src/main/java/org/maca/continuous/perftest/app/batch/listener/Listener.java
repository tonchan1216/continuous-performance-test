package org.maca.continuous.perftest.app.batch.listener;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.model.AWSCodePipelineException;
import com.amazonaws.services.codepipeline.model.ApprovalResult;
import com.amazonaws.services.codepipeline.model.ApprovalStatus;
import com.amazonaws.services.codepipeline.model.PutApprovalResultRequest;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.batch.helper.PipelineHelper;
import org.maca.continuous.perftest.common.app.model.Approval;
import org.maca.continuous.perftest.domain.model.PrimaryKey;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Listener extends JobExecutionListenerSupport {
    @Autowired
    RunnerStatusService runnerStatusService;

    @Autowired
    PipelineHelper pipelineHelper;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(this.getClass().getName() + "#beforeJob started.");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
        if (exceptions.isEmpty()) {
            log.info(this.getClass().getName() + "#afterJob started.");
            return;
        }

        String errorReason = exceptions.stream().map(Throwable::getMessage).collect(Collectors.toList()).toString();
        log.error("This job has occurred some exceptions as follow. " +
                        "[job-name:{}] [size:{}]",
                jobExecution.getJobInstance().getJobName(), exceptions.size());
        log.error(errorReason);

        // Update DynamoDB
        String testId = executionContext.getString("testId");
        Date startTime = (Date) executionContext.get("startTime");
        RunnerStatus runnerStatus = runnerStatusService.getRunnerStatus(testId, startTime);
        runnerStatus.setStatus("failed");
        runnerStatus.setErrorReason(errorReason);
        runnerStatus.setEndTime(new Date());
        runnerStatusService.updateRunnerStatus(runnerStatus);

        // Update Codepipeline
        Approval approval = (Approval)executionContext.get("approval");
        if (Objects.nonNull(approval)) {
            ApprovalResult approvalResult = new ApprovalResult()
                    .withStatus(ApprovalStatus.Rejected)
                    .withSummary("Test has failed. (" + errorReason + ")");

            pipelineHelper.approvalPipeline(approval, approvalResult);
        }
    }
}