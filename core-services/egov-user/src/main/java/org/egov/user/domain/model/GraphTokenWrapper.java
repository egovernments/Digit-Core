package org.egov.user.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for Graph API access token encryption/decryption via egov-enc-service.
 * MDMS DataSecurity policy for model 'GraphToken' must define jsonPath $.token.
 */
@Getter
@Setter
public class GraphTokenWrapper {

    @JsonProperty("token")
    private String token;

}
