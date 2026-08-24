package org.digit.services.employee.model;

import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One call that provisions a login user, a person record and an employee, linked by the new
 * user's id. All or nothing: if any step fails the earlier ones are undone.
 *
 * <p>{@code individual} is passed through to the individual service untouched, so it is left as raw
 * JSON rather than duplicating that service's model here.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardRequest {
    @JsonProperty("user")
    private OnboardUser user;
    @JsonProperty("individual")
    private JsonNode individual;
    @JsonProperty("employee")
    private CreateEmployeeRequest employee;
}
