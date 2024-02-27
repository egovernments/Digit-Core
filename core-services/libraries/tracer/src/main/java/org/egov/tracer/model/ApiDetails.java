package org.egov.tracer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiDetails {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("contentType")
    private String contentType = null;

    @JsonProperty("methodType")
    private String methodType = null;

    @JsonProperty("requestBody")
    private String requestBody = null;

    @JsonProperty("requestHeaders")
    private Map<String, Object> requestHeaders = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

}
