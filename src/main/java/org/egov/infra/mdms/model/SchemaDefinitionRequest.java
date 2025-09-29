package org.egov.infra.mdms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Bind the Schema defination
 */
@Schema(description = "Bind the Schema defination")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SchemaDefinitionRequest {
    @JsonProperty("SchemaDefinition")
    @NotNull
    @Valid
    private SchemaDefinition schemaDefinition = null;


}
