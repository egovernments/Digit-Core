package org.digit.services.otp.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Re-sends an existing OTP; subject to the purpose's cooldown and hourly resend cap. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequest {
    @JsonProperty("referenceId")
    private String referenceId;
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
