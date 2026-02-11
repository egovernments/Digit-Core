package org.egov.user.config;

import java.util.List;

/**
 * Supplies the list of OIDC auth providers. Can be backed by static config (auth.providers)
 * or by MDMS (generic: add any IdP in MDMS and it works).
 */
public interface OidcProviderSupplier {

    /**
     * Returns the current list of OIDC providers (may be cached when backed by MDMS).
     */
    List<AuthProperties.Provider> getProviders();
}
