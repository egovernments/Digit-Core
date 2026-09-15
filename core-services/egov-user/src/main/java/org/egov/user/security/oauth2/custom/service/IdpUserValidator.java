package org.egov.user.security.oauth2.custom.service;

import org.egov.user.config.AuthProperties;
import org.egov.user.domain.model.User;

/**
 * Provider-specific validator for username/password login when the user has a non-LOCAL authProvider.
 * Checks whether the user still has access at the external IdP before allowing login.
 * Implementations are selected by {@link #supports(AuthProperties.Provider)} based on provider config (idpUserValidatorType).
 */
public interface IdpUserValidator {

    /**
     * Whether this implementation handles the given provider (e.g. idpUserValidatorType = "azure").
     */
    boolean supports(AuthProperties.Provider provider);

    /**
     * Validates that the user still has access at the IdP. Throws if access has been revoked.
     *
     * @param user     user attempting login (has idpSubject, idpIssuer, authProvider set)
     * @param provider auth provider config for this user's authProvider
     * @throws org.egov.user.domain.exception.sso.IdpUserAccessRevokedException when access is revoked
     */
    void validate(User user, AuthProperties.Provider provider);
}
