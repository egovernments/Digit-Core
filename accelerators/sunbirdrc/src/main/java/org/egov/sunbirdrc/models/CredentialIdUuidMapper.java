package org.egov.sunbirdrc.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CredentialIdUuidMapper {

    private String uuid;
    private String credentialId;
    private String created_at;
    private String updated_at;

}
