package org.egov.user.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Supplies OIDC providers from static config (auth.providers in application.properties / env).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.oidc.providers-source", havingValue = "static", matchIfMissing = true)
public class StaticOidcProviderSupplier implements OidcProviderSupplier {

    private final AuthProperties authProperties;

    @Override
    public List<AuthProperties.Provider> getProviders() {
        return authProperties.getProviders();
    }
}
