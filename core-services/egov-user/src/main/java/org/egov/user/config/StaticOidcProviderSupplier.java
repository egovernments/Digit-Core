package org.egov.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplies OIDC providers from static config (auth.providers in application.properties / env).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.oidc.providers-source", havingValue = OidcConfigConstants.PROVIDERS_SOURCE_STATIC, matchIfMissing = true)
public class StaticOidcProviderSupplier implements OidcProviderSupplier {

    private final AuthProperties authProperties;

    @Override
    public List<AuthProperties.Provider> getProviders() {
        return new ArrayList<>(authProperties.getProviders()); // Defensive copy
    }
}
