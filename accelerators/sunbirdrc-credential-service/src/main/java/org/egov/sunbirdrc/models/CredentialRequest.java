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
    private List<Object> context;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private List<String> type;

    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("expirationDate")
    private String expirationDate;

    @JsonProperty("credentialSubject")
    private Object credentialSubject;
}
