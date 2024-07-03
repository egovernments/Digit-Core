package digit.web.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mdms {
    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 128)
    private String tenantId = null;

    @JsonProperty("schemaCode")
    @Size(min = 2, max = 128)
    private String schemaCode = null;

    @JsonProperty("uniqueIdentifier")
    @Size(min = 2, max = 128)
    private String uniqueIdentifier = null;

    @JsonProperty("data")
    @NotNull
    private JsonNode data = null;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;
}
