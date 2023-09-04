package org.egov.elasticrequestcrypt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EncryptedCorrelator {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("encryptedRequest")
    private Object encryptedRequest;

}
