package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Where the registry posts a notification when a record of the schema changes. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfig {
    @JsonProperty("url")
    private String url;
    @JsonProperty("method")
    private String method;
    @JsonProperty("apiKey")
    private String apiKey;
    @JsonProperty("headers")
    private Map<String, String> headers;
    @JsonProperty("active")
    private boolean active;
}
