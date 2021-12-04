package org.maca.continuous.perftest.app.batch.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.common.app.model.Parameter;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Objects;

@Slf4j
public class InputTasklet implements Tasklet {
    @Autowired
    RunnerStatusService runnerStatusService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws JsonProcessingException {
        //Get Parameters from SQS
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        String param = stepExecution.getJobParameters().getString("param");
        Parameter parameter;
        ObjectMapper mapper = new ObjectMapper();
        parameter = mapper.readValue(param, Parameter.class);

        // set default parameter if not specified.
        if (Objects.isNull(parameter.clusterSize)) {
            parameter.setClusterSize("1");
        }

        if (Objects.isNull(parameter.scenarioName)) {
            if (Objects.nonNull(parameter.approval.customData)){
                parameter.setScenarioName(parameter.approval.customData.toString());
            } else {
                parameter.setScenarioName("default");
            }
        }

        // DynamoDB INSERT
        Date startTime = new Date();
        RunnerStatus runnerStatus = runnerStatusService.addRunnerStatus(
                RunnerStatus.builder()
                        .scenarioName(parameter.scenarioName)
                        .clusterSize(parameter.clusterSize)
                        .startTime(startTime)
                        .status("running")
                        .build()
        );

        // Set jobExecutionContext
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        jobExecutionContext.put("clusterSize", parameter.clusterSize);
        jobExecutionContext.put("scenarioName", parameter.scenarioName);
        jobExecutionContext.put("testId", runnerStatus.getTestId());
        jobExecutionContext.put("startTime", startTime);

        return RepeatStatus.FINISHED;
    }
}