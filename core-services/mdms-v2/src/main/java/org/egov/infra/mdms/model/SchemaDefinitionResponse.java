package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Response from server
 */
@Schema(description = "Response from server")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemaDefinitionResponse {
    @JsonProperty("ResponseInfo")

    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("SchemaDefinitions")
    @Valid
    private List<SchemaDefinition> schemaDefinitions = null;


    public SchemaDefinitionResponse addSchemaDefinitionsItem(SchemaDefinition schemaDefinitionsItem) {
        if (this.schemaDefinitions == null) {
            this.schemaDefinitions = new ArrayList<>();
        }
        this.schemaDefinitions.add(schemaDefinitionsItem);
        return this;
    }

}
