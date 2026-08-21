package org.egov.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class OtpValidationRequest {
    private String otpReference;
    private String mobileNumber;
    private String countryCode;
    protected String tenantId;
}
