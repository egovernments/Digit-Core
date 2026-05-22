package org.egov.payment.clients.idgen.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateIdRequest {

    @JsonProperty("templateCode")
    private String templateCode;

    @JsonProperty("variables")
    private Map<String, String> variables;
}
