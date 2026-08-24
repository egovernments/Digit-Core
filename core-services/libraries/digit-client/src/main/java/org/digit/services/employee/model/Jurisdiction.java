package org.digit.services.employee.model;

import org.digit.services.common.model.AuditDetails;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A jurisdiction as returned by the service. Mirrors its {@code JurisdictionResponse}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * Jackson would otherwise publish each is-prefixed boolean twice: once under the name its
 * @JsonProperty gives it, and again under the name inferred from Lombok's isX() getter — so
 * {"isActive":false,"active":false}. Services that parse strictly reject the second key outright,
 * which made every individual write fail; the rest silently ignored it. Suppressing is-getter
 * detection leaves the annotated fields as the single source of the wire contract.
 */
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Jurisdiction {
    @JsonProperty("id")
    private String id;
    @JsonProperty("employeeId")
    private String employeeId;
    @JsonProperty("boundaryRelation")
    private List<BoundaryRef> boundaryRelation;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("version")
    private int version;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
