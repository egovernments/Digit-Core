package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.egov.common.contract.request.RequestInfo;
import org.egov.user.config.UserServiceConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
public class ActiveSessionSearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull(message = "tenantId is mandatory")
    @Pattern(regexp = UserServiceConstants.PATTERN_TENANT)
    @Size(max = 256)
    @JsonProperty("tenantId")
    private String tenantId;

    // Mandatory so the search is always scoped to an explicit set of users (e.g. a campaign's
    // users); an empty list is valid and yields an empty result.
    @NotNull(message = "userUuids is mandatory")
    @Size(max = 1000, message = "userUuids cannot contain more than 1000 entries")
    @JsonProperty("userUuids")
    private List<String> userUuids;
}
