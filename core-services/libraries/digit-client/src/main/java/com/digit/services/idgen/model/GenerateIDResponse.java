package com.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateIDResponse {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("templateCode")
    private String templateCode;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("id")
    private String id;
}
