package org.maca.continuous.perftest.app.batch.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class RunTaskTasklet implements Tasklet {
    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Value("#{jobExecutionContext['testName']}")
    private String testName;

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

        log.info(testName + " will be started."
                + " starting Performance Test Job: " + stepExecutionContext.getString("partitionId"));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("InterruptedException");
            Thread.currentThread().interrupt();
        }
        return RepeatStatus.FINISHED;
    }
}