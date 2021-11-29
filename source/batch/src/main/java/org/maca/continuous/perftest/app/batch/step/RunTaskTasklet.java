package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.stream.Collectors;

@Slf4j
public class RunTaskTasklet implements Tasklet {
    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Value("#{jobExecutionContext['clusterSize']}")
    private String clusterSize;

    @Value("#{jobExecutionContext['scenarioName']}")
    private String scenarioName;

    @Value("#{jobExecutionContext['testId']}")
    private String testId;

    @Autowired
    AmazonECS amazonECS;

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        //Get Partition ID
        ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
        String partitionId  = stepExecutionContext.getString("partitionId");
        log.info(scenarioName + " will be started."
                + "starting Performance Test Job: " + partitionId);

        // ECS Fargate Run Task
        AwsVpcConfiguration awsVpcConfiguration = new AwsVpcConfiguration()
                .withSubnets("subnet-02fb5c9a3dceb48c1")
                .withSecurityGroups("sg-0fc69388b99eb3893");
        NetworkConfiguration networkConfiguration = new NetworkConfiguration().withAwsvpcConfiguration(awsVpcConfiguration);
        ContainerOverride containerOverride = new ContainerOverride()
                .withName("ma-furutanito-load-test")
                .withCommand(testId, scenarioName);
        TaskOverride taskOverride = new TaskOverride().withContainerOverrides(containerOverride);
        RunTaskRequest request = new RunTaskRequest()
                .withCluster("ma-furutanito-cluster")
                .withTaskDefinition("ma-furutanito-load-test:9")
                .withLaunchType(LaunchType.FARGATE)
                .withNetworkConfiguration(networkConfiguration)
                .withOverrides(taskOverride)
                .withTags(new Tag().withKey("TEST_ID").withValue(testId));

        RunTaskResult response = amazonECS.runTask(request);

        if (response.getFailures().size() > 0) {
            throw new Exception(response.getFailures().stream().map(Failure::getReason).collect(Collectors.toList()).toString());
        }

        return RepeatStatus.FINISHED;
    }
}