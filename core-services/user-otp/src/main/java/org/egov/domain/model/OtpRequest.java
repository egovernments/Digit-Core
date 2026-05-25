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
    private String countryCode;

    @Setter
    private MobileValidationConfig mdmsValidationConfig;
    @Setter
    private String mdmsValidationErrorMessage;

    public void validate() {
        if (isTenantIdAbsent()
                || !isUserNameOrMobileNumberPresent()
                || isInvalidType()) {
            throw new InvalidOtpRequestException(this);
        }
    }

    public boolean isUserNameOrMobileNumberPresent() {
        return !isMobileNumberAbsent() || !isUserNameAbsent();
    }

    public boolean hasMdmsValidationError() {
        return !isEmpty(mdmsValidationErrorMessage);
    }

    public boolean isRegistrationRequestType() {
        return OtpRequestType.REGISTER.equals(getType());
    }

    public boolean isLoginRequestType() {
        return OtpRequestType.LOGIN.equals(getType());
    }

    public boolean isPasswordResetRequestType() {
        return OtpRequestType.PASSWORD_RESET.equals(getType());
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
