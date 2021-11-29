package org.maca.continuous.perftest.config;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ECSConfig {

    @Bean
    public AmazonECS amazonECS(){
        return AmazonECSClientBuilder.standard().build();
    }
}
