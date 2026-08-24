package org.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateIDResponse {
    @JsonProperty(value="templateCode")
    private String templateCode;
    @JsonProperty(value="version")
    private String version;
    @JsonProperty(value="id")
    private String id;
}