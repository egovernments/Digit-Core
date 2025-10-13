package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.mdmsv2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MobileNumberValidator {

    @Autowired
    private RestTemplate restTemplate;

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

        // Skip validation if mobile number is null or empty (mobile number is optional)
        if (StringUtils.isEmpty(mobileNumber)) {
            return;
        }

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
        if (!StringUtils.isEmpty(validationRules.getPattern())) {
            Pattern pattern = Pattern.compile(validationRules.getPattern());
            if (!pattern.matcher(mobileNumber).matches()) {
                String errorMessage = !StringUtils.isEmpty(validationRules.getErrorMessage())
                        ? validationRules.getErrorMessage()
                        : "Invalid mobile number format";
                errorMap.put("INVALID_MOBILE_FORMAT", errorMessage);
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
        defaultRules.setErrorMessage("Mobile number must be exactly 10 digits");
        return defaultRules;
    }

    /**
     * Fetches validation rules from MDMS-v2
     *
     * @param tenantId    Tenant ID
     * @param requestInfo Request Info
     * @return ValidationRules or null if not found
     */
    private ValidationRules fetchValidationRules(String tenantId, RequestInfo requestInfo) {
        try {
            String url = mdmsHost + mdmsV2SearchEndpoint;

            // Extract state level tenant (first 2 characters)
            String stateLevelTenantId = tenantId;
            if (tenantId != null && tenantId.contains(".")) {
                stateLevelTenantId = tenantId.split("\\.")[0];
            }

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

            log.info("Calling MDMS-v2 at: {}", url);
            MdmsV2Response response = restTemplate.postForObject(url, searchRequest, MdmsV2Response.class);

            if (response != null && !CollectionUtils.isEmpty(response.getMdms())) {
                // Filter for entry with validationName = "defaultMobileValidation" and isActive = true
                for (MdmsV2Data mdmsData : response.getMdms()) {
                    if (mdmsData.getData() != null
                        && Boolean.TRUE.equals(mdmsData.getIsActive())
                        && "defaultMobileValidation".equals(mdmsData.getData().getValidationName())) {
                        log.info("Found defaultMobileValidation configuration for tenant: {}", tenantId);
                        return mdmsData.getData().getRules();
                    }
                }
                log.warn("No active defaultMobileValidation configuration found for tenant: {}", tenantId);
            }

            log.warn("No validation rules found in MDMS-v2 for tenant: {}", tenantId);
            return null;

        } catch (Exception e) {
            log.error("Error fetching validation rules from MDMS-v2", e);
            // Don't fail user creation if MDMS is down, just log the error
            return null;
        }
    }
}
