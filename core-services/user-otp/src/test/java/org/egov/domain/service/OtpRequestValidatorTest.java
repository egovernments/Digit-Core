package org.egov.domain.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.MobileNumerValidationCacheRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class OtpRequestValidatorTest {

    @Mock
    private MdmsRepository mdmsRepository;

    @Mock
    private MobileNumerValidationCacheRepository cacheRepository;

    @Mock
    private MultiStateInstanceUtil multiStateInstanceUtil;

    private OtpRequestValidator validator;

    // New flat-schema helpers
    private static MobileValidationConfig indiaConfig() {
        return MobileValidationConfig.builder()
                .countryCode("+91")
                .mobileNumberRegex("^[6-9][0-9]{9}$")
                .isDefault(true)
                .build();
    }

    private static MobileValidationConfig ethiopiaConfig() {
        return MobileValidationConfig.builder()
                .countryCode("+251")
                .mobileNumberRegex("^[79][0-9]{8}$")
                .isDefault(false)
                .build();
    }

    private static MobileValidationConfig ukConfig() {
        return MobileValidationConfig.builder()
                .countryCode("+44")
                .mobileNumberRegex("^7[0-9]{9}$")
                .isDefault(false)
                .build();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new OtpRequestValidator(mdmsRepository, cacheRepository, multiStateInstanceUtil);
        ReflectionTestUtils.setField(validator, "defaultRegex", "^[6-9][0-9]{9}$");
        ReflectionTestUtils.setField(validator, "multiStateInstanceUtil", multiStateInstanceUtil);
        when(multiStateInstanceUtil.getStateLevelTenant(anyString())).thenAnswer(inv -> {
            String t = (String) inv.getArguments()[0];
            return t.contains(".") ? t.split("\\.")[0] : t;
        });
        when(cacheRepository.getValidationRules(anyString(), anyString())).thenReturn(null);
    }

    // -------- mandatory field checks --------

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_when_tenantId_absent() {
        validator.validate(OtpRequest.builder()
                .mobileNumber("9123456789").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_when_mobile_absent() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(indiaConfig()));
        validator.validate(OtpRequest.builder()
                .tenantId("pb").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_should_throw_when_type_absent() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(indiaConfig()));
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789").build());
    }

    // -------- countryCode matching --------

    @Test
    public void test_validates_mobile_matching_countryCode_config() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Arrays.asList(indiaConfig(), ethiopiaConfig()));
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789")
                .countryCode("+91").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_fails_when_countryCode_matches_but_number_invalid() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(ethiopiaConfig()));
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("1234567890")
                .countryCode("+251").type(OtpRequestType.REGISTER).build());
    }

    @Test
    public void test_uses_default_config_when_no_countryCode_sent() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Arrays.asList(indiaConfig(), ethiopiaConfig()));
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789")
                .type(OtpRequestType.REGISTER).build());
    }

    @Test
    public void test_falls_back_to_default_when_countryCode_not_in_mdms() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(indiaConfig()));
        // +44 not in MDMS, should fall back to default (India) regex
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789")
                .countryCode("+44").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_fails_when_no_mdms_config_and_number_fails_default_regex() {
        // MDMS returns empty → application.properties fallback regex is used
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.emptyList());
        // "1234567890" does not match "^[6-9][0-9]{9}$"
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("1234567890")
                .type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_fails_when_countryCode_sent_no_matching_and_no_default_and_number_fails_default() {
        // No matching countryCode +44, no default=true entry → falls back to application.properties regex.
        // "1234567890" starts with 1, fails the default regex ^[6-9][0-9]{9}$ → exception expected.
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(ethiopiaConfig())); // no default=true
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("1234567890")
                .countryCode("+44").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_fails_when_countryCode_sent_no_matching_and_no_default() {
        // No matching countryCode +44, no default=true entry → config not found error.
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(ethiopiaConfig())); // no default=true
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789")
                .countryCode("+44").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_fails_for_invalid_pattern_match() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(ethiopiaConfig()));
        // 123456789 starts with 1, doesn't match ^[79][0-9]{8}$
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("123456789")
                .type(OtpRequestType.REGISTER).build());
    }

    // -------- PASSWORD_RESET skip --------

    @Test
    public void test_password_reset_skips_mobile_validation() {
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.emptyList());
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("0000000000")
                .type(OtpRequestType.PASSWORD_RESET).build());
    }

    // -------- isMobileNumberValid edge cases --------

    @Test
    public void test_isMobileNumberValid_true_when_no_mdms_config_but_mobile_matches_default() {
        OtpRequest req = OtpRequest.builder()
                .tenantId("pb").mobileNumber("9999999999")
                .type(OtpRequestType.REGISTER).build();
        assertTrue(validator.isMobileNumberValid(req));
    }

    @Test
    public void test_isMobileNumberValid_false_when_no_mdms_config_and_mobile_fails_default() {
        OtpRequest req = OtpRequest.builder()
                .tenantId("pb").mobileNumber("1234567890")
                .type(OtpRequestType.REGISTER).build();
        assertFalse(validator.isMobileNumberValid(req));
    }

    // -------- cache hit --------

    @Test
    public void test_uses_cached_config_when_present() {
        when(cacheRepository.getValidationRules(anyString(), anyString()))
                .thenReturn(indiaConfig());
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("9123456789")
                .countryCode("+91").type(OtpRequestType.REGISTER).build());
    }

    @Test(expected = InvalidOtpRequestException.class)
    public void test_number_failing_configured_regex_throws() {
        MobileValidationConfig config = MobileValidationConfig.builder()
                .countryCode("+91").mobileNumberRegex("^[6-9][0-9]{9}$")
                .isDefault(true).build();
        when(mdmsRepository.fetchMobileValidationConfigs(anyString(), any()))
                .thenReturn(Collections.singletonList(config));
        // "0000000000" starts with 0, fails ^[6-9][0-9]{9}$
        validator.validate(OtpRequest.builder()
                .tenantId("pb").mobileNumber("0000000000")
                .type(OtpRequestType.REGISTER).build());
    }
}
