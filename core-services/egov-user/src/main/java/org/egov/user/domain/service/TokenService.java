package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.user.config.AuthProperties;
import org.egov.user.domain.exception.InvalidAccessTokenException;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.UserDetail;
import org.egov.user.persistence.repository.ActionRestRepository;
import org.egov.user.security.oauth2.custom.jwt.JwtConstants;
import org.egov.user.security.oauth2.custom.jwt.JwtValidationService;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class TokenService {

    private TokenStore tokenStore;

    private ActionRestRepository actionRestRepository;

    private JwtValidationService jwtValidationService;

    private AuthProperties authProperties;

    @Value("${roles.state.level.enabled}")
    private boolean isRoleStateLevel;

    private TokenService(TokenStore tokenStore, ActionRestRepository actionRestRepository,
                         JwtValidationService jwtValidationService, AuthProperties authProperties) {
        this.tokenStore = tokenStore;
        this.actionRestRepository = actionRestRepository;
        this.jwtValidationService = jwtValidationService;
        this.authProperties = authProperties;
    }

    /**
     * Get UserDetails By AccessToken
     *
     * @param accessToken
     * @return
     */
    public UserDetail getUser(String accessToken, String idToken) {
        if (StringUtils.isEmpty(accessToken)) {
            throw new InvalidAccessTokenException();
        }

        OAuth2Authentication authentication = tokenStore.readAuthentication(accessToken);

        if (authentication == null) {
            throw new InvalidAccessTokenException();
        }

        verifyIdTokenForSsoSession(authentication, idToken);

        SecureUser secureUser = ((SecureUser) authentication.getPrincipal());
        return new UserDetail(secureUser, null);
//		String tenantId = null;
//		if (isRoleStateLevel && (secureUser.getTenantId() != null && secureUser.getTenantId().contains(".")))
//			tenantId = secureUser.getTenantId().split("\\.")[0];
//		else
//			tenantId = secureUser.getTenantId();
//
//		List<Action> actions = actionRestRepository.getActionByRoleCodes(secureUser.getRoleCodes(), tenantId);
//		log.info("returning STATE-LEVEL roleactions for tenant: "+tenantId);
//		return new UserDetail(secureUser, actions);
    }

    private void verifyIdTokenForSsoSession(OAuth2Authentication authentication, String idToken) {
        if (authProperties.getOidc() == null || !authProperties.getOidc().isDetailsIdTokenCheckEnabled()) {
            return;
        }
        OAuth2Request request = authentication.getOAuth2Request();
        Map<String, Serializable> ext = request == null ? null : request.getExtensions();
        if (ext == null || ext.get(JwtConstants.EXT_IDP_PROVIDER_ID) == null) {
            return;
        }
        if (StringUtils.isBlank(idToken)) {
            throw IdpJwtValidationException.idTokenMissing();
        }
        OidcValidatedJwt jwt;
        try {
            jwt = jwtValidationService.validate(idToken, (String) ext.get(JwtConstants.EXT_IDP_TENANT_ID));
        } catch (IdpJwtValidationException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("ID token rejected on /_details: {}", e.getMessage());
            throw IdpJwtValidationException.invalid(e);
        }
        if (!Objects.equals(jwt.getProviderId(), ext.get(JwtConstants.EXT_IDP_PROVIDER_ID))
                || !Objects.equals(jwt.getIssuer(), ext.get(JwtConstants.EXT_IDP_ISSUER))
                || !Objects.equals(jwt.getSubject(), ext.get(JwtConstants.EXT_IDP_SUBJECT))) {
            throw IdpJwtValidationException.idTokenMismatch();
        }
    }
}
