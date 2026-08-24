package org.digit.services.employee.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The provisioned login user. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardUserResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("roles")
    private List<String> roles;
}
