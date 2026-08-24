package org.digit.services.otp.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Asks for an OTP to be issued to {@code identifier} — a mobile number or email, depending on
 * what the purpose is configured to deliver to.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpRequest {
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("purpose")
    private String purpose;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
