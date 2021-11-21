package org.maca.continuous.perftest.app.batch.partitioner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SamplePartitioner implements Partitioner {
    private String param;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> executionContextMap = new HashMap<>();
        try(InputStream inputStream = getClass().getResourceAsStream(param);
            Reader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            String readLine;
            int index = 0;
            while ((readLine = bufferedReader.readLine()) != null){
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.putString("partitionId", readLine);
                executionContextMap.put("partition" + index, executionContext);
                index++;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return executionContextMap;
    }
}