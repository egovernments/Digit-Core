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

    public void validate() {
        if(isTenantIdAbsent()
				|| !isUserNameOrMobileNumberValid()
		        || isInvalidType()) {
            throw new InvalidOtpRequestException(this);
        }
    }

	public boolean isUserNameOrMobileNumberValid(){
		if(!isMobileNumberAbsent()){
			if(isMobileNumberNumeric()
					|| isMobileNumberValidLength()){
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

	public boolean isUserNameAbsent() {
		return isEmpty(userName);
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
