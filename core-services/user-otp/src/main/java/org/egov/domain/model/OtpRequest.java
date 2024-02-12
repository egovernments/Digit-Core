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

    public void validate() {
        if(isTenantIdAbsent()
				|| isMobileNumberAbsent()
				|| isInvalidType()
				|| isMobileNumberNumeric()
				|| isMobileNumberValidLength()) {
            throw new InvalidOtpRequestException(this);
        }
    }

	public boolean isMobileNumberNumeric() {
		// TODO Auto-generated method stub
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString())))
		//return !StringUtils.isNumeric(mobileNumber);
		return !(mobileNumber != null && mobileNumber.matches("\\d+"));
		return false;
	}

	public boolean isMobileNumberValidLength() {
		// TODO Auto-generated method stub
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString())))
		return !(mobileNumber != null && mobileNumber.matches("^[0-9]{10,13}$"));
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
}
