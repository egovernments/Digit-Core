package org.egov.web.error;

import org.egov.domain.model.OtpRequest;
import org.egov.web.contract.Error;
import org.egov.web.contract.ErrorField;
import org.egov.web.contract.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class OtpRequestErrorAdapter implements ErrorAdapter<OtpRequest> {

    private static final String INVALID_OTP_REQUEST = "Invalid OTP request";

    private static final String TENANT_MANDATORY_CODE = "OTP.TENANT_ID_MANDATORY";
    private static final String TENANT_MANDATORY_MESSAGE = "Tenant field is mandatory";
    private static final String TENANT_FIELD = "otp.tenantId";

    private static final String MOBILE_MANDATORY_CODE = "OTP.MOBILE_NUMBER_MANDATORY";
    private static final String MOBILE_MANDATORY_MESSAGE = "Mobile number or username field is mandatory";
    private static final String MOBILE_FIELD = "otp.mobileNumber";

    private static final String TYPE_INVALID_CODE = "OTP.REQUEST_TYPE_MANDATORY";
    private static final String TYPE_INVALID_MESSAGE = "Request type (register, passwordreset, login) is mandatory";
    private static final String TYPE_FIELD = "otp.type";

    private static final String MOBILE_VALIDATION_FAILED_CODE = "OTP.MOBILE_NUMBER_VALIDATION_FAILED";
    private static final String MOBILE_INVALID_FIELD = "otp.mobileNumber";

    @Override
    public ErrorResponse adapt(OtpRequest model) {
        final Error error = getError(model);
        return new ErrorResponse(null, error);
    }

    private Error getError(OtpRequest model) {
        List<ErrorField> errorFields = getErrorFields(model);
        return Error.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(INVALID_OTP_REQUEST)
                .fields(errorFields)
                .build();
    }

    private List<ErrorField> getErrorFields(OtpRequest model) {
        List<ErrorField> errorFields = new ArrayList<>();
        addTenantIdValidationErrors(model, errorFields);
        addMobileOrUserNameMandatoryErrors(model, errorFields);
        addRequestTypeValidationErrors(model, errorFields);
        addMdmsValidationErrors(model, errorFields);
        return errorFields;
    }

    private void addTenantIdValidationErrors(OtpRequest model, List<ErrorField> errorFields) {
        if (!model.isTenantIdAbsent()) return;
        errorFields.add(ErrorField.builder()
                .code(TENANT_MANDATORY_CODE)
                .message(TENANT_MANDATORY_MESSAGE)
                .field(TENANT_FIELD)
                .build());
    }

    private void addMobileOrUserNameMandatoryErrors(OtpRequest model, List<ErrorField> errorFields) {
        if (model.isUserNameOrMobileNumberPresent()) return;
        errorFields.add(ErrorField.builder()
                .code(MOBILE_MANDATORY_CODE)
                .message(MOBILE_MANDATORY_MESSAGE)
                .field(MOBILE_FIELD)
                .build());
    }

    private void addRequestTypeValidationErrors(OtpRequest model, List<ErrorField> errorFields) {
        if (!model.isInvalidType()) return;
        errorFields.add(ErrorField.builder()
                .code(TYPE_INVALID_CODE)
                .message(TYPE_INVALID_MESSAGE)
                .field(TYPE_FIELD)
                .build());
    }

    private void addMdmsValidationErrors(OtpRequest model, List<ErrorField> errorFields) {
        if (!model.hasMdmsValidationError()) return;
        errorFields.add(ErrorField.builder()
                .code(MOBILE_VALIDATION_FAILED_CODE)
                .message(model.getMdmsValidationErrorMessage())
                .field(MOBILE_INVALID_FIELD)
                .build());
    }
}
