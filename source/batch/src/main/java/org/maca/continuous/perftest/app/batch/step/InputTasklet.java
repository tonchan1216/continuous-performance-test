package org.maca.continuous.perftest.app.batch.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.model.Parameter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class InputTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {

        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        String param = stepExecution.getJobParameters().getString("param");
        ObjectMapper mapper = new ObjectMapper();
        Parameter parameter = mapper.readValue(param, Parameter.class);

        log.info(this.getClass().getName() + "#execute() started.");
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        jobExecutionContext.put("clusterSize", parameter.clusterSize);
        jobExecutionContext.put("testName", parameter.testName);
        return RepeatStatus.FINISHED;
    }
}