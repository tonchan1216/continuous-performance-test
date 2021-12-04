package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.maca.continuous.perftest.common.app.model.Approval;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Message {

    @JsonProperty("region")
    public String region;
    @JsonProperty("consoleLink")
    public String consoleLink;
    @JsonProperty("approval")
    public Approval approval;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}