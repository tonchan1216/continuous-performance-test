package org.maca.continuous.perftest.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {
    public Double testDuration;
    public Integer throughput;
    public Integer concurrency;
    public Integer success;
    public Integer fail;

    public Double avgResponseTime;
    public Double avgLatency;
    public Double perc_90;
}
