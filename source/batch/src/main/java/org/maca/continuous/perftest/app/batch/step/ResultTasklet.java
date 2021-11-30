package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.maca.continuous.perftest.app.model.*;
import org.maca.continuous.perftest.domain.service.RunnerStatusService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ResultTasklet implements Tasklet {
    private static final String S3_BUCKET_PREFIX = "s3://";
    private static final String DELIMITER = "/";
    private static final String DIRECTORY_PREFIX = "results";
    private static final String ARTIFACT_DIRECTORY_SUFFIX = "artifacts";

    @Value("${bucket.name}")
    private String bucketName;

    @Value("#{jobExecutionContext['clusterSize']}")
    private String clusterSize;

    @Value("#{jobExecutionContext['scenarioName']}")
    private String scenarioName;

    @Value("#{jobExecutionContext['testId']}")
    private String testId;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private ResourcePatternResolver resolver;

    @Autowired
    AmazonECS amazonECS;

    @Autowired
    RunnerStatusService runnerStatusService;

    @Autowired
    public void setupResolver(ApplicationContext applicationContext, AmazonS3 amazonS3){
        this.resolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        //Get FARGATE Task ARNs
        ListTasksRequest listTasksRequest = new ListTasksRequest()
                .withCluster("ma-furutanito-cluster")
                .withFamily("ma-furutanito-load-test");
        ListTasksResult familyTaskList = amazonECS.listTasks(listTasksRequest);

        //Filtered FARGATE Task ARNs
        DescribeTasksRequest initTasksRequest = new DescribeTasksRequest()
                .withCluster("ma-furutanito-cluster")
                .withTasks(familyTaskList.getTaskArns())
                .withInclude("TAGS");
        DescribeTasksResult initResponse = amazonECS.describeTasks(initTasksRequest);
        List<String> filteredTaskArns = initResponse.getTasks().stream()
                .filter(task -> task.getTags().contains(new Tag().withKey("TEST_ID").withValue(testId)))
                .map(Task::getTaskArn)
                .collect(Collectors.toList());

        // Polling Task Status
        DescribeTasksRequest pollingTasksRequest = new DescribeTasksRequest()
                .withCluster("ma-furutanito-cluster")
                .withTasks(filteredTaskArns);
        Long totalCount = Long.parseLong(clusterSize);
        while(true){
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }

            DescribeTasksResult pollingResponse = amazonECS.describeTasks(pollingTasksRequest);
            Map<String, Long> countByStatus = pollingResponse.getTasks().stream()
                    .collect(Collectors.groupingBy(Task::getLastStatus, Collectors.counting()));

            log.info(countByStatus.toString());
            if (Objects.nonNull(countByStatus.get("STOPPED")) && Objects.equals(countByStatus.get("STOPPED"), totalCount)) {
                break;
            }
        }

        //S3 Download
        Resource[] results = resolver.getResources(S3_BUCKET_PREFIX +
                bucketName + DELIMITER +
                DIRECTORY_PREFIX + DELIMITER +
                testId + DELIMITER +
                ARTIFACT_DIRECTORY_SUFFIX  + "/*/results.xml");
        String[] resultList = new String[results.length];
        for(int i = 0; i < results.length; i++){
            try(InputStream inputStream = results[i].getInputStream()){
                resultList[i] = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // Parse XML
        XmlMapper xmlMapper = new XmlMapper();
        List<FinalStatus> finalStatusList = new ArrayList<>();
        for (String s : resultList) {
            log.info(s);
            finalStatusList.add(xmlMapper.readValue(s, FinalStatus.class));
        }

        if (finalStatusList.isEmpty()) {
            throw new Exception("empty result");
        }

        //Calculate Result
        List<Group> grpByLabel = finalStatusList.stream()
                .flatMap(g -> g.getGroupList().stream())
                .collect(Collectors.groupingBy(Group::getLabel)).get("");

        Result result = Result.builder()
                .testDuration(finalStatusList.stream()
                        .map(FinalStatus::getTestDuration)
                        .mapToDouble(v -> v)
                        .average().orElse(0.0))
                .throughput(grpByLabel.stream()
                        .map(Group::getThroughput)
                        .mapToInt(CountMetrics::getValue).sum())
                .concurrency(grpByLabel.stream()
                        .map(Group::getConcurrency)
                        .mapToInt(CountMetrics::getValue).sum())
                .success(grpByLabel.stream()
                        .map(Group::getSuccess)
                        .mapToInt(CountMetrics::getValue).sum())
                .fail(grpByLabel.stream()
                        .map(Group::getFail)
                        .mapToInt(CountMetrics::getValue).sum())
                .avgResponseTime(grpByLabel.stream()
                        .map(Group::getAvgResponseTime)
                        .mapToDouble(TimeMetrics::getValue)
                        .average().orElse(0.0))
                .perc_90(grpByLabel.stream()
                        .map(Group::getPercentiles)
                        .flatMap(Collection::stream)
                        .filter(p -> p.getParam().equals("90.0"))
                        .collect(Collectors.averagingDouble(Percentile::getValue)))
                .build();
        log.info(result.toString());

        //TODO: DynamoDB Update
        runnerStatusService.updateRunnerStatus(
                RunnerStatusModelMapper.map(RunnerStatusModel.builder()
                        .testId(testId)
                        .clusterSize(clusterSize)
                        .scenarioName(scenarioName)
                        .build()
                )
        );

        return RepeatStatus.FINISHED;
    }
}