package org.egov.user.security.oauth2.custom.jwt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IDPJwtValidator implements JwtValidator {

    private final AuthProperties authProperties;
    private final OidcProviderSupplier oidcProviderSupplier;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${mdms.ssoroles.masterName}")
    private String ssoRoleMasterName;

    @Value("${mdms.ssoroles.moduleName}")
    private String ssoRoleModuleName;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;

    // cache by providerId
    private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

    // cache for MDMS role mapping: key is providerId:tenantId
    private final Map<String, Map<String, String>> roleMappingCache = new ConcurrentHashMap<>();

    public IDPJwtValidator(AuthProperties authProperties, OidcProviderSupplier oidcProviderSupplier,
            ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.authProperties = authProperties;
        this.oidcProviderSupplier = oidcProviderSupplier;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Initializes the JWT validator by pre-loading SSO role mappings from MDMS for all configured providers.
     * This improves performance by caching role mappings at startup rather than fetching them on-demand.
     */
    @PostConstruct
    public void init() {
        if (authProperties.getOidc() == null || !authProperties.getOidc().isEnabled()) {
            return;
        }
        
        log.info("Initializing SSO role mapping cache for OIDC providers...");
        oidcProviderSupplier.getProviders().stream()
                .filter(p -> p.getTenantId() != null && !p.getTenantId().isEmpty())
                .forEach(provider -> {
                    log.info("Pre-loading role mapping for provider: {} and tenant: {}", 
                            provider.getId(), provider.getTenantId());
                    fetchRoleMapping(provider, provider.getTenantId());
                });
    }

    /**
     * Checks if this validator supports tokens from the given issuer.
     * Returns true if OIDC is enabled in configuration.
     *
     * @param issuer the issuer (iss) claim from the JWT token
     * @return true if OIDC is enabled, false otherwise
     */
    @Override
    public boolean supports(String issuer) {
        return authProperties.getOidc().isEnabled();
    }

    /**
     * Validates a JWT token from an identity provider.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Extracts issuer and audience from token (unverified)</li>
     *   <li>Resolves the provider configuration based on issuer and audience</li>
     *   <li>Decodes and validates the token signature using JWKS</li>
     *   <li>Validates standard claims (iss, aud, exp, nbf)</li>
     *   <li>Extracts and normalizes tenant ID and user type</li>
     *   <li>Maps roles from JWT claims to Digit roles using MDMS or configuration</li>
     *   <li>Extracts boundary information from roles</li>
     * </ol>
     *
     * @param token the raw JWT token string to validate
     * @return OidcValidatedJwt containing validated claims and extracted information
     * @throws OAuth2Exception if token validation fails (invalid signature, expired, wrong audience, etc.)
     * @throws CustomException if provider configuration is missing or invalid
     */
    @Override
    public OidcValidatedJwt validate(String token) {
        try {
            Map<String, Object> claims = null;
            Date expirationTime = null;
            Date issueTime = null;
            Set<String> roles = new HashSet<>();
            String boundary = null;
            String issuer = extractIssuerUnverified(token);
            List<String> audiences = extractAudiencesUnverified(token);
            AuthProperties.Provider provider = resolveProvider(issuer, audiences);
            JwtDecoder decoder = getDecoder(provider);
            Jwt jwt = null;
            try {
                jwt = decoder.decode(token);
            } catch (Exception e) {
                log.error("Error while decoding the jwt token", e);
                throw new OAuth2Exception(e.getMessage(), e);
            }
            try {
                claims = new HashMap<>(jwt.getClaims());
                expirationTime = Date.from(jwt.getExpiresAt());
                issueTime = Date.from(jwt.getIssuedAt());
                String tenantId = firstNonEmpty((String) claims.get("tenantId"), (String) claims.get("tenant_id"),
                        provider.getTenantId());
                String userType = firstNonEmpty((String) claims.get("userType"), (String) claims.get("user_type"),
                        provider.getUserType());
                claims.put("tenantId", tenantId);
                claims.put("userType", userType);
                roles = extractRoles(provider, claims);
                boundary = extractBoundary(provider, claims);
            } catch (Exception e) {
                log.error("Error while parsing the jwt token", e);
                throw new OAuth2Exception("Invalid JWT token", e);
            }
            return new OidcValidatedJwt(roles, claims, expirationTime, issueTime, provider.getProjectName(),
                    provider.getHierarchyType(), boundary, token, provider.getId());
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation", e);
            throw new OAuth2Exception("JWT validation failed: " + e.getMessage(), e);
        }
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
        String tenantId = (String) claims.get("tenantId");
        Map<String, String> rolesMapping = fetchRoleMapping(provider, tenantId);
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
     * Fetches role mapping for a provider and tenant, using cache if available.
     * Falls back to provider configuration if MDMS fetch fails.
     *
     * @param provider the OIDC provider configuration
     * @param tenantId the tenant ID to fetch role mapping for
     * @return map of SSO role names to Digit role codes (comma-separated if multiple)
     */
    private Map<String, String> fetchRoleMapping(AuthProperties.Provider provider, String tenantId) {
        String cacheKey = provider.getId() + ":" + tenantId;
        return roleMappingCache.computeIfAbsent(cacheKey, k -> {
            try {
                return fetchRoleMappingFromMdms(tenantId);
            } catch (Exception e) {
                log.error("Failed to fetch role mapping from MDMS for tenant {}. Falling back to config.", tenantId, e);
            }
            log.info("Falling back to configured role mapping for provider: {} and tenant: {}", 
                    provider.getId(), tenantId);
            return getProviderRoleMapping(provider);
        });
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
     * Fetches SSO role mapping from MDMS (Master Data Management Service).
     * Queries MDMS for role mappings configured for the tenant.
     *
     * @param tenantId the tenant ID to fetch role mapping for
     * @return map of SSO role names to Digit role codes, empty map if not found
     */
    private Map<String, String> fetchRoleMappingFromMdms(String tenantId) {
        String url = mdmsHost + mdmsEndpoint;

        MasterDetail masterDetail = MasterDetail.builder().name(ssoRoleMasterName).build();
        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(ssoRoleModuleName)
                .masterDetails(Collections.singletonList(masterDetail))
                .build();

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(tenantId);
        mdmsCriteria.setModuleDetails(Collections.singletonList(moduleDetail));

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setRequestInfo(new RequestInfo());
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);

        log.debug("Fetching SSO role mapping from MDMS for tenant {}: {}", tenantId, url);
        JsonNode response = restTemplate.postForObject(url, mdmsCriteriaReq, JsonNode.class);

        // Early return if response is invalid
        if (response == null || !response.has("MdmsRes")) {
            log.warn("No SSO role mapping found in MDMS for tenant {}: invalid response", tenantId);
            return Collections.emptyMap();
        }

        JsonNode mdmsRes = response.get("MdmsRes");
        if (!mdmsRes.has(ssoRoleModuleName)) {
            log.warn("No SSO role mapping found in MDMS for tenant {}: module not found", tenantId);
            return Collections.emptyMap();
        }

        JsonNode moduleNode = mdmsRes.get(ssoRoleModuleName);
        if (!moduleNode.has(ssoRoleMasterName)) {
            log.warn("No SSO role mapping found in MDMS for tenant {}: master not found", tenantId);
            return Collections.emptyMap();
        }

        JsonNode masterNode = moduleNode.get(ssoRoleMasterName);
        if (!masterNode.isArray() || masterNode.size() == 0) {
            log.warn("No SSO role mapping found in MDMS for tenant {}: empty master array", tenantId);
            return Collections.emptyMap();
        }

        // Process role mappings from master node array
        Map<String, String> mapping = new HashMap<>();
        for (JsonNode entry : masterNode) {
            if (isValidRoleMappingEntry(entry)) {
                String ssoRole = entry.get("ssoRole").asText();
                String digitRolesStr = extractDigitRoles(entry);
                if (ssoRole != null && digitRolesStr != null) {
                    mapping.merge(ssoRole, digitRolesStr, (oldVal, newVal) -> oldVal + "," + newVal);
                }
            }
        }

        if (mapping.isEmpty()) {
            log.warn("No SSO role mapping found in MDMS for tenant {}", tenantId);
        }
        return mapping;
    }

    /**
     * Checks if a JSON entry contains valid role mapping data.
     *
     * @param entry the JSON node entry to validate
     * @return true if entry has ssoRole and either digitRoles or digitRole
     */
    private boolean isValidRoleMappingEntry(JsonNode entry) {
        return entry != null 
                && entry.has("ssoRole") 
                && (entry.has("digitRoles") || entry.has("digitRole"));
    }

    /**
     * Extracts digit roles string from a role mapping entry.
     * Handles both array and string formats.
     *
     * @param entry the JSON node entry containing role mapping
     * @return comma-separated string of digit roles, or null if not found
     */
    private String extractDigitRoles(JsonNode entry) {
        JsonNode digitRolesNode = entry.has("digitRoles") 
                ? entry.get("digitRoles") 
                : entry.get("digitRole");
        
        if (digitRolesNode == null) {
            return null;
        }

        return digitRolesNode.isArray()
                ? extractRolesFromArray(digitRolesNode)
                : digitRolesNode.asText();
    }

    /**
     * Extracts roles from a JSON array and returns as comma-separated string.
     *
     * @param rolesArray the JSON array node containing roles
     * @return comma-separated string of roles
     */
    private String extractRolesFromArray(JsonNode rolesArray) {
        return java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(rolesArray.iterator(), 
                        java.util.Spliterator.ORDERED), false)
                .map(JsonNode::asText)
                .collect(Collectors.joining(","));
    }

    /**
     * Extracts boundary code from JWT claims based on roles.
     * Uses role-to-boundary mapping from provider configuration, with fallback to defaults.
     *
     * @param provider the OIDC provider configuration
     * @param claims the validated JWT claims map
     * @return boundary code string
     * @throws OAuth2Exception if no boundary code can be determined
     */
    private String extractBoundary(AuthProperties.Provider provider, Map<String, Object> claims) {
        String roleClaimKey = provider.getRoleClaimKey();
        Object rolesObject = claims.get(roleClaimKey);
        
        // Try to get boundary from role mapping
        String boundaryCode = extractBoundaryFromRoles(provider, rolesObject);
        
        // Fallback to provider default
        if (boundaryCode == null) {
            boundaryCode = provider.getDefaultBoundaryCode();
        }
        
        // Fallback to global default
        if (boundaryCode == null) {
            boundaryCode = authProperties.getDefaultBoundaryCode();
        }
        
        // Throw exception if still no boundary found
        if (boundaryCode == null) {
            log.error("No boundary mapping found for roles {}", rolesObject);
            throw new OAuth2Exception("No boundaryCode mapping found for roles " + rolesObject);
        }
        
        return boundaryCode;
    }

    /**
     * Extracts boundary code from roles using role-to-boundary mapping.
     *
     * @param provider the OIDC provider configuration
     * @param rolesObject the roles object from JWT claims
     * @return boundary code if found, null otherwise
     */
    private String extractBoundaryFromRoles(AuthProperties.Provider provider, Object rolesObject) {
        Map<String, String> roleProjectMapping = provider.getRoleBoundaryMapping();
        if (roleProjectMapping == null || !(rolesObject instanceof List)) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObject;
        return roles.stream()
                .map(roleProjectMapping::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
            throw new RuntimeException("Failed to parse JWT", e);
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
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }

    /**
     * Gets or creates a JWT decoder for the given provider.
     * Decoders are cached by provider ID to avoid repeated initialization.
     * Configures validators for issuer, audience, and timestamps.
     *
     * @param provider the OIDC provider configuration
     * @return configured JwtDecoder instance
     * @throws CustomException if JWKS URI or issuer URI is missing
     */
    private JwtDecoder getDecoder(AuthProperties.Provider provider) {
        return decoders.computeIfAbsent(provider.getId(), id -> {
            validateProviderConfiguration(provider);
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(provider.getJwkSetUri().trim()).build();
            decoder.setJwtValidator(createJwtValidator(provider));
            return decoder;
        });
    }

    /**
     * Validates that provider has required configuration for JWT decoding.
     *
     * @param provider the provider to validate
     * @throws CustomException if required configuration is missing
     */
    private void validateProviderConfiguration(AuthProperties.Provider provider) {
        if (provider.getJwkSetUri() == null || provider.getJwkSetUri().trim().isEmpty()) {
            throw new CustomException("OIDC_JWKS_MISSING",
                    "jwk-set-uri missing for providerId=" + provider.getId());
        }
        if (provider.getIssuerUri() == null || provider.getIssuerUri().trim().isEmpty()) {
            throw new CustomException("OIDC_ISSUER_MISSING", 
                    "issuer-uri missing for providerId=" + provider.getId());
        }
    }

    /**
     * Creates JWT validator with timestamp, issuer, and optionally audience validation.
     *
     * @param provider the OIDC provider configuration
     * @return configured OAuth2TokenValidator
     */
    private OAuth2TokenValidator<Jwt> createJwtValidator(AuthProperties.Provider provider) {
        OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new MultiIssuerValidator(getAllowedIssuers(provider));
        
        List<OAuth2TokenValidator<Jwt>> validators = new java.util.ArrayList<>();
        validators.add(timestampValidator);
        validators.add(issuerValidator);
        
        if (provider.getAudiences() != null && !provider.getAudiences().isEmpty()) {
            validators.add(new AudienceValidator(provider.getAudiences()));
        }
        
        return new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(validators);
    }

    /**
     * Resolves the provider configuration based on issuer and audience.
     * Handles cases where multiple providers share the same issuer by using audience for disambiguation.
     *
     * @param issuerRaw the raw issuer claim from the token
     * @param tokenAudiences the audience claims from the token
     * @return the matching provider configuration
     * @throws CustomException if no provider matches or multiple providers match without disambiguation
     */
    private AuthProperties.Provider resolveProvider(String issuerRaw, List<String> tokenAudiences) {
        String issuer = normalizeIssuer(issuerRaw);
        if (issuer == null || issuer.isEmpty()) {
            throw new CustomException("OIDC_ISSUER_MISSING_IN_TOKEN", "Missing issuer (iss) in token");
        }

        List<AuthProperties.Provider> issuerMatches = oidcProviderSupplier.getProviders().stream()
                .filter(p -> matchesIssuer(p, issuer))
                .collect(Collectors.toList());

        if (issuerMatches.isEmpty()) {
            throw new CustomException("OIDC_PROVIDER_NOT_FOUND",
                    "No OIDC provider configured for issuer=" + issuerRaw);
        }

        if (issuerMatches.size() == 1) {
            return issuerMatches.get(0);
        }

        // Disambiguate by audience when multiple providers share the same issuer
        return resolveProviderByAudience(issuerRaw, issuerMatches, tokenAudiences);
    }

    /**
     * Resolves provider from multiple issuer matches using audience disambiguation.
     *
     * @param issuerRaw the raw issuer claim for error messages
     * @param issuerMatches list of providers matching the issuer
     * @param tokenAudiences the audience claims from the token
     * @return the matching provider configuration
     * @throws CustomException if disambiguation fails
     */
    private AuthProperties.Provider resolveProviderByAudience(String issuerRaw, 
            List<AuthProperties.Provider> issuerMatches, List<String> tokenAudiences) {
        List<String> aud = tokenAudiences == null ? Collections.emptyList() : tokenAudiences;
        if (aud.isEmpty()) {
            throw new CustomException("OIDC_PROVIDER_AMBIGUOUS",
                    "Multiple OIDC providers match issuer=" + issuerRaw + " but token has no audience (aud)");
        }

        List<AuthProperties.Provider> audMatches = issuerMatches.stream()
                .filter(p -> intersects(p.getAudiences(), aud))
                .collect(Collectors.toList());

        if (audMatches.size() == 1) {
            return audMatches.get(0);
        }

        if (audMatches.isEmpty()) {
            throw new CustomException("OIDC_PROVIDER_NOT_FOUND",
                    "No OIDC provider matches issuer=" + issuerRaw + " and audience=" + aud);
        }

        String matchedIds = audMatches.stream()
                .map(AuthProperties.Provider::getId)
                .collect(Collectors.joining(","));
        throw new CustomException("OIDC_PROVIDER_AMBIGUOUS",
                "Multiple OIDC providers match issuer=" + issuerRaw + " and audience=" + aud + " providers=" + matchedIds);
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
            
            return createFailureResult("Invalid issuer (iss). Expected one of " + allowedIssuersNormalized + " but was " + iss);
        }

        private OAuth2TokenValidatorResult createFailureResult(String message) {
            OAuth2Error error = new OAuth2Error("invalid_token", message, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final Set<String> allowedAudiences;

        /**
         * Creates an audience validator.
         *
         * @param allowedAudiences list of allowed audience values
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
            
            return createFailureResult("Invalid audience (aud). Expected one of " + allowedAudiences + " but was " + aud);
        }

        private OAuth2TokenValidatorResult createFailureResult(String message) {
            OAuth2Error error = new OAuth2Error("invalid_token", message, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
