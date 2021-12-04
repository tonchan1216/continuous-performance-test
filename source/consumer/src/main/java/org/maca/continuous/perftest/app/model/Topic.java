package org.maca.continuous.perftest.app.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("jsonschema2pojo")
public class Topic {

    @JsonProperty("Type")
    public String type;
    @JsonProperty("MessageId")
    public String messageId;
    @JsonProperty("TopicArn")
    public String topicArn;
    @JsonProperty("Subject")
    public String subject;
    @JsonProperty("Message")
    public String message;
    @JsonProperty("Timestamp")
    public String timestamp;
    @JsonProperty("SignatureVersion")
    public String signatureVersion;
    @JsonProperty("Signature")
    public String signature;
    @JsonProperty("SigningCertURL")
    public String signingCertURL;
    @JsonProperty("UnsubscribeURL")
    public String unsubscribeURL;
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
