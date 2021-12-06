package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class JUnitError {
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "message")
    private String message;

    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "type")
    private String type;
}