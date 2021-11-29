package org.maca.continuous.perftest.domain.service;

import org.maca.continuous.perftest.domain.model.RunnerStatus;

import java.util.List;

public interface RunnerStatusService {
    public RunnerStatus getRunnerStatus(String testId);

    public List<RunnerStatus> getRunnerStatus();

    public RunnerStatus addRunnerStatus(RunnerStatus runnerStatus);

    public RunnerStatus updateRunnerStatus(RunnerStatus runnerStatus);

    public RunnerStatus deleteRunnerStatus(String testId);
}
