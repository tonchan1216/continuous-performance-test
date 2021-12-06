package org.maca.continuous.perftest.domain.service;

import org.maca.continuous.perftest.domain.model.PrimaryKey;
import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.maca.continuous.perftest.domain.repository.RunnerStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RunnerStatusServiceImpl implements RunnerStatusService {
    @Autowired
    RunnerStatusRepository runnerStatusRepository;

    @Override
    public RunnerStatus getRunnerStatus(PrimaryKey primaryKey){
        return runnerStatusRepository.findById(primaryKey).get();
    }

    @Override
    public RunnerStatus getRunnerStatus(String testId, Date startTime){
        PrimaryKey primaryKey = PrimaryKey.builder().testId(testId).startTime(startTime).build();
        return runnerStatusRepository.findById(primaryKey).get();
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
    public RunnerStatus deleteRunnerStatus(PrimaryKey primaryKey){
        RunnerStatus runnerStatuses = runnerStatusRepository.findById(primaryKey).get();
        runnerStatusRepository.deleteById(primaryKey);
        return runnerStatuses;
    }
}
