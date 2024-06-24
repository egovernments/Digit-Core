package org.egov.sunbirdrc.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CredentialIdResponse {

    private ResponseInfo responseInfo;
    private String credentialId;
    private String schemaId;
}
