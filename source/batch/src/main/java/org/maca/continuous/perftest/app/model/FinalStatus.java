package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class FinalStatus {

	@JacksonXmlProperty(localName = "TestDuration")
	public double testDuration;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "Group")
	public List<Group> groupList;
}