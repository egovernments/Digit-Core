package org.egov.payment.clients.idgen.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateIdResponse {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("templateCode")
    private String templateCode;

    @JsonProperty("version")
    private String version;

    @JsonProperty("id")
    private String id;
}
