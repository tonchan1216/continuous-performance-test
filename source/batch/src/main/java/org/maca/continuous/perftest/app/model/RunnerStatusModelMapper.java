package org.maca.continuous.perftest.app.model;

import org.maca.continuous.perftest.domain.model.RunnerStatus;

public interface RunnerStatusModelMapper {
    public static RunnerStatus map(RunnerStatusModel runnerStatusModel){
        return RunnerStatus.builder()
                .testId(runnerStatusModel.getTestId())
                .scenarioName(runnerStatusModel.getScenarioName())
                .clusterSize(runnerStatusModel.getClusterSize())
                .build();
    }

}
