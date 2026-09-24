package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.model.enums.UserType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Request to create the tenant-mapping rows of users that already exist.
 *
 * <p>Mappings are written when a user is created, so users created before that behaviour
 * existed have none and are invisible to the shared-login tenant lookup. This backfills
 * them for named users.</p>
 */
@Getter
@Setter
public class TenantMappingUpsertRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull(message = "Tenant ID is required")
    @Size(max = 256, message = "Tenant ID must not exceed 256 characters")
    @JsonProperty("tenantId")
    private String tenantId;

    /** Defaults to EMPLOYEE when absent. */
    @JsonProperty("userType")
    private UserType userType;

    @JsonProperty("userNames")
    private List<String> userNames;

    @JsonProperty("userIds")
    private List<Long> userIds;
}
