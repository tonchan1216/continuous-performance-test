package org.maca.continuous.perftest.app.listener;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableSqs
public class MessageListener {
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @SqsListener(value = "MA-furutanito-sqs", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMessage(String message) throws
            JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        jobParameterMap.put("param", new JobParameter(message));
        jobParameterMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(jobParameterMap);
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        ExecutionContext executionContext = jobExecution.getExecutionContext();
    }
}
