package org.egov.access.domain.model.authorize;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthorizationRequestWrapper {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("AuthorizationRequest")
    @Valid
    private AuthorizationRequest authorizationRequest;

}
