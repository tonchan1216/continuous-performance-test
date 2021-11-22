package org.maca.continuous.perftest.app.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pipelineName",
        "stageName",
        "actionName",
        "token",
        "expires",
        "customData"
})
@Generated("jsonschema2pojo")
public class Approval {

    @JsonProperty("pipelineName")
    public String pipelineName;
    @JsonProperty("stageName")
    public String stageName;
    @JsonProperty("actionName")
    public String actionName;
    @JsonProperty("token")
    public String token;
    @JsonProperty("expires")
    public String expires;
    @JsonProperty("customData")
    public Object customData;
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