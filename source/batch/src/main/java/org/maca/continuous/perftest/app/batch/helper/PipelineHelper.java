package org.maca.continuous.perftest.app.batch.helper;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.model.AWSCodePipelineException;
import com.amazonaws.services.codepipeline.model.ApprovalResult;
import com.amazonaws.services.codepipeline.model.PutApprovalResultRequest;
import lombok.extern.slf4j.Slf4j;
import org.maca.continuous.perftest.common.app.model.Approval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PipelineHelper {
    @Autowired
    AWSCodePipeline awsCodePipeline;

    public void approvalPipeline (Approval approval, ApprovalResult approvalResult) {
        PutApprovalResultRequest putApprovalResultRequest = new PutApprovalResultRequest()
                .withActionName(approval.actionName)
                .withPipelineName(approval.pipelineName)
                .withStageName(approval.stageName)
                .withToken(approval.token)
                .withResult(approvalResult);

        try {
            awsCodePipeline.putApprovalResult(putApprovalResultRequest);
        } catch (AWSCodePipelineException e) {
            log.error(e.getMessage());
        }
    }
}
