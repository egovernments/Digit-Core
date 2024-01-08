package com.example.geoJsonService.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
//import jdk.nashorn.internal.ir.ObjectNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feature {

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("geometry")
    private JsonNode geometry =null;

    @JsonProperty("properties")
    private ObjectNode properties =null;

}
