package org.maca.continuous.perftest.app.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.domain.model.PrimaryKey;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Listener extends JobExecutionListenerSupport {
    @Autowired
    RunnerStatusService runnerStatusService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(this.getClass().getName() + "#beforeJob started.");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
        if (exceptions.isEmpty()) {
            log.info(this.getClass().getName() + "#afterJob started.");
            return;
        }

        log.error("This job has occurred some exceptions as follow. " +
                        "[job-name:{}] [size:{}]",
                jobExecution.getJobInstance().getJobName(), exceptions.size());
        exceptions.forEach(th -> log.error(th.getMessage()));

        String testId = jobExecution.getExecutionContext().getString("testId");
        Date startTime = (Date) jobExecution.getExecutionContext().get("startTime");
        PrimaryKey primaryKey = PrimaryKey.builder().testId(testId).startTime(startTime).build();
        RunnerStatus runnerStatus = runnerStatusService.getRunnerStatus(primaryKey);
        runnerStatus.setStatus("failed");
        runnerStatus.setErrorReason(exceptions.stream().map(Throwable::getMessage).collect(Collectors.toList()).toString());
        runnerStatus.setEndTime(new Date());
        runnerStatusService.updateRunnerStatus(runnerStatus);
    }
}