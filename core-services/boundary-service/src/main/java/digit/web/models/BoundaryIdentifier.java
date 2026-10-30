package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BoundaryIdentifier maps a boundary (by its code within a hierarchy) to an identifierId.
 * Within a tenant and hierarchyType the mapping is one-to-one: a boundary code has at most one
 * identifierId and an identifierId is assigned to at most one boundary code.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryIdentifier {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId = null;

    @JsonProperty("hierarchyType")
    @NotNull
    private String hierarchyType = null;

    @JsonProperty("code")
    @NotNull
    private String code = null;

    @JsonProperty("identifierId")
    @NotNull
    private String identifierId = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
}
