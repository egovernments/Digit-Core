package com.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdGenGenerateRequest {

    @JsonProperty("templateCode")
    private String templateCode;

    @JsonProperty("variables")
    private Map<String, String> variables = new HashMap<>();
}
