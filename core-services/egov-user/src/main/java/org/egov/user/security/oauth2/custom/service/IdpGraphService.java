package org.egov.user.security.oauth2.custom.service;

import org.egov.user.config.AuthProperties;
import org.egov.user.domain.model.User;

import java.util.Optional;

/**
 * Provider-specific service for enriching user with MFA/details from an IdP graph or admin API.
 * Implementations are selected by {@link #supports(AuthProperties.Provider)} based on provider config (e.g. graphServiceType).
 */
public interface IdpGraphService {

    /**
     * Whether this implementation handles the given provider (e.g. graphServiceType = "azure").
     */
    boolean supports(AuthProperties.Provider provider);

    /**
     * Enrich user with MFA details from the IdP (phone last 4, device name, registered date, etc.).
     * No-op or best-effort when not configured; may throw if enrichment fails.
     *
     * @param user             user to enrich (MFA fields set on this instance)
     * @param provider         auth provider config
     * @param externalUserId   IdP-specific user id for API calls (e.g. Azure oid)
     */
    void enrichUserWithMfaDetails(User user, AuthProperties.Provider provider, String externalUserId);

    /**
     * Fetch employee creation profile (employeeType, designation, department) from the IdP graph API.
     * Best-effort: return empty when not applicable, not configured, or on failure (no throw).
     *
     * @param provider       auth provider config
     * @param externalUserId IdP-specific user id for API calls (e.g. Azure oid)
     * @return profile with at least one non-blank field, or empty
     */
    Optional<EmployeeCreationProfile> getEmployeeCreationProfile(AuthProperties.Provider provider, String externalUserId);
}
