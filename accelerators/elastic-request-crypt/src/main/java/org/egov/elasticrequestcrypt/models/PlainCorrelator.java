package org.egov.elasticrequestcrypt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.encryption.web.contract.EncryptionRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PlainCorrelator {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("encryptionRequest")
    private EncryptionRequest encryptionRequest;

}
