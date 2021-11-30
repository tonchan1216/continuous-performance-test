package org.maca.continuous.perftest.app.batch.partitioner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DistributedPartitioner implements Partitioner {
    private String param;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> executionContextMap = new HashMap<>();
        int clusterSize = Integer.parseInt(param);

        for (int i = 0; i < clusterSize; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putInt("partitionId", i + 1);
            executionContextMap.put("partition" + i, executionContext);
        }

        return executionContextMap;
    }
}