package org.egov.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.domain.exception.InvalidOtpRequestException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.MobileValidationRules;
import org.egov.domain.model.OtpRequest;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.ValidationRulesCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OtpRequestValidator {

    @Autowired
    private MdmsRepository mdmsRepository;

    @Autowired
    private ValidationRulesCacheRepository cacheRepository;

    public void validate(OtpRequest otpRequest) {
        if (!otpRequest.isMobileNumberAbsent()) {
            fetchAndSetMdmsValidationConfig(otpRequest);
        }

        if (otpRequest.isTenantIdAbsent()
                || !otpRequest.isUserNameOrMobileNumberPresent()
                || otpRequest.isInvalidType()
                || !isMobileNumberValid(otpRequest)) {
            throw new InvalidOtpRequestException(otpRequest);
        }
    }

    private void fetchAndSetMdmsValidationConfig(OtpRequest otpRequest) {
        String stateTenantId = otpRequest.getTenantId().split("\\.")[0];
        String countryCode = otpRequest.getCountryCode();
        String cacheKey = (countryCode == null || countryCode.isBlank()) ? null : countryCode;

        MobileValidationConfig cached = cacheRepository.getValidationRules(stateTenantId, cacheKey);
        if (cached != null) {
            otpRequest.setMdmsValidationConfig(cached);
            return;
        }

        List<MobileValidationConfig> configs = mdmsRepository.fetchMobileValidationConfigs(
                otpRequest.getTenantId(), otpRequest.getRequestInfo());

        MobileValidationConfig selected = selectConfig(configs, countryCode);
        if (selected != null) {
            cacheRepository.cacheValidationRules(stateTenantId, cacheKey, selected);
            otpRequest.setMdmsValidationConfig(selected);
        }
    }

    private MobileValidationConfig selectConfig(List<MobileValidationConfig> configs, String countryCode) {
        if (configs == null || configs.isEmpty()) return null;

        if (countryCode != null && !countryCode.isBlank()) {
            MobileValidationConfig match = configs.stream()
                    .filter(c -> c.getAttributes() != null
                            && (countryCode.equals(c.getAttributes().getPrefix())
                                || countryCode.equals(c.getAttributes().getCountryCode())))
                    .findFirst()
                    .orElse(null);
            if (match != null) return match;
        }

        return configs.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                .findFirst()
                .orElse(null);
    }

    private boolean isMobileNumberValid(OtpRequest otpRequest) {
        if (otpRequest.isMobileNumberAbsent()) {
            return true; // email OTP path — no mobile to validate
        }

        // PASSWORD_RESET skips format validation (existing user may have old format)
        if (otpRequest.isPasswordResetRequestType()) {
            return true;
        }

        MobileValidationConfig config = otpRequest.getMdmsValidationConfig();
        if (config == null || config.getRules() == null) {
            String countryCode = otpRequest.getCountryCode();
            String msg = (countryCode != null && !countryCode.isBlank())
                    ? "Mobile number validation failed. No validation config found for country code "
                      + countryCode + " and no default validation config is configured in MDMS"
                    : "Mobile number validation failed. No default validation config is configured in MDMS";
            otpRequest.setMdmsValidationErrorMessage(msg);
            return false;
        }

        return validateWithMdmsConfig(otpRequest, config.getRules());
    }

    private boolean validateWithMdmsConfig(OtpRequest otpRequest, MobileValidationRules rules) {
        String mobile = otpRequest.getMobileNumber();

        if (rules.getMinLength() != null && mobile.length() < rules.getMinLength()) {
            otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
            return false;
        }

        if (rules.getMaxLength() != null && mobile.length() > rules.getMaxLength()) {
            otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
            return false;
        }

        if (rules.getPattern() != null && !mobile.matches(rules.getPattern())) {
            otpRequest.setMdmsValidationErrorMessage(rules.getErrorMessage());
            return false;
        }

        return true;
    }
}
