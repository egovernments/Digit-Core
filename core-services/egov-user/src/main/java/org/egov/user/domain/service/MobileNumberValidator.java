package org.egov.user.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.mdmsv2.MdmsV2Data;
import org.egov.user.domain.model.mdmsv2.MdmsV2Response;
import org.egov.user.domain.model.mdmsv2.MdmsV2SearchCriteria;
import org.egov.user.domain.model.mdmsv2.MdmsV2SearchRequest;
import org.egov.user.repository.MobileNumerValidationCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

@Service
@Slf4j
public class MobileNumberValidator {

    private static final String FIELD_COUNTRY_CODE = "countryCode";
    private static final String FIELD_REGEX = "mobileNumberRegex";
    private static final String FIELD_DEFAULT = "default";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MobileNumerValidationCacheRepository cacheRepository;

    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Value("${egov.mdms.v2.host}")
    private String mdmsHost;

    @Value("${egov.mdms.v2.search.endpoint}")
    private String mdmsV2SearchEndpoint;

    @Value("${egov.mobile.validation.schema.code}")
    private String validationSchemaCode;

    @Value("${egov.mobile.validation.default.country.code:+91}")
    private String defaultCountryCode;

    @Value("${egov.mobile.validation.default.regex:^[6-9][0-9]{9}$}")
    private String defaultMobileRegex;

    /**
     * Validates both the primary and alternate mobile numbers on a User, then sets
     * the resolved country code back on the user object.
     * Callers (services) use this instead of calling the lower-level methods directly.
     */
    public void validateAndSetMobileNumbers(org.egov.user.domain.model.User user, RequestInfo requestInfo) {
        if (user == null) return;
        String resolvedCountryCode = validateMobileNumberWithCountryCode(
                user.getMobileNumber(), user.getCountryCode(), user.getTenantId(), requestInfo);
        user.setCountryCode(resolvedCountryCode);
        validateMobileNumberWithCountryCode(
                user.getAlternateMobileNumber(), resolvedCountryCode, user.getTenantId(), requestInfo);
    }

    public void validateMobileNumber(String mobileNumber, String tenantId, RequestInfo requestInfo) {
        validateMobileNumberWithCountryCode(mobileNumber, null, tenantId, requestInfo);
    }

    public String validateMobileNumberWithCountryCode(String mobileNumber, String countryCode,
                                                      String tenantId, RequestInfo requestInfo) {
        if (!StringUtils.hasText(mobileNumber)) {
            // Mobile number is optional (e.g. alternateMobileNumber). When absent there is
            // nothing to validate, but we still return the incoming countryCode so the caller
            // can store it unchanged — skipping this field must not wipe out the code that was
            // already resolved for the primary mobile number.
            return countryCode;
        }
        mobileNumber = mobileNumber.trim();
        if (countryCode != null) {
            countryCode = countryCode.trim();
            if (countryCode.isEmpty()) {
                countryCode = null;  // treat empty string as absent — will fall back to default
            }
        }

        log.info("Validating mobile number for countryCode: {} tenantId: {}", countryCode, tenantId);

        String stateTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);

        // 1. Try cache
        String regex = cacheRepository.getMobileRegex(stateTenantId, countryCode);

        if (regex == null) {
            // 2. Try incoming tenantId in MDMS
            regex = fetchRegexFromMdms(countryCode, tenantId, requestInfo);

            // 3. Fallback to state tenant if incoming returned nothing
            if (regex == null && !tenantId.equals(stateTenantId)) {
                log.info("No MDMS config for tenantId: {}, retrying with stateTenant: {}", tenantId, stateTenantId);
                regex = fetchRegexFromMdms(countryCode, stateTenantId, requestInfo);
            }

            if (regex != null) {
                cacheRepository.cacheMobileRegex(stateTenantId, countryCode, regex);
            }
        }

        // 4. Fallback to application.properties default
        if (regex == null) {
            log.warn("No MDMS validation config found for tenantId: {} countryCode: {}. Using application.properties default.",
                    tenantId, countryCode);
            regex = defaultMobileRegex;
        }

        applyRegexValidation(mobileNumber, regex);
        log.info("Mobile validation successful for countryCode: {}", countryCode);
        return countryCode != null ? countryCode : defaultCountryCode;
    }

    private void applyRegexValidation(String mobileNumber, String regex) {
        try {
            if (!mobileNumber.matches(regex)) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("INVALID_MOBILE_NUMBER",
                        "Mobile number must match the configured pattern: " + regex);
                throw new CustomException(errorMap);
            }
        } catch (PatternSyntaxException ex) {
            log.error("Invalid regex pattern in MDMS: '{}'", regex, ex);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("INVALID_VALIDATION_PATTERN", "Invalid regex pattern configured in MDMS for mobile number validation.");
            throw new CustomException(errorMap);
        }
    }

    private String fetchRegexFromMdms(String countryCode, String tenantId, RequestInfo requestInfo) {
        try {
            String url = mdmsHost + mdmsV2SearchEndpoint;
            MdmsV2SearchRequest searchRequest = MdmsV2SearchRequest.builder()
                    .mdmsCriteria(MdmsV2SearchCriteria.builder()
                            .tenantId(tenantId)
                            .schemaCode(validationSchemaCode)
                            .limit(1000)
                            .offset(0)
                            .build())
                    .requestInfo(requestInfo)
                    .build();

            log.debug("Calling MDMS-v2 for tenantId: {} schemaCode: {}", tenantId, validationSchemaCode);
            MdmsV2Response response = restTemplate.postForObject(url, searchRequest, MdmsV2Response.class);

            if (response == null || CollectionUtils.isEmpty(response.getMdms())) {
                return null;
            }

            return selectRegex(response.getMdms(), countryCode);

        } catch (Exception e) {
            log.error("Error fetching validation config from MDMS-v2 for tenantId: {} countryCode: {}", tenantId, countryCode, e);
            return null;
        }
    }

    private String selectRegex(List<MdmsV2Data> mdmsEntries, String countryCode) {
        String defaultRegex = null;
        for (MdmsV2Data entry : mdmsEntries) {
            if (entry.getData() == null || Boolean.FALSE.equals(entry.getIsActive())) {
                continue;
            }
            JsonNode data = entry.getData();

            String entryRegex = data.has(FIELD_REGEX) ? data.get(FIELD_REGEX).asText(null) : null;
            if (!StringUtils.hasText(entryRegex)) {
                continue;
            }

            boolean isDefault = data.has(FIELD_DEFAULT) && data.get(FIELD_DEFAULT).asBoolean(false);
            String entryCountryCode = data.has(FIELD_COUNTRY_CODE) ? data.get(FIELD_COUNTRY_CODE).asText(null) : null;

            if (isDefault && defaultRegex == null) {
                defaultRegex = entryRegex;
            }

            if (StringUtils.hasText(countryCode) && countryCode.equals(entryCountryCode)) {
                log.info("Found MDMS MobileNumberValidation entry for countryCode: {}", countryCode);
                return entryRegex;
            }
        }

        if (defaultRegex != null) {
            log.info("No MDMS entry for countryCode: {}, using default entry regex.", countryCode);
        }
        return defaultRegex;
    }

}
