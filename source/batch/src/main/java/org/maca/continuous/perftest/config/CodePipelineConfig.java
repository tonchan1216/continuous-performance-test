package org.maca.continuous.perftest.config;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodePipelineConfig {

    @Value("${amazon.region}")
    private String region;

    @Bean
    public AWSCodePipeline awsCodePipeline(){
        return AWSCodePipelineClientBuilder.standard().withRegion(region).build();
    }
}
