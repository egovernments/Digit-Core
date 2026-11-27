package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemaDefinitionHistory {

    @JsonProperty("changeDetails")
    private ChangeDetails changeDetails = null;

    @JsonProperty("SchemaDefinition")
    private SchemaDefinition schemaDefinition = null;
}
