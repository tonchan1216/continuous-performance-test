package org.maca.continuous.perftest.config;

import org.maca.continuous.perftest.app.batch.listener.SampleListener;
import org.maca.continuous.perftest.app.batch.partitioner.SamplePartitioner;
import org.maca.continuous.perftest.app.batch.step.SampleProcessor;
import org.maca.continuous.perftest.app.batch.step.SampleTasklet;
import org.maca.continuous.perftest.app.batch.step.SampleWriter;
import org.maca.continuous.perftest.app.model.Sample;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.sql.DataSource;

@Import(BatchInfraConfig.class)                                               // ...(A)
@Configuration
@EnableBatchProcessing
public class BatchAppConfig extends DefaultBatchConfigurer {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(@Qualifier("step1") Step step1, @Qualifier("step2") Step step2){
        return jobBuilderFactory.get("job")
                .listener(jobExecutionListener())
                .start(step1)
                .next(partionStep())
                .build();
    }

    @Bean
    public Step step1(){
        return stepBuilderFactory
                .get("step1")
                .tasklet(sampleTasklet())
                .build();
    }

    @Bean
    protected Step step2(){
        return stepBuilderFactory.get("step2")
                .<Sample, Sample>chunk(10)
                .reader(sampleFlatFileItemReader(null))
                .processor(sampleProcessor())
                .writer(sampleWriter())
                .build();
    }

    @Bean
    protected Step partionStep(){
        return stepBuilderFactory.get("partitionStep")
                .partitioner(step2())
                .partitioner("step2", partitioner(null))
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    @Value("#{jobExecutionContext['paramBySampleTasklet']}")
    public FlatFileItemReader<Sample> sampleFlatFileItemReader(String paramBySampleTasklet){

        FlatFileItemReader<Sample> flatFileItemReader = new FlatFileItemReader<>();

        flatFileItemReader.setResource(new DefaultResourceLoader().getResource(paramBySampleTasklet));

        DefaultLineMapper<Sample> defaultLineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("stepParam");
        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);

        BeanWrapperFieldSetMapper<Sample> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(Sample.class);
        defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(defaultLineMapper);

        return flatFileItemReader;
    }

    @Bean
    @StepScope
    protected ItemProcessor<Sample, Sample> sampleProcessor(){
        return new SampleProcessor();
    }

    @Bean
    @StepScope
    protected ItemWriter<Sample> sampleWriter(){
        return new SampleWriter();
    }

    @Bean
    protected Tasklet sampleTasklet(){
        return new SampleTasklet();
    }

    @Bean
    protected JobExecutionListener jobExecutionListener(){
        return new SampleListener();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        return simpleAsyncTaskExecutor;
    }

    @Bean
    @StepScope
    @Value("#{jobExecutionContext['paramBySampleTasklet']}")
    public Partitioner partitioner(String paramBySampleTasklet){
        return new SamplePartitioner(paramBySampleTasklet);
    }

    @Override
    @Autowired
    public void setDataSource(@Qualifier("batchDataSource") DataSource dataSource){
        super.setDataSource(dataSource);
    }
}