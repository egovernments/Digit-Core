package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.model.LoginAudit;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAuditResponse {

    @JsonProperty("responseInfo")
    ResponseInfo responseInfo;

    @JsonProperty("LoginAudits")
    List<LoginAudit> loginAudits;
}
