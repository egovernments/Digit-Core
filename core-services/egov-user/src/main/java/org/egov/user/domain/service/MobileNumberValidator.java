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
     * Validates mobile number based on MDMS-v2 configuration
     *
     * @param mobileNumber Mobile number to validate
     * @param tenantId     Tenant ID
     * @param requestInfo  Request Info
     * @throws CustomException if validation fails
     */
    public void validateMobileNumber(String mobileNumber, String tenantId, RequestInfo requestInfo) {

        // Skip validation if mobile number is null/blank (mobile is optional)
        if (!StringUtils.hasText(mobileNumber)) {
            return;
        }
        mobileNumber = mobileNumber.trim();

        log.info("Validating mobile number for tenantId: {}", tenantId);

        // Fetch validation config from MDMS-v2
        ValidationRules validationRules = fetchValidationRules(tenantId, requestInfo);

        // If MDMS rules not found, apply default validation
        if (validationRules == null) {
            log.info("MDMS validation rules not found. Applying default validation pattern: {}",
                     UserServiceConstants.PATTERN_MOBILE);
            validationRules = getDefaultValidationRules();
        }

        if (!Boolean.TRUE.equals(validationRules.getIsActive())) {
            log.info("Mobile validation is not active. Skipping validation.");
            return;
        }

        // Perform validation
        Map<String, String> errorMap = new HashMap<>();

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
                log.warn("Invalid MDMS regex '{}'. Falling back to default pattern.", validationRules.getPattern(), ex);
                Pattern fallback = Pattern.compile(UserServiceConstants.PATTERN_MOBILE);
                if (!fallback.matcher(mobileNumber).matches()) {
                    String errorMessage = StringUtils.hasText(validationRules.getErrorMessage())
                            ? validationRules.getErrorMessage()
                            : "Invalid mobile number format";
                    errorMap.put("INVALID_MOBILE_FORMAT", errorMessage);
                }
            }
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }

        log.info("Mobile number validation successful");
    }

    /**
     * Returns default validation rules using the pattern from UserServiceConstants
     *
     * @return Default ValidationRules
     */
    private ValidationRules getDefaultValidationRules() {
        ValidationRules defaultRules = new ValidationRules();
        defaultRules.setPattern(UserServiceConstants.PATTERN_MOBILE);
        defaultRules.setIsActive(true);
        defaultRules.setMinLength(10);
        defaultRules.setMaxLength(10);
        defaultRules.setErrorMessage("Invalid mobile number format or length");
        return defaultRules;
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
                // Filter for entry with validationName = "defaultMobileValidation" and isActive = true
                for (MdmsV2Data mdmsData : response.getMdms()) {
                    if (mdmsData.getData() != null
                        && Boolean.TRUE.equals(mdmsData.getIsActive())
                        && UserServiceConstants.DEFAULT_MOBILE_VALIDATION_NAME.equals(mdmsData.getData().getValidationName())) {
                        log.info("Found defaultMobileValidation configuration for tenant: {}", stateLevelTenantId);
                        ValidationRules rules = mdmsData.getData().getRules();

                        // Cache the fetched rules
                        cacheRepository.cacheValidationRules(stateLevelTenantId, rules);

                        return rules;
                    }
                }
                log.warn("No active defaultMobileValidation configuration found for tenant: {}", stateLevelTenantId);
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
