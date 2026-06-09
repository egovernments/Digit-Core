package org.egov.user.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.mdmsv2.MdmsV2Data;
import org.egov.user.domain.model.mdmsv2.MdmsV2Response;
import org.egov.user.repository.MobileNumerValidationCacheRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MobileNumberValidatorTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MobileNumerValidationCacheRepository cacheRepository;

    @Mock
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @InjectMocks
    private MobileNumberValidator validator;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RequestInfo requestInfo = RequestInfo.builder().build();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(validator, "mdmsHost", "http://mdms-host");
        ReflectionTestUtils.setField(validator, "mdmsV2SearchEndpoint", "/mdms-v2/v2/_search");
        ReflectionTestUtils.setField(validator, "validationSchemaCode", "common-masters.MobileNumberValidation");
        ReflectionTestUtils.setField(validator, "defaultCountryCode", "+91");
        ReflectionTestUtils.setField(validator, "defaultMobileRegex", "^[6-9][0-9]{9}$");

        when(multiStateInstanceUtil.getStateLevelTenant(anyString())).thenAnswer(inv -> {
            String t = (String) inv.getArguments()[0];
            return t.contains(".") ? t.split("\\.")[0] : t;
        });
        when(cacheRepository.getMobileRegex(anyString(), any())).thenReturn(null);
    }

    // -------- skip validation for blank mobile --------

    @Test
    public void test_skip_when_mobile_blank() {
        validator.validateMobileNumber("", "pb", requestInfo);
        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void test_skip_when_mobile_null() {
        validator.validateMobileNumber(null, "pb", requestInfo);
        verifyZeroInteractions(restTemplate);
    }

    // -------- valid mobile with matching MDMS entry --------

    @Test
    public void test_valid_mobile_with_india_config() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        validator.validateMobileNumberWithCountryCode("9123456789", "+91", "pb", requestInfo);
    }

    @Test
    public void test_valid_mobile_no_countryCode_uses_default_entry() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        validator.validateMobileNumberWithCountryCode("8888888888", null, "pb", requestInfo);
    }

    // -------- invalid mobile --------

    @Test(expected = CustomException.class)
    public void test_throws_for_mobile_not_matching_regex() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        validator.validateMobileNumberWithCountryCode("1234567890", "+91", "pb", requestInfo);
    }

    // -------- state-level tenant fallback --------

    @Test
    public void test_falls_back_to_state_tenant_when_incoming_tenant_returns_empty() {
        // First MDMS call (incoming tenant) returns empty; second call (state tenant) returns valid config
        when(restTemplate.postForObject(anyString(), any(), eq(MdmsV2Response.class)))
                .thenReturn(emptyMdmsResponse())
                .thenReturn(mdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true));

        when(multiStateInstanceUtil.getStateLevelTenant("pb.amritsar")).thenReturn("pb");

        validator.validateMobileNumberWithCountryCode("9876543210", "+91", "pb.amritsar", requestInfo);
    }

    // -------- application.properties fallback --------

    @Test
    public void test_uses_default_regex_when_mdms_returns_nothing() {
        when(restTemplate.postForObject(anyString(), any(), eq(MdmsV2Response.class)))
                .thenReturn(emptyMdmsResponse());
        // "9876543210" matches the default regex ^[6-9][0-9]{9}$
        validator.validateMobileNumberWithCountryCode("9876543210", null, "pb", requestInfo);
    }

    @Test(expected = CustomException.class)
    public void test_throws_when_mdms_returns_nothing_and_number_fails_default_regex() {
        when(restTemplate.postForObject(anyString(), any(), eq(MdmsV2Response.class)))
                .thenReturn(emptyMdmsResponse());
        validator.validateMobileNumberWithCountryCode("0000000000", null, "pb", requestInfo);
    }

    // -------- cache hit --------

    @Test
    public void test_uses_cached_regex_without_calling_mdms() {
        when(cacheRepository.getMobileRegex("pb", "+91")).thenReturn("^[6-9][0-9]{9}$");
        validator.validateMobileNumberWithCountryCode("9123456789", "+91", "pb", requestInfo);
        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void test_caches_regex_after_mdms_fetch() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        validator.validateMobileNumberWithCountryCode("9123456789", "+91", "pb", requestInfo);
        verify(cacheRepository).cacheMobileRegex(eq("pb"), eq("+91"), eq("^[6-9][0-9]{9}$"));
    }

    // -------- countryCode resolution --------

    @Test
    public void test_returns_provided_countryCode_after_validation() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        String result = validator.validateMobileNumberWithCountryCode("9123456789", "+91", "pb", requestInfo);
        assertEquals("+91", result);
    }

    @Test
    public void test_returns_default_countryCode_when_null_provided() {
        stubMdmsResponse("pb", "+91", "^[6-9][0-9]{9}$", true, true);
        String result = validator.validateMobileNumberWithCountryCode("9123456789", null, "pb", requestInfo);
        assertEquals("+91", result);
    }

    // -------- inactive entry skipped --------

    @Test
    public void test_inactive_mdms_entry_skipped_falls_to_default_regex() {
        // Returns one inactive entry → selectRegex returns null → fallback regex used
        when(restTemplate.postForObject(anyString(), any(), eq(MdmsV2Response.class)))
                .thenReturn(mdmsResponseInactive("pb", "+91", "^[6-9][0-9]{9}$"));
        // "9123456789" passes default regex, so no exception
        validator.validateMobileNumberWithCountryCode("9123456789", "+91", "pb", requestInfo);
    }

    // -------- helpers --------

    private void stubMdmsResponse(String tenantId, String countryCode, String regex,
                                  boolean isDefault, boolean active) {
        when(restTemplate.postForObject(anyString(), any(), eq(MdmsV2Response.class)))
                .thenReturn(mdmsResponse(tenantId, countryCode, regex, isDefault, active));
    }

    private MdmsV2Response mdmsResponse(String tenantId, String countryCode, String regex,
                                        boolean isDefault, boolean active) {
        ObjectNode data = mapper.createObjectNode();
        data.put("countryCode", countryCode);
        data.put("mobileNumberRegex", regex);
        data.put("default", isDefault);
        data.put("active", active);

        MdmsV2Data entry = MdmsV2Data.builder()
                .tenantId(tenantId)
                .schemaCode("common-masters.MobileNumberValidation")
                .data(data)
                .isActive(true)
                .build();

        MdmsV2Response response = new MdmsV2Response();
        response.setMdms(Collections.singletonList(entry));
        return response;
    }

    private MdmsV2Response mdmsResponseInactive(String tenantId, String countryCode, String regex) {
        ObjectNode data = mapper.createObjectNode();
        data.put("countryCode", countryCode);
        data.put("mobileNumberRegex", regex);
        data.put("default", true);
        data.put("active", false); // inactive

        MdmsV2Data entry = MdmsV2Data.builder()
                .tenantId(tenantId)
                .schemaCode("common-masters.MobileNumberValidation")
                .data(data)
                .isActive(true)
                .build();

        MdmsV2Response response = new MdmsV2Response();
        response.setMdms(Collections.singletonList(entry));
        return response;
    }

    private MdmsV2Response emptyMdmsResponse() {
        MdmsV2Response response = new MdmsV2Response();
        response.setMdms(Collections.emptyList());
        return response;
    }
}
