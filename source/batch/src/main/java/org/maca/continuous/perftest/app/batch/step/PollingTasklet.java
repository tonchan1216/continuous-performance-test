package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.*;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.domain.model.PrimaryKey;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class PollingTasklet implements Tasklet {

    @Value("${amazon.ecs.pollingInterval}")
    private String pollingInterval;

    @Value("${amazon.ecs.cluster}")
    private String cluster;

    @Value("${amazon.ecs.taskDefinition}")
    private String taskDefinition;

    @Value("#{jobExecutionContext['clusterSize']}")
    private String clusterSize;

    @Value("#{jobExecutionContext['testId']}")
    private String testId;

    @Value("#{jobExecutionContext['startTime']}")
    private Date startTime;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    AmazonECS amazonECS;

    @Autowired
    RunnerStatusService runnerStatusService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        Date endTime;

        //Get FARGATE Task ARNs
        ListTasksRequest listTasksRequest = new ListTasksRequest()
                .withCluster(cluster)
                .withFamily(taskDefinition);
        ListTasksResult familyTaskList = amazonECS.listTasks(listTasksRequest);

        //Filtered FARGATE Task ARNs
        DescribeTasksRequest initTasksRequest = new DescribeTasksRequest()
                .withCluster(cluster)
                .withTasks(familyTaskList.getTaskArns())
                .withInclude("TAGS");
        DescribeTasksResult initResponse = amazonECS.describeTasks(initTasksRequest);
        List<String> filteredTaskArns = initResponse.getTasks().stream()
                .filter(task -> task.getTags().contains(new Tag().withKey("TEST_ID").withValue(testId)))
                .map(Task::getTaskArn)
                .collect(Collectors.toList());

        if (filteredTaskArns.isEmpty()) {
            throw new Exception("Load test containers cannot running.");
        }

        // Polling Task Status
        DescribeTasksRequest pollingTasksRequest = new DescribeTasksRequest()
                .withCluster(cluster)
                .withTasks(filteredTaskArns);
        Long totalCount = Long.parseLong(clusterSize);
        while(true){
            try {
                Thread.sleep(Integer.parseInt(pollingInterval));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            DescribeTasksResult pollingResponse = amazonECS.describeTasks(pollingTasksRequest);
            Map<String, Long> countByStatus = pollingResponse.getTasks().stream()
                    .collect(Collectors.groupingBy(Task::getLastStatus, Collectors.counting()));

            log.info("Polling stats: " + countByStatus.toString());
            if (Objects.nonNull(countByStatus.get("STOPPED")) && Objects.equals(countByStatus.get("STOPPED"), totalCount)) {
                Map<Integer, Long> exitCodeList = pollingResponse.getTasks().stream()
                        .flatMap(task -> task.getContainers().stream())
                        .collect(Collectors.groupingBy(Container::getExitCode, Collectors.counting()));
                if (Objects.isNull(exitCodeList.get(0)) || exitCodeList.get(0) < totalCount) {
                    throw new Exception("Load test containers terminated abnormally.");
                }

                endTime = new Date();
                break;
            }
        }

        // DynamoDB Update
        PrimaryKey primaryKey = PrimaryKey.builder().testId(testId).startTime(startTime).build();
        RunnerStatus runnerStatus = runnerStatusService.getRunnerStatus(primaryKey);
        runnerStatus.setStatus("stop");
        runnerStatus.setCompleteTasks(filteredTaskArns);
        runnerStatus.setEndTime(endTime);
        runnerStatusService.updateRunnerStatus(runnerStatus);

        return RepeatStatus.FINISHED;
    }
}