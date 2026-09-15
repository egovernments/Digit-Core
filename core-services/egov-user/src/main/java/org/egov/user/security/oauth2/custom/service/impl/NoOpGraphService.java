package org.egov.user.security.oauth2.custom.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.domain.model.User;
import org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile;
import org.egov.user.security.oauth2.custom.service.IdpGraphService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * No-operation implementation of IdpGraphService.
 * Used as fallback when no provider-specific implementation is available.
 */
@Slf4j
@Service
public class NoOpGraphService implements IdpGraphService {

    @Override
    public boolean supports(AuthProperties.Provider provider) {
        return false;
    }

    @Override
    public void enrichUserWithMfaDetails(User user, AuthProperties.Provider provider, String externalUserId) {
        log.debug("NoOpGraphService: skipping MFA enrichment - no provider implementation matches");
    }

    @Override
    public Optional<EmployeeCreationProfile> getEmployeeCreationProfile(AuthProperties.Provider provider, String externalUserId) {
        return Optional.empty();
    }
}
