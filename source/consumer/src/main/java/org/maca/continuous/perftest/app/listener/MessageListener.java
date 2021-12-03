package org.maca.continuous.perftest.app.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.app.model.Message;
import org.maca.continuous.perftest.app.model.Topic;
import org.maca.continuous.perftest.common.app.model.Parameter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableSqs
@Slf4j
public class MessageListener {
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @SqsListener(value = "${amazon.sqs.queueName}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMessage(String message) throws
            JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        ObjectMapper mapper = new ObjectMapper();
        //Parse message from SQS
        String params;
        try {
            Topic parsedTopic = mapper.readValue(message, Topic.class);
            Message parsedMessage = mapper.readValue(parsedTopic.message, Message.class);
            Parameter parameter = new Parameter();
            parameter.setApproval(parsedMessage.approval);
            params = mapper.writeValueAsString(parameter);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return;
        }

        log.info(params);
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        jobParameterMap.put("param", new JobParameter(params));
        jobParameterMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(jobParameterMap);
        jobLauncher.run(job, jobParameters);
    }
}
