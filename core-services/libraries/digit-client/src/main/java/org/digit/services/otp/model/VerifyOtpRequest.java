package org.digit.services.otp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Verifies a code against an issued OTP. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    @JsonProperty("referenceId")
    private String referenceId;
    @JsonProperty("purpose")
    private String purpose;
    @JsonProperty("otp")
    private String otp;
    @JsonProperty("sessionId")
    private String sessionId;
}
