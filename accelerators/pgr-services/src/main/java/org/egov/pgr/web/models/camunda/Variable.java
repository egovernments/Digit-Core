package org.egov.pgr.web.models.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variable {

    @JsonProperty("value")
    private Object value;

    @JsonProperty("type")
    private String type;
} 