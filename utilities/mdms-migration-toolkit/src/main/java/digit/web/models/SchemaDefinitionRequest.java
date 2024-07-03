package digit.web.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SchemaDefinitionRequest {
    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("SchemaDefinition")
    @NotNull
    @Valid
    private SchemaDefinition schemaDefinition = null;
}
