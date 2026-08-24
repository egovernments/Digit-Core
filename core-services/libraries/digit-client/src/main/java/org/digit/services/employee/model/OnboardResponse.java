package org.digit.services.employee.model;

import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The three linked records onboarding created. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardResponse {
    @JsonProperty("user")
    private OnboardUserResponse user;
    @JsonProperty("individual")
    private JsonNode individual;
    @JsonProperty("employee")
    private Employee employee;
}
