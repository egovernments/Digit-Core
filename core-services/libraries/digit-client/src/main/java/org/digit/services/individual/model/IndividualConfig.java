package org.digit.services.individual.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-tenant individual validation config. Mirrors the service's {@code ConfigDTO}.
 *
 * <p>At least one of {@code mobileRegex}, {@code nameRegex} or {@code uniquenessCriteria} must be
 * set. {@code uniquenessCriteria} names the fields the service enforces as unique within the tenant —
 * a create that collides on one of them is rejected as a duplicate.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualConfig {
    @JsonProperty("mobileRegex")
    private String mobileRegex;
    @JsonProperty("nameRegex")
    private String nameRegex;
    @JsonProperty("uniquenessCriteria")
    private List<String> uniquenessCriteria;
    @JsonProperty("version")
    private int version;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
