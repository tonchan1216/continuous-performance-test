package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "testsuites")
public class JUnitTestSuites {

    @JacksonXmlProperty(localName = "testsuite")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<JUnitTestSuite> testSuites;

    @Data
    public static class JUnitTestSuite {
        @JsonProperty(required = true)
        @JacksonXmlProperty(localName = "name", isAttribute = true)
        private String name;

        @JsonProperty(required = true)
        @JacksonXmlProperty(localName = "package_name", isAttribute = true)
        private String packageName;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "testcase")
        private List<JUnitTestCase> testCases;
    }
}