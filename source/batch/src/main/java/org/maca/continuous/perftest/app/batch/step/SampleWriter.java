package org.maca.continuous.perftest.app.batch.step;

import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.model.Sample;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

@Slf4j
public class SampleWriter implements ItemWriter<Sample> {
    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public void write(List samples) throws Exception {
        ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
        log.info(this.getClass().getName() + " started.");
        samples.stream()
                .filter(sample -> Objects.equals(((Sample) sample).getStepParam(), stepExecutionContext.get("partitionId")))
                .forEach(sample -> {
                    log.info(this.getClass().getName() + " sample.stepParam:" + ((Sample) sample).getStepParam());
                });
        stepExecutionContext.put("status", "complete!");
    }
}