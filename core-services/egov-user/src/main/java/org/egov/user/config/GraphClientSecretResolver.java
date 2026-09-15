package org.egov.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Resolves Microsoft Graph client secret for a provider.
 * Only reads from environment variable GRAPH_CLIENT_SECRET_<TENANT_ID>_<PROVIDER_ID>.
 * Throws error if environment variable is not found.
 * Enforces storing secrets via K8s Secret at deploy time.
 */
@Slf4j
@Component
public class GraphClientSecretResolver {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

    /**
     * Resolves the Graph client secret from environment variable only.
     *
     * @param provider the OIDC provider (id and tenantId used for env key)
     * @return the secret from environment variable
     * @throws IllegalStateException if environment variable is not found
     */
    public String resolve(AuthProperties.Provider provider) {
        if (provider == null) return null;

        String envKey = OidcConfigConstants.GRAPH_CLIENT_SECRET_ENV_PREFIX
                + normalizeForEnv(provider.getTenantId())
                + "_"
                + normalizeForEnv(provider.getId());
        log.debug("Resolving Graph client secret for provider: {}", provider.getId());
        String fromEnv = System.getenv(envKey);
        if (StringUtils.hasText(fromEnv)) {
            log.debug("Graph client secret resolved from env var: key={}", envKey);
            return fromEnv;
        }

        log.warn("Graph client secret not configured for provider: {}", provider.getId());
        throw new IllegalStateException("Graph client secret not configured for provider: " + provider.getId());
    }

    private static String normalizeForEnv(String value) {
        if (value == null || value.isEmpty()) return "";
        return NON_ALPHANUMERIC.matcher(value.toUpperCase()).replaceAll("_");
    }
}
