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
	private MobileValidationConfig mdmsValidationConfig;

	@Setter
	private String defaultNumericPattern;

	@Setter
	private String defaultLengthPattern;

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

	/**
	 * Checks if mobile number matches the numeric pattern.
	 * Used by error adapter for default validation error messages.
	 */
	public boolean isMobileNumberNumeric() {
		if (type != null && OtpRequestType.PASSWORD_RESET.equals(type)) {
			return false;
		}
		String pattern = (defaultNumericPattern != null && !defaultNumericPattern.isEmpty())
				? defaultNumericPattern : "\\d+";
		return !(mobileNumber != null && mobileNumber.matches(pattern));
	}

	/**
	 * Checks if mobile number matches the length pattern.
	 * Used by error adapter for default validation error messages.
	 */
	public boolean isMobileNumberValidLength() {
		if (type != null && OtpRequestType.PASSWORD_RESET.equals(type)) {
			return false;
		}
		String pattern = (defaultLengthPattern != null && !defaultLengthPattern.isEmpty())
				? defaultLengthPattern : "^[0-9]{10,13}$";
		return !(mobileNumber != null && mobileNumber.matches(pattern));
	}
}
