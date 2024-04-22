package org.egov.sunbirdrc.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CredentialRequest {
    @JsonProperty("@context")
    private List<String> context;
    private String id;
    private List<String> type;
    private String issuer;
    private String expirationDate;
    private Object credentialSubject;
}
