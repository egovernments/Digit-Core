package org.egov.user.config;

import java.util.Collections;
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

    /**
     * Providers configured but switched off ({@code active: false}); used only to report
     * {@code sso.idp.disabled} instead of an unknown issuer.
     */
    default List<AuthProperties.Provider> getDisabledProviders() {
        return Collections.emptyList();
    }
}
