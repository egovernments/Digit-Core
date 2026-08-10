package org.egov.domain.model;

import lombok.*;
import org.egov.web.contract.RequestInfo;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Domain model representing an OTP request.
 * This is a pure DTO - validation logic is handled by OtpRequestValidator.
 */
@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@NoArgsConstructor
public class OtpRequest {

	private RequestInfo requestInfo;

	@Setter
    private String mobileNumber;

    private String tenantId;

    private OtpRequestType type;

    private String userType;

    @Setter
    private String countryCode;

	@Setter
	private MobileValidationConfig mdmsValidationConfig;

	@Setter
	@Getter
	private String mdmsValidationErrorMessage;

	public boolean isRegistrationRequestType() {
    	return OtpRequestType.REGISTER.equals(getType());
	}

	public boolean isLoginRequestType() {
    	return OtpRequestType.LOGIN.equals(getType());
	}

	public boolean isInvalidType() {
    	return isEmpty(type);
	}

	public boolean isTenantIdAbsent() {
        return isEmpty(tenantId);
    }

    public boolean isMobileNumberAbsent() {
        return isEmpty(mobileNumber);
    }

	public boolean hasMdmsValidationError() {
		return mdmsValidationErrorMessage != null && !mdmsValidationErrorMessage.isEmpty();
	}
}
