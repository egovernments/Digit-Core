package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.egov.sunbirdrc.models.AuditDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CredentialIdUuidMapper {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("credentialId")
    private String credentialId;

    @JsonProperty("did")
    private String issuerDid;

    private AuditDetails auditDetails;

}
