package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryData {
    @JsonIgnore
    private String schemaCode;
    @JsonProperty(value="version")
    private Integer version;
    @JsonProperty(value="data")
    @NotNull
    private JsonNode data;
}