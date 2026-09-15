package org.egov.user.security.oauth2.custom.jwt;

import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IDPJwtValidator implements JwtValidator {

    private final AuthProperties authProperties;
    private final OidcProviderSupplier oidcProviderSupplier;

    // cache by tenant:providerId
    private final Map<String, DecoderEntry> decoders = new ConcurrentHashMap<>();

    /**
     * TTL in milliseconds for decoder cache entries.
     * Derived from {@code auth.oidc.jwks-cache-ttl-ms} with fallback to
     * {@link OidcConfigConstants#DEFAULT_JWKS_CACHE_TTL_MS}.
     */
    private final long decoderCacheTtlMs;

    public IDPJwtValidator(AuthProperties authProperties, OidcProviderSupplier oidcProviderSupplier) {
        this.authProperties = authProperties;
        this.oidcProviderSupplier = oidcProviderSupplier;
        Long configuredTtl = null;
        if (authProperties.getOidc() != null) {
            configuredTtl = authProperties.getOidc().getJwksCacheTtlMs();
        }
        if (configuredTtl == null || configuredTtl <= 0L) {
            this.decoderCacheTtlMs = OidcConfigConstants.DEFAULT_JWKS_CACHE_TTL_MS;
        } else {
            this.decoderCacheTtlMs = configuredTtl;
        }
    }

    /**
     * Returns true if OIDC is enabled and the given issuer matches any configured provider
     * (primary issuer URI or issuer alias). Used to route tokens to this validator.
     *
     * @param issuer the token issuer (iss claim), may be null
     * @return true if this validator supports the issuer
     */
    @Override
    public boolean supports(String issuer) {
        if (issuer == null || issuer.isEmpty() || !authProperties.getOidc().isEnabled()) {
            return false;
        }
        String normalized = normalizeIssuer(issuer);
        return oidcProviderSupplier.getProviders().stream()
                .anyMatch(p -> matchesIssuer(p, normalized));
    }

    /**
     * Validates a JWT token from an identity provider.
     *
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Extracts issuer and audience from token (unverified)</li>
     *   <li>Resolves the provider configuration based on issuer, audience, and tenant ID</li>
     *   <li>Decodes and validates the token signature using JWKS</li>
     *   <li>Validates standard claims (iss, aud, exp) using default Spring JWT validators</li>
     *   <li>Sets tenant ID from request parameter and normalizes user type from claims or provider default</li>
     *   <li>Maps roles from JWT claims to Digit roles using MDMS or configuration</li>
     * </ol>
     *
     * @param token the raw JWT token string to validate
     * @param tenantId tenant ID for provider resolution and for setting on validated claims
     * @return OidcValidatedJwt containing validated claims and extracted information
     * @throws IdpJwtValidationException if token validation fails (invalid signature, expired, wrong audience, etc.)
     * @throws OidcProviderConfigException if provider configuration is missing or invalid
     */
    @Override
    public OidcValidatedJwt validate(String token, String tenantId) {
        String issuer = extractIssuerUnverified(token);
        List<String> audiences = extractAudiencesUnverified(token);
        AuthProperties.Provider provider = resolveProvider(issuer, audiences, tenantId);
        JwtDecoder decoder = getDecoder(provider);

        Jwt jwt;
        try {
            jwt = decoder.decode(token);
        } catch (JwtValidationException e) {
            boolean isExpiry = e.getErrors().stream()
                    .anyMatch(err -> err.getDescription() != null
                            && err.getDescription().toLowerCase().contains("expired"));
            if (isExpiry) {
                log.warn("JWT token has expired for issuer: {}", issuer);
                throw IdpJwtValidationException.expired(e);
            }

            boolean isSignatureFailure = e.getErrors().stream()
                    .anyMatch(err -> {
                        String desc = err.getDescription();
                        return desc != null
                                && desc.toLowerCase().contains(SsoErrorCodes.JWT_SIGNATURE_ERROR_SUBSTRING);
                    });

            if (!isSignatureFailure) {
                log.error("JWT signature or claim validation failed for issuer: {}", issuer, e);
                throw IdpJwtValidationException.invalid(e);
            }

            // Likely signature failure: refresh decoder once and retry
            log.info(SsoErrorCodes.MSG_DECODER_REFRESH_ON_SIGNATURE_FAILURE, provider.getId());
            clearDecoderForProvider(provider.getTenantId(), provider.getId());
            JwtDecoder freshDecoder = getDecoder(provider);
            try {
                jwt = freshDecoder.decode(token);
            } catch (JwtValidationException retryEx) {
                log.error(SsoErrorCodes.MSG_DECODER_REFRESH_FAILED_AFTER_RETRY, issuer, retryEx);
                throw IdpJwtValidationException.invalid(retryEx);
            } catch (Exception retryEx) {
                log.error("Error decoding JWT token after decoder refresh", retryEx);
                throw IdpJwtValidationException.invalid(retryEx);
            }
        } catch (Exception e) {
            log.error("Error decoding JWT token", e);
            throw IdpJwtValidationException.invalid(e);
        }

        Map<String, Object> claims;
        Date expirationTime;
        Date issueTime;
        Set<String> roles;
        try {
            claims = new HashMap<>(jwt.getClaims());
            expirationTime = Date.from(jwt.getExpiresAt());
            issueTime = Date.from(jwt.getIssuedAt());
            String userType = firstNonEmpty((String) claims.get(JwtConstants.CLAIM_USER_TYPE),
                    (String) claims.get(JwtConstants.CLAIM_USER_TYPE_ALT), provider.getUserType());
            claims.put(JwtConstants.CLAIM_TENANT_ID, tenantId);
            claims.put(JwtConstants.CLAIM_USER_TYPE, userType);
            roles = extractRoles(provider, claims);
        } catch (Exception e) {
            log.error("Error parsing JWT claims", e);
            throw IdpJwtValidationException.invalid("Failed to extract claims from JWT", e);
        }

        return new OidcValidatedJwt(roles, claims, expirationTime, issueTime, token, provider.getId());
    }

    /**
     * Clears the entire JWT decoder cache for all providers.
     * Intended for operational use (e.g. config changes, testing).
     */
    public void clearDecoderCache() {
        decoders.clear();
        log.info(SsoErrorCodes.MSG_DECODER_CACHE_CLEARED);
    }

    /**
     * Clears JWT decoder cache entries by tenant and/or provider.
     * <ul>
     *   <li>tenantId + providerId: clear specific entry for that tenant+provider.</li>
     *   <li>tenantId only (providerId null): clear all decoders for that tenant.</li>
     * </ul>
     *
     * @param tenantId tenant ID (required, null/empty treated as "default")
     * @param providerId provider ID, or null for "all providers for given tenant"
     */
    public void clearDecoderCacheFor(String tenantId, String providerId) {
        if (providerId != null) {
            // Clear specific tenant+provider combination
            String key = cacheKey(tenantId, providerId);
            DecoderEntry removed = decoders.remove(key);
            if (removed != null) {
                log.info(SsoErrorCodes.MSG_DECODER_CACHE_PROVIDER_CLEARED, key);
            }
        } else {
            // Clear all entries for this tenant
            String prefix = normalizedTenant(tenantId) + ":";
            boolean removed = decoders.keySet().removeIf(k -> k.startsWith(prefix));
            if (removed) {
                log.info(SsoErrorCodes.MSG_DECODER_CACHE_TENANT_CLEARED, normalizedTenant(tenantId));
            }
        }
    }

    /**
     * Clears decoder cache for a specific tenant and provider combination.
     * This is a convenience method that calls {@link #clearDecoderCacheFor(String, String)}.
     *
     * @param tenantId the tenant ID
     * @param providerId the provider ID
     */
    public void clearDecoderForProvider(String tenantId, String providerId) {
        clearDecoderCacheFor(tenantId, providerId);
    }

    private static String normalizedTenant(String tenantId) {
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default";
    }

    private static String cacheKey(String tenantId, String providerId) {
        return normalizedTenant(tenantId) + ":" + providerId;
    }

    /**
     * Returns the first non-empty string from the provided values.
     * Used to select tenant ID or user type from multiple possible sources.
     *
     * @param values variable number of string values to check
     * @return the first non-null, non-empty string, or null if all are empty
     */
    private String firstNonEmpty(String... values) {
        return Arrays.stream(values)
                .filter(v -> v != null && !v.isEmpty())
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts and maps roles from JWT claims to Digit role codes.
     * Uses role mapping from MDMS (cached) or provider configuration.
     * Falls back to default roles if no roles are found in the token.
     *
     * @param provider the OIDC provider configuration
     * @param claims the validated JWT claims map
     * @return set of Digit role codes mapped from JWT roles
     */
    private Set<String> extractRoles(AuthProperties.Provider provider, Map<String, Object> claims) {
        String roleClaimKey = provider.getRoleClaimKey();
        Map<String, String> rolesMapping = getProviderRoleMapping(provider);
        Set<String> defaultRoles = getDefaultRoles(provider);

        if (claims == null || roleClaimKey == null || claims.get(roleClaimKey) == null) {
            return defaultRoles;
        }

        Object rolesObject = claims.get(roleClaimKey);
        if (!(rolesObject instanceof List)) {
            return defaultRoles;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObject;
        Set<String> mappedRoles = roles.stream()
                .map(rolesMapping::get)
                .filter(Objects::nonNull)
                .flatMap(roleString -> Arrays.stream(roleString.split(",")).map(String::trim))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return mappedRoles.isEmpty() ? defaultRoles : mappedRoles;
    }

    /**
     * Gets default role codes from provider configuration.
     *
     * @param provider the OIDC provider configuration
     * @return set of default role codes, empty set if not configured
     */
    private Set<String> getDefaultRoles(AuthProperties.Provider provider) {
        if (provider.getDefaultRoleCodes() == null || provider.getDefaultRoleCodes().trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(provider.getDefaultRoleCodes().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Gets role mapping from provider configuration.
     *
     * @param provider the OIDC provider configuration
     * @return role mapping map, or empty map if not configured
     */
    private Map<String, String> getProviderRoleMapping(AuthProperties.Provider provider) {
        return provider.getRoleMapping() != null ? provider.getRoleMapping() : Collections.emptyMap();
    }


    /**
     * Extracts the issuer claim from a JWT without full validation.
     * Used for provider resolution before signature validation.
     *
     * @param jwt the JWT token string
     * @return the issuer (iss) claim value
     * @throws RuntimeException if the token cannot be parsed
     */
    private String extractIssuerUnverified(String jwt) {
        try {
            return JWTParser.parse(jwt).getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            throw IdpJwtValidationException.parseFailed(e);
        }
    }

    /**
     * Extracts the audience claim from a JWT without full validation.
     * Used for provider resolution when multiple providers share the same issuer.
     *
     * @param jwt the JWT token string
     * @return list of audience (aud) claim values, empty list if not present
     * @throws RuntimeException if the token cannot be parsed
     */
    private List<String> extractAudiencesUnverified(String jwt) {
        try {
            List<String> aud = JWTParser.parse(jwt).getJWTClaimsSet().getAudience();
            return aud == null ? Collections.emptyList() : aud;
        } catch (Exception e) {
            throw IdpJwtValidationException.parseFailed(e);
        }
    }

    /**
     * Gets or creates a JWT decoder for the given provider.
     * Decoders are cached by provider ID to avoid repeated initialization.
     * Validates provider configuration, builds NimbusJwtDecoder with JWKS URI,
     * and sets custom JWT validator.
     *
     * @param provider the OIDC provider configuration
     * @return configured JwtDecoder instance
     * @throws OidcProviderConfigException if JWKS URI or issuer URI is missing
     */
    private JwtDecoder getDecoder(AuthProperties.Provider provider) {
        String key = cacheKey(provider.getTenantId(), provider.getId());
        long now = System.currentTimeMillis();

        DecoderEntry existing = decoders.get(key);
        if (existing != null && !existing.isExpired(now, decoderCacheTtlMs)) {
            return existing.getDecoder();
        }

        validateProviderConfiguration(provider);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(provider.getJwkSetUri().trim()).build();
        decoder.setJwtValidator(createJwtValidator(provider));

        DecoderEntry newEntry = new DecoderEntry(decoder, now);
        decoders.put(key, newEntry);
        return newEntry.getDecoder();
    }

    /**
     * Validates that provider has required configuration for JWT decoding.
     *
     * @param provider the provider to validate
     * @throws OidcProviderConfigException if required configuration is missing
     */
    private void validateProviderConfiguration(AuthProperties.Provider provider) {
        if (provider.getJwkSetUri() == null || provider.getJwkSetUri().trim().isEmpty()) {
            throw OidcProviderConfigException.jwksMissing(provider.getId());
        }
        if (provider.getIssuerUri() == null || provider.getIssuerUri().trim().isEmpty()) {
            throw OidcProviderConfigException.issuerMissing(provider.getId());
        }
    }

    /**
     * Creates JWT validator with timestamp, issuer, and mandatory audience validation.
     *
     * @param provider the OIDC provider configuration
     * @return configured OAuth2TokenValidator
     * @throws OidcProviderConfigException if no audiences are configured in the provider
     */
    private OAuth2TokenValidator<Jwt> createJwtValidator(AuthProperties.Provider provider) {
        OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new MultiIssuerValidator(getAllowedIssuers(provider));
        
        List<OAuth2TokenValidator<Jwt>> validators = new java.util.ArrayList<>();
        validators.add(timestampValidator);
        validators.add(issuerValidator);
        
        // Audience validation is now mandatory - provider must have audiences configured
        if (provider.getAudiences() == null || provider.getAudiences().isEmpty()) {
            throw OidcProviderConfigException.audiencesMissing(provider.getId());
        }
        validators.add(new AudienceValidator(provider.getAudiences()));
        
        return new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(validators);
    }

    /**
     * Resolves the provider configuration based on issuer, audience, and tenant ID.
     * Handles cases where multiple providers share the same issuer by using audience for disambiguation.
     *
     * @param issuerRaw the raw issuer claim from the token
     * @param tokenAudiences the audience claims from the token
     * @param tenantId tenant ID to filter providers (optional; if null or empty, not used for filtering)
     * @return the matching provider configuration
     * @throws OidcProviderConfigException if no provider matches or multiple providers match without disambiguation
     */
    private AuthProperties.Provider resolveProvider(String issuerRaw, List<String> tokenAudiences, String tenantId) {
        String issuer = normalizeIssuer(issuerRaw);
        if (issuer == null || issuer.isEmpty()) {
            throw OidcProviderConfigException.issuerMissingInToken();
        }

        List<AuthProperties.Provider> issuerMatches = oidcProviderSupplier.getProviders().stream()
                .filter(p -> matchesIssuer(p, issuer))
                .filter(p -> {
                    // If no tenantId provided, don't filter by tenantId
                    if (tenantId == null || tenantId.isEmpty()) {
                        return true;
                    }
                    // If provider has no tenantId configured, don't match
                    if (p.getTenantId() == null || p.getTenantId().isEmpty()) {
                        return false;
                    }
                    // Match tenantId exactly
                    return tenantId.equals(p.getTenantId());
                })
                .collect(Collectors.toList());

        if (issuerMatches.isEmpty()) {
            throw OidcProviderConfigException.providerNotFound(issuerRaw);
        }

        if (issuerMatches.size() == 1) {
            return issuerMatches.get(0);
        }

        // Disambiguate by audience when multiple providers share the same issuer
        return resolveProviderByAudience(issuerRaw, issuerMatches, tokenAudiences);
    }

    /**
     * Resolves provider from multiple issuer matches using audience disambiguation.
     * Filters providers by intersecting their configured audiences with token audiences.
     *
     * @param issuerRaw the raw issuer claim for error messages
     * @param issuerMatches list of providers matching the issuer
     * @param tokenAudiences the audience claims from the token
     * @return the matching provider configuration
     * @throws OidcProviderConfigException if disambiguation fails:
     *         - providerAmbiguous when no audiences in token or multiple audience matches
     *         - providerNotFound when no providers match the token audiences
     */
    private AuthProperties.Provider resolveProviderByAudience(String issuerRaw, 
            List<AuthProperties.Provider> issuerMatches, List<String> tokenAudiences) {
        List<String> aud = tokenAudiences == null ? Collections.emptyList() : tokenAudiences;
        if (aud.isEmpty()) {
            throw OidcProviderConfigException.providerAmbiguous(issuerRaw);
        }

        List<AuthProperties.Provider> audMatches = issuerMatches.stream()
                .filter(p -> intersects(p.getAudiences(), aud))
                .collect(Collectors.toList());

        if (audMatches.size() == 1) {
            return audMatches.get(0);
        }

        if (audMatches.isEmpty()) {
            throw OidcProviderConfigException.providerNotFound(issuerRaw);
        }

        throw OidcProviderConfigException.providerAmbiguous(issuerRaw);
    }

    /**
     * Checks if a provider matches the given normalized issuer.
     * Compares against both the primary issuer URI and any configured aliases.
     *
     * @param provider the provider configuration to check
     * @param normalizedIssuer the normalized issuer string to match
     * @return true if the provider matches this issuer, false otherwise
     */
    private boolean matchesIssuer(AuthProperties.Provider provider, String normalizedIssuer) {
        if (provider == null) {
            return false;
        }
        Set<String> allowedIssuers = getAllowedIssuers(provider);
        return allowedIssuers.contains(normalizedIssuer);
    }

    /**
     * Gets all allowed issuer URIs for a provider, including aliases.
     * Normalizes all issuer strings (removes trailing slashes).
     *
     * @param provider the provider configuration
     * @return set of normalized issuer URIs
     */
    private Set<String> getAllowedIssuers(AuthProperties.Provider provider) {
        Set<String> issuers = new HashSet<>();
        
        // Add primary issuer URI
        addNormalizedIssuer(issuers, provider.getIssuerUri());
        
        // Add issuer aliases
        if (provider.getIssuerAliases() != null) {
            provider.getIssuerAliases().stream()
                    .forEach(alias -> addNormalizedIssuer(issuers, alias));
        }
        
        return issuers;
    }

    /**
     * Normalizes and adds an issuer URI to the set if it's valid.
     *
     * @param issuers the set to add to
     * @param issuerUri the issuer URI to normalize and add
     */
    private void addNormalizedIssuer(Set<String> issuers, String issuerUri) {
        if (issuerUri == null) {
            return;
        }
        String normalized = normalizeIssuer(issuerUri);
        if (normalized != null && !normalized.isEmpty()) {
            issuers.add(normalized);
        }
    }

    /**
     * Normalizes an issuer URI by trimming whitespace and removing trailing slashes.
     * Ensures consistent comparison of issuer values.
     *
     * @param issuer the issuer URI to normalize
     * @return normalized issuer URI, or null if input is null
     */
    private String normalizeIssuer(String issuer) {
        if (issuer == null) {
            return null;
        }
        return issuer.trim().replaceAll("/+$", "");
    }

    /**
     * Checks if provider audiences and token audiences have any intersection.
     * Used to disambiguate providers when multiple share the same issuer.
     *
     * @param providerAudiences the audiences configured for the provider
     * @param tokenAudiences the audiences from the token
     * @return true if there is at least one common audience, false otherwise
     */
    private boolean intersects(List<String> providerAudiences, List<String> tokenAudiences) {
        if (providerAudiences == null || providerAudiences.isEmpty() 
                || tokenAudiences == null || tokenAudiences.isEmpty()) {
            return false;
        }
        
        Set<String> tokenSet = tokenAudiences.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return providerAudiences.stream()
                .filter(Objects::nonNull)
                .anyMatch(tokenSet::contains);
    }

    private static final class DecoderEntry {
        private final JwtDecoder decoder;
        private final long createdAtMs;

        private DecoderEntry(JwtDecoder decoder, long createdAtMs) {
            this.decoder = decoder;
            this.createdAtMs = createdAtMs;
        }

        private boolean isExpired(long nowMs, long ttlMs) {
            if (ttlMs == 0L) {
                // TTL of 0 means cache is disabled - always expired
                return true;
            }
            if (ttlMs < 0L) {
                // Negative TTL means cache never expires
                return false;
            }
            // Positive TTL - check if time elapsed exceeds TTL
            return (nowMs - createdAtMs) >= ttlMs;
        }

        private JwtDecoder getDecoder() {
            return decoder;
        }
    }

    private static class MultiIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final Set<String> allowedIssuersNormalized;

        /**
         * Creates a multi-issuer validator.
         *
         * @param allowedIssuersNormalized set of normalized issuer URIs that are allowed
         */
        private MultiIssuerValidator(Set<String> allowedIssuersNormalized) {
            this.allowedIssuersNormalized = allowedIssuersNormalized == null ? Collections.emptySet() : allowedIssuersNormalized;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            if (token.getIssuer() == null) {
                return createFailureResult("Missing issuer (iss) in token");
            }
            
            String iss = token.getIssuer().toString();
            String normalized = iss.trim().replaceAll("/+$", "");
            
            if (allowedIssuersNormalized.contains(normalized)) {
                return OAuth2TokenValidatorResult.success();
            }
            
            log.debug("JWT issuer mismatch: received '{}', allowed {}", iss, allowedIssuersNormalized);
            return createFailureResult(SsoErrorCodes.MSG_INVALID_ISSUER);
        }

        private OAuth2TokenValidatorResult createFailureResult(String message) {
            OAuth2Error error = new OAuth2Error(JwtConstants.OAUTH2_ERROR_INVALID_TOKEN, message, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final Set<String> allowedAudiences;

        /**
         * Creates an audience validator.
         *
         * @param allowedAudiences list of allowed audience values (must be non-empty)
         */
        private AudienceValidator(List<String> allowedAudiences) {
            this.allowedAudiences = allowedAudiences == null ? Collections.emptySet()
                    : allowedAudiences.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }

        /**
         * Validates that the token's audience matches at least one of the allowed audiences.
         *
         * @param token the JWT token to validate
         * @return success if audience matches, failure otherwise
         */
        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            List<String> aud = token.getAudience();
            if (aud == null || aud.isEmpty()) {
                return createFailureResult("Missing audience (aud) in token");
            }
            
            boolean hasMatch = aud.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(allowedAudiences::contains);
            
            if (hasMatch) {
                return OAuth2TokenValidatorResult.success();
            }
            
            log.debug("JWT audience mismatch: received {}, allowed {}", aud, allowedAudiences);
            return createFailureResult(SsoErrorCodes.MSG_INVALID_AUDIENCE);
        }

        private OAuth2TokenValidatorResult createFailureResult(String message) {
            OAuth2Error error = new OAuth2Error(JwtConstants.OAUTH2_ERROR_INVALID_TOKEN, message, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
