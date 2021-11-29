package org.maca.continuous.perftest.domain.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
