package org.maca.continuous.perftest.app.batch.step;

import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.model.Sample;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

@Slf4j
public class SampleProcessor implements ItemProcessor<Sample, Sample> {
    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public Sample process(Sample sample) throws Exception {
        ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        if(Objects.equals(sample.getStepParam(), stepExecutionContext.get("partitionId"))){
            log.info(this.getClass().getName()
                    + " started. sample.stepParam:" + sample.getStepParam()
                    + " stepExecution.partitionId:" + stepExecutionContext.getString("partitionId"));
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        return sample;
    }
}