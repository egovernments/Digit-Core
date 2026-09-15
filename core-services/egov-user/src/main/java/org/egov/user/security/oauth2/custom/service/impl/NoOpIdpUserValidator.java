package org.egov.user.security.oauth2.custom.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.domain.model.User;
import org.egov.user.security.oauth2.custom.service.IdpUserValidator;
import org.springframework.stereotype.Component;

/**
 * No-operation implementation of IdpUserValidator.
 * Used when idpUserValidatorType is "none" or blank; skips IdP access validation on username/password login.
 */
@Slf4j
@Component
public class NoOpIdpUserValidator implements IdpUserValidator {

    @Override
    public boolean supports(AuthProperties.Provider provider) {
        if (provider == null || provider.getIdpUserValidatorType() == null) {
            return true;
        }
        String type = provider.getIdpUserValidatorType().trim();
        return type.isEmpty() || OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_NONE.equalsIgnoreCase(type);
    }

    @Override
    public void validate(User user, AuthProperties.Provider provider) {
        log.debug("NoOpIdpUserValidator: skipping IdP access validation - validator type is none or not configured");
    }
}
