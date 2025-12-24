package org.egov.domain.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import org.egov.config.OtpValidationConfig;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.MobileValidationRules;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.ValidationRulesCacheRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OtpRequestValidatorTest {

    @Mock
    private MdmsRepository mdmsRepository;

    @Mock
    private OtpValidationConfig otpValidationConfig;

    @Mock
    private ValidationRulesCacheRepository cacheRepository;

    private OtpRequestValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(otpValidationConfig.getDefaultNumericPattern()).thenReturn("\\d+");
        when(otpValidationConfig.getDefaultLengthPattern()).thenReturn("^[0-9]{10,13}$");
        validator = new OtpRequestValidator(mdmsRepository, otpValidationConfig, cacheRepository);
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_exception_when_tenant_id_is_not_present() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId(null)
                .mobileNumber("1234567890")
                .type(OtpRequestType.REGISTER)
                .build();

        validator.validate(otpRequest);
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_exception_when_mobile_number_is_not_present() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber(null)
                .type(OtpRequestType.REGISTER)
                .build();

        validator.validate(otpRequest);
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_exception_when_type_is_not_present() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("1234567890")
                .type(null)
                .build();

        validator.validate(otpRequest);
    }

    @Test
    public void test_should_not_throw_exception_for_valid_register_request() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("1234567890")
                .type(OtpRequestType.REGISTER)
                .build();

        validator.validate(otpRequest);
    }

    @Test
    public void test_should_not_throw_exception_for_valid_login_request() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("1234567890")
                .type(OtpRequestType.LOGIN)
                .build();

        validator.validate(otpRequest);
    }

    @Test
    public void test_should_validate_with_mdms_config() {
        MobileValidationRules rules = MobileValidationRules.builder()
                .pattern("^[79][0-9]{8}$")
                .minLength(9)
                .maxLength(9)
                .errorMessage("Invalid mobile number")
                .build();
        MobileValidationConfig config = MobileValidationConfig.builder()
                .rules(rules)
                .fieldType("mobile")
                .build();

        when(mdmsRepository.fetchMobileValidationConfig(anyString(), any())).thenReturn(config);

        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("712345678")
                .type(OtpRequestType.REGISTER)
                .build();

        validator.validate(otpRequest);
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_fail_mdms_validation_for_invalid_pattern() {
        MobileValidationRules rules = MobileValidationRules.builder()
                .pattern("^[79][0-9]{8}$")
                .minLength(9)
                .maxLength(9)
                .errorMessage("Invalid mobile number")
                .build();
        MobileValidationConfig config = MobileValidationConfig.builder()
                .rules(rules)
                .fieldType("mobile")
                .build();

        when(mdmsRepository.fetchMobileValidationConfig(anyString(), any())).thenReturn(config);

        // Mobile starts with 1, which doesn't match pattern ^[79]
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("123456789")
                .type(OtpRequestType.REGISTER)
                .build();

        validator.validate(otpRequest);
    }

    @Test
    public void test_should_skip_validation_for_password_reset() {
        MobileValidationRules rules = MobileValidationRules.builder()
                .pattern("^[79][0-9]{8}$")
                .minLength(9)
                .maxLength(9)
                .errorMessage("Invalid mobile number")
                .build();
        MobileValidationConfig config = MobileValidationConfig.builder()
                .rules(rules)
                .fieldType("mobile")
                .build();

        when(mdmsRepository.fetchMobileValidationConfig(anyString(), any())).thenReturn(config);

        // Mobile number doesn't match rules but should pass for PASSWORD_RESET
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("1234567890")
                .type(OtpRequestType.PASSWORD_RESET)
                .build();

        validator.validate(otpRequest);
    }

    @Test
    public void test_isMobileNumberValid_returns_true_for_valid_number() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("1234567890")
                .type(OtpRequestType.REGISTER)
                .defaultNumericPattern("\\d+")
                .defaultLengthPattern("^[0-9]{10,13}$")
                .build();

        assertTrue(validator.isMobileNumberValid(otpRequest));
    }

    @Test
    public void test_isMobileNumberValid_returns_false_for_non_numeric() {
        final OtpRequest otpRequest = OtpRequest.builder()
                .tenantId("tenantId")
                .mobileNumber("abc1234567")
                .type(OtpRequestType.REGISTER)
                .defaultNumericPattern("\\d+")
                .defaultLengthPattern("^[0-9]{10,13}$")
                .build();

        assertFalse(validator.isMobileNumberValid(otpRequest));
    }
}
