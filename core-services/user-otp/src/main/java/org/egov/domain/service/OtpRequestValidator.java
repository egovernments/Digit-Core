package org.egov.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.config.OtpValidationConfig;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.MobileValidationRules;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.ValidationRulesCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Validator for OTP requests. Handles both MDMS-based validation
 * and fallback to default validation patterns.
 * Uses Redis cache for MDMS validation config to avoid repeated API calls.
 */
@Component
@Slf4j
public class OtpRequestValidator {

    private final MdmsRepository mdmsRepository;
    private final OtpValidationConfig otpValidationConfig;
    private final ValidationRulesCacheRepository cacheRepository;

    @Autowired
    public OtpRequestValidator(MdmsRepository mdmsRepository, OtpValidationConfig otpValidationConfig,
                               ValidationRulesCacheRepository cacheRepository) {
        this.mdmsRepository = mdmsRepository;
        this.otpValidationConfig = otpValidationConfig;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Validates the OTP request. Fetches MDMS config and performs validation.
     * Throws InvalidOtpRequestException if validation fails.
     */
    public void validate(OtpRequest otpRequest) {
        // Fetch and set MDMS validation config
        fetchAndSetMdmsValidationConfig(otpRequest);

        // Set default validation patterns
        setDefaultValidationPatterns(otpRequest);

        // Perform validation
        if (isTenantIdAbsent(otpRequest)
                || isMobileNumberAbsent(otpRequest)
                || !isMobileNumberValid(otpRequest)
                || isInvalidType(otpRequest)) {
            throw new InvalidOtpRequestException(otpRequest);
        }
    }

    public boolean isTenantIdAbsent(OtpRequest otpRequest) {
        return isEmpty(otpRequest.getTenantId());
    }

    public boolean isMobileNumberAbsent(OtpRequest otpRequest) {
        return isEmpty(otpRequest.getMobileNumber());
    }

    public boolean isInvalidType(OtpRequest otpRequest) {
        return isEmpty(otpRequest.getType());
    }

    /**
     * Validates the mobile number using MDMS config if available,
     * otherwise falls back to default validation patterns.
     */
    public boolean isMobileNumberValid(OtpRequest otpRequest) {
        if (isMobileNumberAbsent(otpRequest)) {
            return true;
        }

        MobileValidationConfig mdmsConfig = otpRequest.getMdmsValidationConfig();

        // Use MDMS validation if config is available
        if (mdmsConfig != null && mdmsConfig.getRules() != null) {
            return validateWithMdmsConfig(otpRequest, mdmsConfig.getRules());
        }

        // Fallback to default validation
        return validateWithDefaultPatterns(otpRequest);
    }

    /**
     * Validates mobile number using MDMS configuration rules.
     *
     * Note: Validation is skipped for PASSWORD_RESET type to allow existing users
     * to reset their password even if their mobile number doesn't match new validation rules.
     * This prevents users from being locked out when validation rules change.
     */
    private boolean validateWithMdmsConfig(OtpRequest otpRequest, MobileValidationRules rules) {
        String mobileNumber = otpRequest.getMobileNumber();

        // Skip validation for PASSWORD_RESET type to allow existing users to reset password
        // even if their mobile number doesn't conform to new validation rules
        if (otpRequest.getType() != null
                && OtpRequestType.PASSWORD_RESET.equals(otpRequest.getType())) {
            return true;
        }

        // Validate minimum length
        if (rules.getMinLength() != null && mobileNumber.length() < rules.getMinLength()) {
            otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
            return false;
        }

        // Validate maximum length
        if (rules.getMaxLength() != null && mobileNumber.length() > rules.getMaxLength()) {
            otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
            return false;
        }

        // Validate pattern (includes starting character validation via regex)
        if (rules.getPattern() != null && !rules.getPattern().isEmpty()) {
            if (!mobileNumber.matches(rules.getPattern())) {
                otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
                return false;
            }
        }

        // Note: allowedStartingCharacters field is not used for validation
        // as it's redundant with regex pattern. Kept in model for MDMS compatibility.

        return true;
    }

    /**
     * Validates mobile number using default patterns when MDMS config is not available.
     */
    private boolean validateWithDefaultPatterns(OtpRequest otpRequest) {
        // Skip validation for PASSWORD_RESET type
        if (otpRequest.getType() != null
                && OtpRequestType.PASSWORD_RESET.equals(otpRequest.getType())) {
            return true;
        }

        String mobileNumber = otpRequest.getMobileNumber();

        // Check numeric pattern
        String numericPattern = otpRequest.getDefaultNumericPattern();
        if (numericPattern == null || numericPattern.isEmpty()) {
            numericPattern = "\\d+";
        }
        if (mobileNumber != null && !mobileNumber.matches(numericPattern)) {
            return false;
        }

        // Check length pattern
        String lengthPattern = otpRequest.getDefaultLengthPattern();
        if (lengthPattern == null || lengthPattern.isEmpty()) {
            lengthPattern = "^[0-9]{10,13}$";
        }
        if (mobileNumber != null && !mobileNumber.matches(lengthPattern)) {
            return false;
        }

        return true;
    }

    /**
     * Fetches validation config from cache first, falls back to MDMS API if not cached.
     * Caches the result for subsequent requests (cache-aside pattern).
     */
    private void fetchAndSetMdmsValidationConfig(OtpRequest otpRequest) {
        String tenantId = otpRequest.getTenantId();

        // Skip if tenantId is not provided
        if (tenantId == null || tenantId.isEmpty()) {
            log.debug("TenantId is null or empty, skipping MDMS config fetch");
            return;
        }

        // Extract state-level tenant ID for caching (same as egov-user)
        String stateLevelTenantId = tenantId;
        if (tenantId.contains(".")) {
            stateLevelTenantId = tenantId.split("\\.")[0];
        }

        try {
            // Check cache first
            MobileValidationConfig cachedConfig = cacheRepository.getValidationRules(stateLevelTenantId);
            if (cachedConfig != null) {
                log.debug("Using cached validation config for tenantId: {}", stateLevelTenantId);
                otpRequest.setMdmsValidationConfig(cachedConfig);
                return;
            }

            // Cache miss - fetch from MDMS
            MobileValidationConfig config = mdmsRepository.fetchMobileValidationConfig(
                    tenantId, otpRequest.getRequestInfo());
            if (config != null) {
                log.info("MDMS mobile validation config fetched for tenantId: {}, caching...", tenantId);
                // Cache the config
                cacheRepository.cacheValidationRules(stateLevelTenantId, config);
                otpRequest.setMdmsValidationConfig(config);
            } else {
                log.info("No MDMS mobile validation config found, will use default validation");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch MDMS validation config, will use default validation: {}", e.getMessage());
        }
    }

    private void setDefaultValidationPatterns(OtpRequest otpRequest) {
        otpRequest.setDefaultNumericPattern(otpValidationConfig.getDefaultNumericPattern());
        otpRequest.setDefaultLengthPattern(otpValidationConfig.getDefaultLengthPattern());
    }
}
