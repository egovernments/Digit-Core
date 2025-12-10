package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registry response model matching Go Response struct.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryDataResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("data")
    private Object data;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;
}