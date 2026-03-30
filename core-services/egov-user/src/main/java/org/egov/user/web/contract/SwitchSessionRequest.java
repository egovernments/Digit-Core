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

@Getter
@Setter
@ToString
public class SwitchSessionRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull(message = "username is mandatory")
    @Size(max = 64)
    @JsonProperty("username")
    private String username;

    @NotNull(message = "password is mandatory")
    @JsonProperty("password")
    private String password;

    @NotNull(message = "tenantId is mandatory")
    @Pattern(regexp = UserServiceConstants.PATTERN_TENANT)
    @Size(max = 256)
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull(message = "deviceSwitchReason is mandatory")
    @JsonProperty("deviceSwitchReason")
    private String deviceSwitchReason;

    @JsonProperty("deviceSwitchComment")
    private String deviceSwitchComment;
}
