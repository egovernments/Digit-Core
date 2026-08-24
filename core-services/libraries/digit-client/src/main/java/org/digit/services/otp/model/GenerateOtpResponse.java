package org.digit.services.otp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The handle for the OTP just issued. The code itself is never returned — verification is by
 * {@code referenceId} plus the code the user supplies.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpResponse {
    @JsonProperty("referenceId")
    private String referenceId;
    @JsonProperty("expiresIn")
    private int expiresIn;
    @JsonProperty("cooldownSeconds")
    private int cooldownSeconds;
}
