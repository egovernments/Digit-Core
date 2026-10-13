package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryDataResponse {
    @JsonProperty(value="success")
    private Boolean success;
    @JsonProperty(value="data")
    private Object data;
    @JsonProperty(value="error")
    private String error;
    @JsonProperty(value="message")
    private String message;
}