package org.egov.domain.model;

import lombok.*;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.web.contract.RequestInfo;

import static org.springframework.util.ObjectUtils.isEmpty;

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
	private String userName;

	@Setter
	private MobileValidationConfig mdmsValidationConfig;

	@Setter
	private String defaultNumericPattern;

	@Setter
	private String defaultLengthPattern;

    public void validate() {
        if(isTenantIdAbsent()
				|| !isUserNameOrMobileNumberValid()
		        || isInvalidType()) {
            throw new InvalidOtpRequestException(this);
        }
    }

	public boolean isUserNameOrMobileNumberValid(){
		if(!isMobileNumberAbsent()){
			// First try MDMS validation if config is available
			if(mdmsValidationConfig != null && mdmsValidationConfig.getRules() != null
					&& Boolean.TRUE.equals(mdmsValidationConfig.getRules().getIsActive())) {
				return validateWithMdmsConfig();
			}
			// Fallback to default validation
			if(isMobileNumberNumeric() || isMobileNumberValidLength()){
				return false;
			}
		}
		else {
			if(isUserNameAbsent()){
				return false;
			}
		}
		return true;
	}

	private boolean validateWithMdmsConfig() {
		MobileValidationRules rules = mdmsValidationConfig.getRules();

		// Skip validation for PASSWORD_RESET type
		if(type != null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString())) {
			return true;
		}

		// Validate length
		if(rules.getMinLength() != null && mobileNumber.length() < rules.getMinLength()) {
			return false;
		}
		if(rules.getMaxLength() != null && mobileNumber.length() > rules.getMaxLength()) {
			return false;
		}

		// Validate pattern
		if(rules.getPattern() != null && !rules.getPattern().isEmpty()) {
			if(!mobileNumber.matches(rules.getPattern())) {
				return false;
			}
		}

		// Validate allowed starting digits
		if(rules.getAllowedStartingDigits() != null && !rules.getAllowedStartingDigits().isEmpty()) {
			String firstDigit = mobileNumber.substring(0, 1);
			if(!rules.getAllowedStartingDigits().contains(firstDigit)) {
				return false;
			}
		}

		return true;
	}

	public boolean isMobileNumberNumeric() {
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString()))) {
			String pattern = (defaultNumericPattern != null && !defaultNumericPattern.isEmpty())
					? defaultNumericPattern : "\\d+";
			return !(mobileNumber != null && mobileNumber.matches(pattern));
		}
		return false;
	}

	public boolean isMobileNumberValidLength() {
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString()))) {
			String pattern = (defaultLengthPattern != null && !defaultLengthPattern.isEmpty())
					? defaultLengthPattern : "^[0-9]{10,13}$";
			return !(mobileNumber != null && mobileNumber.matches(pattern));
		}
		return false;
	}
    
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

	public boolean isUserNameAbsent() {
		return isEmpty(userName);
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
