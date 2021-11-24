package org.maca.continuous.perftest.app.batch.step;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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

@Slf4j
public class ResultTasklet implements Tasklet {
    private static final String S3_BUCKET_PREFIX = "s3://";
    private static final String DELIMITER = "/";
    private static final String DIRECTORY_PREFIX = "results";
    private static final String ARTIFACT_DIRECTORY_SUFFIX = "artifacts";

    @Value("${bucket.name}")
    private String bucketName;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private ResourcePatternResolver resolver;

    @Autowired
    public void setupResolver(ApplicationContext applicationContext, AmazonS3 amazonS3){
        this.resolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, applicationContext);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) throws Exception {
        log.info(this.getClass().getName() + "#execute() started.");

        //TODO: ECS Fargate Polling

        //S3 Download
        Resource[] results = resolver.getResources(S3_BUCKET_PREFIX +
                bucketName + DELIMITER +
                DIRECTORY_PREFIX + DELIMITER +
                "0003" + DELIMITER +
                ARTIFACT_DIRECTORY_SUFFIX + "/*");
        String[] resultList = new String[results.length];
        for(int i = 0; i < results.length; i++){
            try(InputStream inputStream = results[i].getInputStream()){
                resultList[i] = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //TODO: Calculate Result

        //TODO: S3 Upload
        return RepeatStatus.FINISHED;
    }
}