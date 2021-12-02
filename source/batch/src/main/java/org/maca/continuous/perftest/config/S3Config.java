package org.maca.continuous.perftest.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${amazon.region}")
    private String region;

    @Bean
    public AmazonS3 amazonS3(){
        return AmazonS3ClientBuilder.standard().withRegion(region).build();
    }
}