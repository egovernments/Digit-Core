package org.digit.tracer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiDetails(
    @JsonProperty("id")                String id,
    @JsonProperty("url")               String url,
    @JsonProperty("contentType")       String contentType,
    @JsonProperty("methodType")        String methodType,
    @JsonProperty("requestBody")       String requestBody,
    @JsonProperty("requestHeaders")    Map<String, Object> requestHeaders,
    @JsonProperty("additionalDetails") Object additionalDetails
) {}
