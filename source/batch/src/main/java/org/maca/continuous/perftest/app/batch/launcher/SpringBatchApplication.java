package org.maca.continuous.perftest.app.batch.launcher;

import org.maca.continuous.perftest.config.BatchAppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@Import(BatchAppConfig.class)
@SpringBootApplication
public class SpringBatchApplication {
    public static void main(String[] args) {
        String inputParam = "param={" +
                "\"approval\": {" +
                "\"pipelineName\": \"codepipeline\"," +
                "\"stageName\": \"Approve\"," +
                "\"actionName\": \"Approval\"," +
                "\"token\": \"6b7a1872-bed6-4ad1-9d9c-c2ba1321ec6d\"," +
                "\"expires\": \"2019-11-26T02:25Z\"," +
                "\"customData\": \"scenario1\"" +
                "}" +
                "}";
        new SpringApplicationBuilder(SpringBatchApplication.class)
                .web(WebApplicationType.NONE)
                .run(inputParam);
    }
}