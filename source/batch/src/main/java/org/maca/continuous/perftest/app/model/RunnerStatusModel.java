package org.maca.continuous.perftest.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RunnerStatusModel implements Serializable {
    private String testId;
    private String scenarioName;
    private String clusterSize;
}
