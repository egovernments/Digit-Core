package org.egov.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.MobileNumerValidationCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Validates OTP requests against MobileNumberValidation rules from MDMS.
 * Validation uses mobileNumberRegex exclusively — no separate min/max length checks.
 *
 * Tenant resolution: strips to state-level tenant for cache keying.
 * Fallback: if MDMS returns no config, application.properties default regex is used.
 */
@Component
@Slf4j
public class OtpRequestValidator {

    private final MdmsRepository mdmsRepository;
    
    private final MobileNumerValidationCacheRepository cacheRepository;

    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Value("${egov.mobile.validation.default.regex:^[6-9][0-9]{9}$}")
    private String defaultRegex;

    @Autowired
    public OtpRequestValidator(MdmsRepository mdmsRepository,
                               MobileNumerValidationCacheRepository cacheRepository, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.mdmsRepository = mdmsRepository;
        this.cacheRepository = cacheRepository;
        this.multiStateInstanceUtil = multiStateInstanceUtil;    }

    /**
     * Validates the OTP request. Throws InvalidOtpRequestException on failure.
     */
    public void validate(OtpRequest otpRequest) {
        fetchAndSetMdmsValidationConfig(otpRequest);

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
     * Returns true if the mobile number passes validation.
     * PASSWORD_RESET is always allowed (legacy users must still be able to reset).
     * If no MDMS config is available, the application.properties default is used.
     */
    public boolean isMobileNumberValid(OtpRequest otpRequest) {
        if (OtpRequestType.PASSWORD_RESET.equals(otpRequest.getType())) {
            return true;
        }

        MobileValidationConfig config = otpRequest.getMdmsValidationConfig();

        if (config == null || !StringUtils.hasText(config.getMobileNumberRegex())) {
            if (StringUtils.hasText(otpRequest.getCountryCode())) {
                otpRequest.setMdmsValidationErrorMessage(
                        "Mobile number validation configuration not found for country code: "
                                + otpRequest.getCountryCode());
                return false;
            }
            log.warn("No MDMS config available for tenantId: {}. Falling back to default regex.",
                    otpRequest.getTenantId());
            return matchesRegex(otpRequest, defaultRegex);
        }

        return matchesRegex(otpRequest, config.getMobileNumberRegex());
    }

    private boolean matchesRegex(OtpRequest otpRequest, String regex) {
        String mobile = otpRequest.getMobileNumber();
        try {
            if (!mobile.matches(regex)) {
                otpRequest.setMdmsValidationErrorMessage(
                        "Mobile number must match the configured pattern: " + regex);
                return false;
            }
            return true;
        } catch (PatternSyntaxException e) {
            log.error("Invalid regex '{}' in MDMS config. Rejecting request to be safe.", regex, e);
            otpRequest.setMdmsValidationErrorMessage("Invalid mobile number validation pattern configured.");
            return false;
        }
    }

    /**
     * Resolves the validation config for the request from cache → MDMS.
     * Selection:
     *   - countryCode present → find matching entry, fallback to default=true entry
     *   - countryCode absent  → use default=true entry
     */
    private void fetchAndSetMdmsValidationConfig(OtpRequest otpRequest) {
        String tenantId = otpRequest.getTenantId();
        if (isEmpty(tenantId)) return;

        String stateTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
        String countryCode = otpRequest.getCountryCode();
        String cacheKey = StringUtils.hasText(countryCode) ? countryCode : "default";

        try {
            MobileValidationConfig cached = cacheRepository.getValidationRules(stateTenantId, cacheKey);
            if (cached != null) {
                otpRequest.setMdmsValidationConfig(cached);
                return;
            }

            List<MobileValidationConfig> configs =
                    mdmsRepository.fetchMobileValidationConfigs(tenantId, otpRequest.getRequestInfo());

            MobileValidationConfig selected = selectConfig(configs, countryCode);
            if (selected != null) {
                cacheRepository.cacheValidationRules(stateTenantId, cacheKey, selected);
                otpRequest.setMdmsValidationConfig(selected);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch MDMS validation config for tenantId: {} countryCode: {} — {}",
                    tenantId, countryCode, e.getMessage());
        }
    }

    /**
     * Picks the best matching config.
     * Priority: exact countryCode match > default=true entry.
     */
    private MobileValidationConfig selectConfig(List<MobileValidationConfig> configs, String countryCode) {
        if (configs == null || configs.isEmpty()) return null;

        MobileValidationConfig defaultConfig = null;
        for (MobileValidationConfig cfg : configs) {
            if (Boolean.TRUE.equals(cfg.getIsDefault()) && defaultConfig == null) {
                defaultConfig = cfg;
            }
            if (StringUtils.hasText(countryCode) && countryCode.equals(cfg.getCountryCode())) {
                return cfg;
            }
        }

        if (StringUtils.hasText(countryCode) && defaultConfig != null) {
            log.info("No MDMS config for countryCode: {}, falling back to default config.", countryCode);
        }
        return defaultConfig;
    }
}
