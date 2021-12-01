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
    @Value("${ecs.cluster}")
    private String cluster;

    @Value("${ecs.taskDefinition}")
    private String taskDefinition;

    @Value("${ecs.containerName}")
    private String containerName;

    @Value("${ecs.subnetIds}")
    private String subnetIds;

    @Value("${ecs.securityGroup}")
    private String securityGroup;

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
        int partitionId  = stepExecutionContext.getInt("partitionId");
        log.info(scenarioName + " will be started."
                + "starting Performance Test Job: partition " + partitionId);

        // ECS Fargate Run Task
        String[] subnetList = subnetIds.split(",");
        AwsVpcConfiguration awsVpcConfiguration = new AwsVpcConfiguration()
                .withSubnets(subnetList[partitionId % subnetList.length]) // round-robin AZ subnet
                .withSecurityGroups(securityGroup);
        NetworkConfiguration networkConfiguration = new NetworkConfiguration().withAwsvpcConfiguration(awsVpcConfiguration);
        ContainerOverride containerOverride = new ContainerOverride()
                .withName(containerName)
                .withCommand(testId, scenarioName, "-o settings.env.CLUSTER_SIZE=" + clusterSize, "-o settings.env.PARTITION_ID=" + partitionId);
        TaskOverride taskOverride = new TaskOverride().withContainerOverrides(containerOverride);
        RunTaskRequest request = new RunTaskRequest()
                .withCluster(cluster)
                .withTaskDefinition(taskDefinition)
                .withLaunchType(LaunchType.FARGATE)
                .withNetworkConfiguration(networkConfiguration)
                .withOverrides(taskOverride)
                .withTags(new Tag().withKey("TEST_ID").withValue(testId));
        RunTaskResult response = amazonECS.runTask(request);

        if (!response.getFailures().isEmpty()) {
            throw new Exception(response.getFailures().stream().map(Failure::getReason).collect(Collectors.toList()).toString());
        }

        return RepeatStatus.FINISHED;
    }
}