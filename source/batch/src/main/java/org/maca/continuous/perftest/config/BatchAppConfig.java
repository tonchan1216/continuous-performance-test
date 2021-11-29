package org.maca.continuous.perftest.config;

import org.maca.continuous.perftest.app.batch.listener.Listener;
import org.maca.continuous.perftest.app.batch.partitioner.DistributedPartitioner;
import org.maca.continuous.perftest.app.batch.step.InputTasklet;
import org.maca.continuous.perftest.app.batch.step.ResultTasklet;
import org.maca.continuous.perftest.app.batch.step.RunTaskTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;

@ComponentScan("org.maca.continuous.perftest.config")
@Configuration
@EnableBatchProcessing
public class BatchAppConfig extends DefaultBatchConfigurer {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(@Qualifier("step1") Step step1, @Qualifier("step2") Step step2){
        return jobBuilderFactory.get("performanceTestJob")
                .listener(jobExecutionListener())
                .start(step1)
                .next(partitionStep())
                .next(step3())
                .build();
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory
                .get("step1")
                .tasklet(inputTasklet())
                .build();
    }

    @Bean
    protected Step step2(){
        return stepBuilderFactory
                .get("step2")
                .tasklet(runTaskTasklet())
                .build();
    }

    @Bean
    protected Step step3(){
        return stepBuilderFactory
                .get("step3")
                .tasklet(resultTasklet())
                .build();
    }

    @Bean
    protected Step partitionStep(){
        return stepBuilderFactory.get("partitionStep")
                .partitioner(step2().getName(), partitioner(null))
                .step(step2())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    protected Tasklet inputTasklet(){
        return new InputTasklet();
    }

    @Bean
    @StepScope
    protected Tasklet runTaskTasklet(){
        return new RunTaskTasklet();
    }

    @Bean
    @StepScope
    protected Tasklet resultTasklet(){
        return new ResultTasklet();
    }

    @Bean
    protected JobExecutionListener jobExecutionListener(){
        return new Listener();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        return simpleAsyncTaskExecutor;
    }

    @Bean
    @StepScope
    @Value("#{jobExecutionContext['clusterSize']}")
    public Partitioner partitioner(String clusterSize){
        return new DistributedPartitioner(clusterSize);
    }

    @Override
    @Autowired
    public void setDataSource(@Qualifier("batchDataSource") DataSource dataSource){
        super.setDataSource(dataSource);
    }
}