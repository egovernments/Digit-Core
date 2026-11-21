package org.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateIDResponse {
    @JsonProperty(value="templateCode")
    private String templateCode;
    @JsonProperty(value="Version")
    private String version;
    @JsonProperty(value="id")
    private String id;
    @JsonProperty(value="tenantId")
    private String tenantId;
}