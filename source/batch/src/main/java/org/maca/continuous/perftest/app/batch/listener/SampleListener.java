package org.maca.continuous.perftest.app.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

@Slf4j
public class SampleListener extends JobExecutionListenerSupport {
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(this.getClass().getName() + "#afterJob started.");
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(this.getClass().getName() + "#beforeJob started.");
    }
}