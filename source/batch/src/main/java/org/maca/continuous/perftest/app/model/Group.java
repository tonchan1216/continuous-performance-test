package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Group {
	@JacksonXmlProperty(localName = "label", isAttribute = true)
	public String label;

	@JacksonXmlProperty(localName = "throughput")
	public CountMetrics throughput;

	@JacksonXmlProperty(localName = "concurrency")
	public CountMetrics concurrency;

	@JacksonXmlProperty(localName = "succ")
	public CountMetrics success;

	@JacksonXmlProperty(localName = "fail")
	public CountMetrics fail;

	@JacksonXmlProperty(localName = "avg_rt")
	public TimeMetrics avgResponseTime;

	@JacksonXmlProperty(localName = "stdev_rt")
	public TimeMetrics stdResponseTime;

	@JacksonXmlProperty(localName = "avg_lt")
	public TimeMetrics avgLatency;

	@JacksonXmlProperty(localName = "avg_ct")
	public TimeMetrics avgConnectTime;

	@JacksonXmlProperty(localName = "bytes")
	public CountMetrics bytes;

	@JacksonXmlProperty(localName = "rc")
	public ResponseCodeCount rc;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "perc")
	public List<Percentile> percentiles;

}
