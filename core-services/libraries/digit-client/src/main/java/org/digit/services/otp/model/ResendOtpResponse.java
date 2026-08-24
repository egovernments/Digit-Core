package org.digit.services.otp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The refreshed handle after a resend. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpResponse {
    @JsonProperty("referenceId")
    private String referenceId;
    @JsonProperty("expiresIn")
    private int expiresIn;
    @JsonProperty("cooldownSeconds")
    private int cooldownSeconds;
    @JsonProperty("purpose")
    private String purpose;
}
