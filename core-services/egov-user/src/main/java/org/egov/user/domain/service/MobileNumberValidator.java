package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.mdmsv2.*;
import org.egov.user.repository.ValidationRulesCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
@Slf4j
public class MobileNumberValidator {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ValidationRulesCacheRepository cacheRepository;

    @Value("${egov.mdms.v2.host}")
    private String mdmsHost;

    @Value("${egov.mdms.v2.search.endpoint}")
    private String mdmsV2SearchEndpoint;

    @Value("${egov.mobile.validation.schema.code}")
    private String validationSchemaCode;

    /**
     * Validates mobile number based on MDMS-v2 configuration (without country code validation)
     *
     * @param mobileNumber Mobile number to validate
     * @param tenantId     Tenant ID
     * @param requestInfo  Request Info
     * @throws CustomException if validation fails
     */
    public void validateMobileNumber(String mobileNumber, String tenantId, RequestInfo requestInfo) {
        validateMobileNumberWithCountryCode(mobileNumber, null, tenantId, requestInfo);
    }

    /**
     * Validates mobile number with country code by finding matching validation config from MDMS array
     *
     * @param mobileNumber Mobile number to validate
     * @param countryCode  Country code prefix (e.g., "+91", "+1", "+44")
     * @param tenantId     Tenant ID
     * @param requestInfo  Request Info
     * @throws CustomException if validation fails
     */
    public String validateMobileNumberWithCountryCode(String mobileNumber, String countryCode, String tenantId, RequestInfo requestInfo) {

        // Skip validation if mobile number is null/blank (mobile is optional)
        if (!StringUtils.hasText(mobileNumber)) {
            return countryCode;
        }
        mobileNumber = mobileNumber.trim();
        if (countryCode != null) {
            countryCode = countryCode.trim();
        }

        log.info("Validating mobile number with country code: {} for tenantId: {}", countryCode, tenantId);

        // Perform validation
        Map<String, String> errorMap = new HashMap<>();

        // Fetch validation config matching the country code from MDMS-v2
        ValidationData validationData = fetchValidationDataByCountryCode(countryCode, tenantId, requestInfo);
        ValidationRules validationRules = validationData != null ? validationData.getRules() : null;
        ValidationAttributes attributes = validationData != null ? validationData.getAttributes() : null;

        // If MDMS rules not found for the country code or default, throw error
        if (validationRules == null) {
            log.warn("No validation rules found in MDMS for country code: {} or as default. Validation skipped.",
                    countryCode);
            errorMap.put("VALIDATION_CONFIG_MISSING", "Validation configuration not found in MDMS");
            throw new CustomException(errorMap);
        }

        // Check length
        if (validationRules.getMinLength() != null && mobileNumber.length() < validationRules.getMinLength()) {
            errorMap.put("INVALID_MOBILE_LENGTH",
                    String.format("Mobile number must be at least %d digits", validationRules.getMinLength()));
        }

        if (validationRules.getMaxLength() != null && mobileNumber.length() > validationRules.getMaxLength()) {
            errorMap.put("INVALID_MOBILE_LENGTH",
                    String.format("Mobile number must not exceed %d digits", validationRules.getMaxLength()));
        }

        // Check pattern
        if (StringUtils.hasText(validationRules.getPattern())) {
            try {
                Pattern pattern = Pattern.compile(validationRules.getPattern());
                if (!pattern.matcher(mobileNumber).matches()) {
                    String errorMessage = StringUtils.hasText(validationRules.getErrorMessage())
                            ? validationRules.getErrorMessage()
                            : "Invalid mobile number format";
                    errorMap.put("INVALID_MOBILE_FORMAT", errorMessage);
                }
            } catch (PatternSyntaxException ex) {
                log.error("Invalid MDMS regex '{}'.", validationRules.getPattern(), ex);
                errorMap.put("INVALID_REGEX", "Invalid validation pattern configured in MDMS");
            }
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }

        log.info("Mobile number and country code validation successful");
        return attributes != null ? attributes.getPrefix() : countryCode;
    }

    /**
     * Returns default validation rules using the pattern from UserServiceConstants
     *
     * @return Default ValidationRules
     */
    private ValidationRules getDefaultValidationRules() {
        ValidationRules defaultRules = new ValidationRules();
        defaultRules.setPattern(UserServiceConstants.PATTERN_MOBILE);
        defaultRules.setMinLength(10);
        defaultRules.setMaxLength(10);
        defaultRules.setErrorMessage("Invalid mobile number format or length");
        return defaultRules;
    }

    /**
     * Fetches validation data for a specific country code from MDMS-v2 array
     * Implements cache-aside pattern: check cache first, fetch from MDMS on miss, then cache
     *
     * @param countryCode  Country code to match (e.g., "+91", "+1")
     * @param tenantId     Tenant ID
     * @param requestInfo  Request Info
     * @return ValidationData matching the country code, or first active config if countryCode is null
     */
    private ValidationData fetchValidationDataByCountryCode(String countryCode, String tenantId, RequestInfo requestInfo) {
        // Extract state level tenant (prefix before first dot)
        String stateLevelTenantId = tenantId;
        if (tenantId != null && tenantId.contains(".")) {
            stateLevelTenantId = tenantId.split("\\.")[0];
        }

        // Check cache first
        ValidationData cachedData = cacheRepository.getValidationData(stateLevelTenantId, countryCode);
        if (cachedData != null) {
            return cachedData;
        }

        // Cache miss - fetch from MDMS-v2
        try {
            String url = mdmsHost + mdmsV2SearchEndpoint;

            MdmsV2SearchCriteria searchCriteria = MdmsV2SearchCriteria.builder()
                    .tenantId(stateLevelTenantId)
                    .schemaCode(validationSchemaCode)
                    .limit(1000)
                    .offset(0)
                    .build();

            MdmsV2SearchRequest searchRequest = MdmsV2SearchRequest.builder()
                    .mdmsCriteria(searchCriteria)
                    .requestInfo(requestInfo)
                    .build();

            log.info("Calling MDMS-v2 at: {} for tenant: {} with country code: {}", url, stateLevelTenantId,
                    countryCode);
            MdmsV2Response response = restTemplate.postForObject(url, searchRequest, MdmsV2Response.class);

            if (response != null && !CollectionUtils.isEmpty(response.getMdms())) {
                ValidationData defaultData = null;

                // Iterate through all MDMS data entries
                for (MdmsV2Data mdmsData : response.getMdms()) {
                    if (mdmsData.getData() != null
                            && Boolean.TRUE.equals(mdmsData.getIsActive())) {

                        ValidationData data = mdmsData.getData();

                        // Keep track of the first active default entry
                        if (defaultData == null && Boolean.TRUE.equals(data.getIsDefault())) {
                            defaultData = data;
                        }

                        // If country code is provided, check for matching prefix
                        if (StringUtils.hasText(countryCode)
                                && data.getAttributes() != null
                                && countryCode.equals(data.getAttributes().getPrefix())) {
                            log.info("Found validation config for country code: {} in tenant: {}", countryCode,
                                    stateLevelTenantId);

                            // Cache the fetched data
                            cacheRepository.cacheValidationData(stateLevelTenantId, countryCode, data);

                            return data;
                        }
                    }
                }

                // If no exact match found but we have a default entry, return it
                if (defaultData != null) {
                    log.info("No match for country code: {}. Returning default MDMS validation config for tenant: {}",
                            countryCode, stateLevelTenantId);
                    // Cache the default data for this country code as well to avoid re-searching
                    cacheRepository.cacheValidationData(stateLevelTenantId, countryCode, defaultData);
                    return defaultData;
                }

                log.warn("No validation configuration or default found for country code: {} in tenant: {}",
                        countryCode, stateLevelTenantId);
            }

            log.warn("No validation rules found in MDMS-v2 for tenant: {}", stateLevelTenantId);
            return null;

        } catch (Exception e) {
            log.error("Error fetching validation data from MDMS-v2 for tenant: {} and country code: {}",
                    stateLevelTenantId, countryCode, e);
            // Don't fail user creation if MDMS is down, just log the error
            return null;
        }
    }

    /**
     * Fetches validation rules from cache or MDMS-v2
     * Implements cache-aside pattern: check cache first, fetch from MDMS on miss, then cache
     *
     * @param tenantId    Tenant ID
     * @param requestInfo Request Info
     * @return ValidationRules or null if not found
     */
    private ValidationRules fetchValidationRules(String tenantId, RequestInfo requestInfo) {
        // Extract state level tenant (prefix before first dot)
        String stateLevelTenantId = tenantId;
        if (tenantId != null && tenantId.contains(".")) {
            stateLevelTenantId = tenantId.split("\\.")[0];
        }

        // Check cache first
        ValidationRules cachedRules = cacheRepository.getValidationRules(stateLevelTenantId);
        if (cachedRules != null) {
            return cachedRules;
        }

        // Cache miss - fetch from MDMS-v2
        try {
            String url = mdmsHost + mdmsV2SearchEndpoint;

            MdmsV2SearchCriteria searchCriteria = MdmsV2SearchCriteria.builder()
                    .tenantId(stateLevelTenantId)
                    .schemaCode(validationSchemaCode)
                    .limit(1000)
                    .offset(0)
                    .build();

            MdmsV2SearchRequest searchRequest = MdmsV2SearchRequest.builder()
                    .mdmsCriteria(searchCriteria)
                    .requestInfo(requestInfo)
                    .build();

            log.info("Calling MDMS-v2 at: {} for tenant: {}", url, stateLevelTenantId);
            MdmsV2Response response = restTemplate.postForObject(url, searchRequest, MdmsV2Response.class);

            if (response != null && !CollectionUtils.isEmpty(response.getMdms())) {
                // Filter for entry with isActive = true
                for (MdmsV2Data mdmsData : response.getMdms()) {
                    if (mdmsData.getData() != null
                        && Boolean.TRUE.equals(mdmsData.getIsActive())) {
                        log.info("Found mobile validation configuration for tenant: {}", stateLevelTenantId);
                        ValidationRules rules = mdmsData.getData().getRules();

                        // Cache the fetched rules
                        cacheRepository.cacheValidationRules(stateLevelTenantId, rules);

                        return rules;
                    }
                }
                log.warn("No active mobile validation configuration found for tenant: {}", stateLevelTenantId);
            }

            log.warn("No validation rules found in MDMS-v2 for tenant: {}", stateLevelTenantId);
            return null;

        } catch (Exception e) {
            log.error("Error fetching validation rules from MDMS-v2 for tenant: {}", stateLevelTenantId, e);
            // Don't fail user creation if MDMS is down, just log the error
            return null;
        }
    }
}
