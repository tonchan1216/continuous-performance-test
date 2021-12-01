package org.maca.continuous.perftest.domain.service;

import org.maca.continuous.perftest.domain.model.PrimaryKey;
import org.maca.continuous.perftest.domain.model.RunnerStatus;

import java.util.List;

public interface RunnerStatusService {
    RunnerStatus getRunnerStatus(PrimaryKey primaryKey);

    List<RunnerStatus> getRunnerStatus();

    RunnerStatus addRunnerStatus(RunnerStatus runnerStatus);

    RunnerStatus updateRunnerStatus(RunnerStatus runnerStatus);

    RunnerStatus deleteRunnerStatus(PrimaryKey primaryKey);
}
