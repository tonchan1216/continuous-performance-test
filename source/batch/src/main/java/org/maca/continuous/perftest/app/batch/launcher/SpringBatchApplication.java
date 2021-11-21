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
        String inputParam = "param=1";
        new SpringApplicationBuilder(SpringBatchApplication.class)
                .web(WebApplicationType.NONE)
                .run(inputParam);
    }
}