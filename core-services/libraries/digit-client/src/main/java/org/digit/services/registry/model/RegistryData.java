package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Registry data request model matching Go DataRequest struct.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistryData {

    @JsonIgnore
    private String tenantId;

    @JsonIgnore
    private String schemaCode;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("data")
    @NotNull
    private JsonNode data;
}