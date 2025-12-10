package org.egov.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.config.OtpValidationConfig;
import org.egov.domain.exception.UserAlreadyExistInSystemException;
import org.egov.domain.exception.UserMobileNumberNotFoundException;
import org.egov.domain.exception.UserNotExistingInSystemException;
import org.egov.domain.exception.UserNotFoundException;
import org.egov.domain.model.MobileValidationConfig;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.User;
import org.egov.persistence.repository.MdmsRepository;
import org.egov.persistence.repository.OtpEmailRepository;
import org.egov.persistence.repository.OtpRepository;
import org.egov.persistence.repository.OtpSMSRepository;
import org.egov.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OtpService {

    private OtpRepository otpRepository;
    private OtpSMSRepository otpSMSSender;
    private OtpEmailRepository otpEmailRepository;
    private UserRepository userRepository;
    private MdmsRepository mdmsRepository;
    private OtpValidationConfig otpValidationConfig;

    @Autowired
    public OtpService(OtpRepository otpRepository, OtpSMSRepository otpSMSSender, OtpEmailRepository otpEmailRepository,
                      UserRepository userRepository, MdmsRepository mdmsRepository, OtpValidationConfig otpValidationConfig) {
        this.otpRepository = otpRepository;
        this.otpSMSSender = otpSMSSender;
        this.otpEmailRepository = otpEmailRepository;
        this.userRepository = userRepository;
        this.mdmsRepository = mdmsRepository;
        this.otpValidationConfig = otpValidationConfig;
    }

    public void sendOtp(OtpRequest otpRequest) {
        // Fetch MDMS validation config and set it on the request
        fetchAndSetMdmsValidationConfig(otpRequest);
        // Set default validation patterns from config
        setDefaultValidationPatterns(otpRequest);
        otpRequest.validate();
        if (otpRequest.isRegistrationRequestType() || otpRequest.isLoginRequestType()) {
            sendOtpForUserRegistration(otpRequest);
        } else {
            sendOtpForPasswordReset(otpRequest);
        }
    }

    private void sendOtpForUserRegistration(OtpRequest otpRequest) {
        final User matchingUser = userRepository.fetchUser(otpRequest.getMobileNumber(), otpRequest.getTenantId(),
                otpRequest.getUserType());

        if (otpRequest.isRegistrationRequestType() && null != matchingUser)
            throw new UserAlreadyExistInSystemException();
        else if (otpRequest.isLoginRequestType() && null == matchingUser)
            throw new UserNotExistingInSystemException();

        final String otpNumber = otpRepository.fetchOtp(otpRequest);
        otpSMSSender.send(otpRequest, otpNumber);
        if(!otpRequest.isRegistrationRequestType()) // Because new user doesn't have any email configured
            try{
                otpEmailRepository.send(matchingUser.getEmail(), otpNumber, otpRequest);
            } catch (Exception ignore){
                log.warn("Could not send OTP over email");
            }

    }

    private void sendOtpForPasswordReset(OtpRequest otpRequest) {
        final User matchingUser = userRepository.fetchUser(otpRequest.getMobileNumber(), otpRequest.getTenantId(),
                otpRequest.getUserType());
        if (null == matchingUser) {
            throw new UserNotFoundException();
        }
        if (null == matchingUser.getMobileNumber() || matchingUser.getMobileNumber().isEmpty())
            throw new UserMobileNumberNotFoundException();
        try {
            final String otpNumber = otpRepository.fetchOtp(otpRequest);
            otpRequest.setMobileNumber(matchingUser.getMobileNumber());
            otpSMSSender.send(otpRequest, otpNumber);
            try {
                otpEmailRepository.send(matchingUser.getEmail(), otpNumber, otpRequest);
            } catch (Exception ignore){
                log.warn("Could not send OTP over email");
            }
        } catch (Exception e) {
            log.error("Exception while fetching otp: ", e);
        }
    }

    private void fetchAndSetMdmsValidationConfig(OtpRequest otpRequest) {
        try {
            MobileValidationConfig config = mdmsRepository.fetchMobileValidationConfig(
                    otpRequest.getTenantId(), otpRequest.getRequestInfo());
            if (config != null) {
                log.info("MDMS mobile validation config fetched successfully for tenantId: {}", otpRequest.getTenantId());
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
