package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAuditSearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("loginStatus")
    private String loginStatus;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("fromDate")
    private Long fromDate;

    @JsonProperty("toDate")
    private Long toDate;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("offset")
    private Integer offset;


}
