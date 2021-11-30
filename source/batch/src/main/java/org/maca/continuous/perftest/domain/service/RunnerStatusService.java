package org.maca.continuous.perftest.domain.service;

import org.maca.continuous.perftest.domain.model.RunnerStatus;

import java.util.List;

public interface RunnerStatusService {
    RunnerStatus getRunnerStatus(String testId);

    List<RunnerStatus> getRunnerStatus();

    RunnerStatus addRunnerStatus(RunnerStatus runnerStatus);

    RunnerStatus updateRunnerStatus(RunnerStatus runnerStatus);

    RunnerStatus deleteRunnerStatus(String testId);
}
