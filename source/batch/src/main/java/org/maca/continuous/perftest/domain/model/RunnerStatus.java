package org.maca.continuous.perftest.domain.model;

import java.io.Serializable;

import lombok.*;

import org.springframework.data.annotation.Id;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@DynamoDBTable(tableName = "ma-furutanito-runner-status")
public class RunnerStatus implements Serializable {

    @DynamoDBHashKey
    private String testId;
    @DynamoDBAttribute
    private String scenarioName;
    @DynamoDBAttribute
    private String clusterSize;

}
