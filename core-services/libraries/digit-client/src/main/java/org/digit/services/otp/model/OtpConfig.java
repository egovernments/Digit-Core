package org.digit.services.otp.model;

import org.digit.services.common.model.AuditDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-purpose OTP policy: code length, lifetime, and the rate limits applied to generating,
 * resending and verifying.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpConfig {
    @JsonProperty("id")
    private String id;
    @JsonProperty("purpose")
    private String purpose;
    @JsonProperty("otpLength")
    private int otpLength;
    @JsonProperty("ttlSeconds")
    private int ttlSeconds;
    @JsonProperty("cooldownSeconds")
    private int cooldownSeconds;
    @JsonProperty("maxAttempts")
    private int maxAttempts;
    @JsonProperty("lockoutSeconds")
    private int lockoutSeconds;
    @JsonProperty("maxGeneratesPerHour")
    private int maxGeneratesPerHour;
    @JsonProperty("maxResendsPerHour")
    private int maxResendsPerHour;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("contextBindingRequired")
    private boolean contextBindingRequired;
    @JsonProperty("version")
    private int version;
    @JsonProperty("auditDetail")
    private AuditDetails auditDetail;
}
