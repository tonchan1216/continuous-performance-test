package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class JUnitTestCase {
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "classname", isAttribute = true)
    private String className;

    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "error")
    private JUnitError error;
}