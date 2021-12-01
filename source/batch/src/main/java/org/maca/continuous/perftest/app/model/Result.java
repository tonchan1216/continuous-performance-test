package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    @JsonProperty("test_duration")
    public Double testDuration;

    @JsonProperty("throughput")
    public Integer throughput;

    @JsonProperty("concurrency")
    public Integer concurrency;

    @JsonProperty("success")
    public Integer success;

    @JsonProperty("fail")
    public Integer fail;

    @JsonProperty("avg_response_time")
    public Double avgResponseTime;

    @JsonProperty("avg_latency")
    public Double avgLatency;

    @JsonProperty("perc_90")
    public Double perc90;
}
