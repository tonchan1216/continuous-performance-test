package org.maca.continuous.perftest.app.batch.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.model.Parameter;
import org.maca.continuous.perftest.app.model.RunnerStatusModel;
import org.maca.continuous.perftest.app.model.RunnerStatusModelMapper;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class InputTasklet implements Tasklet {
    @Autowired
    RunnerStatusService runnerStatusService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        //Get Parameters from SQS
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        String param = stepExecution.getJobParameters().getString("param");
        ObjectMapper mapper = new ObjectMapper();
        Parameter parameter = mapper.readValue(param, Parameter.class);

        // DynamoDB INSERT
        RunnerStatus runnerStatus = runnerStatusService.addRunnerStatus(
                RunnerStatusModelMapper.map(RunnerStatusModel.builder()
                        .clusterSize(parameter.clusterSize)
                        .scenarioName(parameter.scenarioName)
                        .build()
                )
        );

        // Set jobExecutionContext
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        jobExecutionContext.put("clusterSize", parameter.clusterSize);
        jobExecutionContext.put("scenarioName", parameter.scenarioName);
        jobExecutionContext.put("testId", runnerStatus.getTestId());

        return RepeatStatus.FINISHED;
    }
}