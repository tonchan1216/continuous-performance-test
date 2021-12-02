package org.maca.continuous.perftest.config;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ECSConfig {

    @Value("${amazon.region}")
    private String region;

    @Bean
    public AmazonECS amazonECS(){
        return AmazonECSClientBuilder.standard().withRegion(region).build();
    }
}
