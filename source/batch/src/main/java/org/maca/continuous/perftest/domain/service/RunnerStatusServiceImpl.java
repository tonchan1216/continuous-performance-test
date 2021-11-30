package org.maca.continuous.perftest.domain.service;

import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.repository.RunnerStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RunnerStatusServiceImpl implements RunnerStatusService {
    @Autowired
    RunnerStatusRepository runnerStatusRepository;

    @Override
    public RunnerStatus getRunnerStatus(String testId){
        return runnerStatusRepository.findById(testId).get();
    }

    @Override
    public List<RunnerStatus> getRunnerStatus(){
        List<RunnerStatus> runnerStatuses = new ArrayList<>();
        runnerStatusRepository.findAll().iterator().forEachRemaining(runnerStatuses::add);
        return runnerStatuses;
    }

    @Override
    public RunnerStatus addRunnerStatus(RunnerStatus runnerStatus){
        return runnerStatusRepository.save(runnerStatus);
    }

    @Override
    public RunnerStatus updateRunnerStatus(RunnerStatus runnerStatus){
        return runnerStatusRepository.save(runnerStatus);
    }

    @Override
    public RunnerStatus deleteRunnerStatus(String testId){
        RunnerStatus runnerStatuses = runnerStatusRepository.findById(testId).get();
        runnerStatusRepository.deleteById(testId);
        return runnerStatuses;
    }
}
