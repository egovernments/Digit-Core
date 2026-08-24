package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A reference from a field of one schema to a record of another. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefSchema {
    @JsonProperty("fieldPath")
    private String fieldPath;
    @JsonProperty("schemaCode")
    private String schemaCode;
    @JsonProperty("refField")
    private String refField;
    @JsonProperty("external")
    private Boolean external;
    @JsonProperty("registry")
    private String registry;
}
