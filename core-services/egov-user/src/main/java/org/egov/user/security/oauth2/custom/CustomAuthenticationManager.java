package org.egov.user.security.oauth2.custom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {

    private boolean eraseCredentialsAfterAuthentication = true;
    
    @Autowired
    private List<AuthenticationProvider> authenticationProviders;

    @Autowired
    CustomAuthenticationManager(List<AuthenticationProvider> authenticationProviders) {
        this.authenticationProviders = authenticationProviders;
    }

    /**
     * Attempts to authenticate the provided authentication object by iterating through
     * all registered authentication providers until one successfully authenticates.
     * 
     * <p>This method:
     * <ul>
     *   <li>Iterates through all authentication providers</li>
     *   <li>Skips providers that don't support the authentication type</li>
     *   <li>Stops on first successful authentication</li>
     *   <li>Immediately throws AccountStatusException or InternalAuthenticationServiceException</li>
     *   <li>Erases credentials after successful authentication if configured</li>
     * </ul>
     *
     * @param authentication the authentication request object
     * @return a fully authenticated Authentication object with credentials and authorities
     * @throws AuthenticationException if authentication fails for all providers or account status is invalid
     * @throws OAuth2Exception if no provider can authenticate the request
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> toTest = authentication.getClass();
        Authentication result = null;

        for (AuthenticationProvider provider : authenticationProviders) {
            if (!provider.supports(toTest)) {
                continue;
            }
            log.debug("Authentication attempt using " + provider.getClass().getName());

            try {
                result = provider.authenticate(authentication);

                if (result != null) {
                    copyDetails(authentication, result);
                    break;
                }
            } catch (AccountStatusException | InternalAuthenticationServiceException e) {
                // SEC-546: Avoid polling additional providers if auth failure is due to
                // invalid account status
                throw e;
            } catch (AuthenticationException e) {
                log.error("Unable to authenticate", e);
            }
        }


        if (result != null) {
            if (eraseCredentialsAfterAuthentication
                    && (result instanceof CredentialsContainer)) {
                // Authentication is complete. Remove credentials and other secret data
                // from authentication
                ((CredentialsContainer) result).eraseCredentials();
            }

            return result;
        } else
            throw new OAuth2Exception("AUTHENTICATION_FAILURE, unable to authenticate user");

    }


    /**
     * Copies the authentication details from a source Authentication object to a
     * destination one, provided the latter does not already have one set.
     *
     * @param source source authentication
     * @param dest   the destination authentication object
     */
    private void copyDetails(Authentication source, Authentication dest) {
        if ((dest instanceof AbstractAuthenticationToken) && (dest.getDetails() == null)) {
            AbstractAuthenticationToken token = (AbstractAuthenticationToken) dest;

            token.setDetails(source.getDetails());
        }
    }

}
