package org.digit.services.employee.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The login user to provision. {@code mobileNumber} becomes the username. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardUser {
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    @JsonProperty("password")
    private String password;
    @JsonProperty("email")
    private String email;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("emailVerified")
    private boolean emailVerified;
    @JsonProperty("roles")
    private List<String> roles;
}
