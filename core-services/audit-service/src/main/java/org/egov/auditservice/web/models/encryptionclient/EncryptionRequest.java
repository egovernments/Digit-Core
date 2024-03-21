package org.egov.auditservice.web.models.encryptionclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptionRequest {

    @NotNull
    @JsonProperty("encryptionRequests")
    private List<EncReqObject> encryptionRequests;

}
