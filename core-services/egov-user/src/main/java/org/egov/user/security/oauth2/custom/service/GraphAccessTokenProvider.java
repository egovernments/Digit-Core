package org.egov.user.security.oauth2.custom.service;

import org.egov.user.config.AuthProperties;

/**
 * Provides Microsoft Graph API access tokens (client-credentials flow, cached).
 * Shared by MsGraphService and AzureIdpUserValidator.
 */
public interface GraphAccessTokenProvider {

    /**
     * Obtains an access token for the given provider. Tokens are cached in Redis.
     *
     * @param provider the OIDC provider with Graph credentials (graphClientId, graphTenantId, etc.)
     * @return the access token string, or null if acquisition fails
     */
    String getAccessToken(AuthProperties.Provider provider);
}
