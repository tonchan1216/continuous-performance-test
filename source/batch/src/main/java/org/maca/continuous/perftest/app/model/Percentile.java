package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Percentile {
	public String name;
	public Double value;

	@JacksonXmlProperty(localName = "param", isAttribute = true)
	public String param;
}
