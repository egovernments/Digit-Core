package org.egov.elasticrequestcrypt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class HttpRequestLog {

    @JsonProperty("method")
    private String method;

    @JsonProperty("url")
    private String url;

    @JsonProperty("headers")
    private Map<String, Object> headers;

    @JsonProperty("body")
    private String body;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("queryParams")
    private Map<String, List<String>> queryParams;

    @JsonProperty("createdTime")
    private long createdTime;

}
