package org.maca.continuous.perftest.domain.repository;

import org.maca.continuous.perftest.domain.model.RunnerStatus;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface RunnerStatusRepository extends CrudRepository<RunnerStatus, String>{
}
