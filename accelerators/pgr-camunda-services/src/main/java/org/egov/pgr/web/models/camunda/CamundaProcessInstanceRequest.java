package org.egov.pgr.web.models.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamundaProcessInstanceRequest {

    @JsonProperty("variables")
    private Map<String, Variable> variables;

    @JsonProperty("businessKey")
    private String businessKey;

} 