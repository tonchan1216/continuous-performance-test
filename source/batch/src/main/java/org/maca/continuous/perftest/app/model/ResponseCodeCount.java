package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class ResponseCodeCount {
	public String name;
	public Integer value;

	@JacksonXmlProperty(localName = "param", isAttribute = true)
	public String param;
}
