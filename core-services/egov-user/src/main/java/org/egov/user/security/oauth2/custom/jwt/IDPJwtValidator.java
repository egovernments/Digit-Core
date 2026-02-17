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
        if (authProperties.getOidc() != null && authProperties.getOidc().isEnabled()) {
            log.info("Initializing SSO role mapping cache for OIDC providers...");
            for (AuthProperties.Provider provider : oidcProviderSupplier.getProviders()) {
                if (provider.getTenantId() != null && !provider.getTenantId().isEmpty()) {
                    log.info("Pre-loading role mapping for provider: {} and tenant: {}", provider.getId(),
                            provider.getTenantId());
                    fetchRoleMapping(provider, provider.getTenantId());
                }
            }
        }
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
    }

    /**
     * Returns the first non-empty string from the provided values.
     * Used to select tenant ID or user type from multiple possible sources.
     *
     * @param values variable number of string values to check
     * @return the first non-null, non-empty string, or null if all are empty
     */
    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty())
                return value;
        }
        return null;
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

        Set<String> defaultRoles = new HashSet<>();
        if (provider.getDefaultRoleCodes() != null) {
            defaultRoles.addAll(Arrays.stream(provider.getDefaultRoleCodes().split(",")).map(String::trim)
                    .collect(Collectors.toList()));
        }
        if (claims == null || claims.get(roleClaimKey) == null)
            return defaultRoles;
        Object rolesObject = claims.get(roleClaimKey);
        if (rolesObject instanceof List) {
            List<String> roles = (List<String>) rolesObject;
            return roles.stream()
                    .map(rolesMapping::get)
                    .filter(Objects::nonNull)
                    .flatMap(roleString -> Arrays.stream(roleString.split(",")).map(String::trim))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        return defaultRoles;
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
            log.info("Falling back to configured role mapping for provider: {} and tenant: {}", provider.getId(),
                    tenantId);
            return provider.getRoleMapping() != null ? provider.getRoleMapping() : Collections.emptyMap();
        });
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

        if (response != null && response.has("MdmsRes")) {
            JsonNode mdmsRes = response.get("MdmsRes");
            if (mdmsRes.has(ssoRoleModuleName)) {
                JsonNode moduleNode = mdmsRes.get(ssoRoleModuleName);
                if (moduleNode.has(ssoRoleMasterName)) {
                    JsonNode masterNode = moduleNode.get(ssoRoleMasterName);
                    if (masterNode.isArray() && masterNode.size() > 0) {
                        // Assuming the first entry contains the mapping or it's a list of mappings
                        // Based on the user's previous context, it seems to be an array of objects
                        Map<String, String> mapping = new HashMap<>();
                        for (JsonNode entry : masterNode) {
                            if (entry.has("ssoRole") && (entry.has("digitRoles") || entry.has("digitRole"))) {
                                String ssoRole = entry.get("ssoRole").asText();
                                JsonNode digitRolesNode = entry.has("digitRoles") ? entry.get("digitRoles")
                                        : entry.get("digitRole");
                                String digitRolesStr;
                                if (digitRolesNode.isArray()) {
                                    StringBuilder sb = new StringBuilder();
                                    for (JsonNode roleNode : digitRolesNode) {
                                        if (sb.length() > 0)
                                            sb.append(",");
                                        sb.append(roleNode.asText());
                                    }
                                    digitRolesStr = sb.toString();
                                } else {
                                    digitRolesStr = digitRolesNode.asText();
                                }
                                mapping.merge(ssoRole, digitRolesStr, (oldVal, newVal) -> oldVal + "," + newVal);
                            }
                        }
                        return mapping;
                    }
                }
            }
        }

        log.warn("No SSO role mapping found in MDMS for tenant {}", tenantId);
        return Collections.emptyMap();
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
        Map<String, String> roleProjectMapping = provider.getRoleBoundaryMapping();
        Object rolesObject = claims.get(roleClaimKey);
        String boundaryCode = null;
        if (roleProjectMapping != null && rolesObject instanceof List) {
            List<String> roles = (List<String>) rolesObject;
            boundaryCode = roles.stream().map(roleProjectMapping::get).filter(Objects::nonNull).findFirst()
                    .orElse(null);
        }
        if (boundaryCode == null) {
            boundaryCode = provider.getDefaultBoundaryCode();
        }
        if (boundaryCode == null) {
            boundaryCode = authProperties.getDefaultBoundaryCode();
        }
        if (boundaryCode == null) {
            log.error("No project mapping found for roles {}", rolesObject);
            throw new OAuth2Exception("No boundaryCode mapping found for roles " + rolesObject);
        }
        return boundaryCode;
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
            if (provider.getJwkSetUri() == null || provider.getJwkSetUri().trim().isEmpty()) {
                throw new CustomException("OIDC_JWKS_MISSING",
                        "jwk-set-uri missing for providerId=" + provider.getId());
            }
            if (provider.getIssuerUri() == null || provider.getIssuerUri().trim().isEmpty()) {
                throw new CustomException("OIDC_ISSUER_MISSING", "issuer-uri missing for providerId=" + provider.getId());
            }

            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(provider.getJwkSetUri().trim()).build();

            // Always validate timestamps + issuer. Validate audience when configured.
            OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
            OAuth2TokenValidator<Jwt> issuerValidator = new MultiIssuerValidator(getAllowedIssuers(provider));
            if (provider.getAudiences() != null && !provider.getAudiences().isEmpty()) {
                OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(provider.getAudiences());
                decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                        timestampValidator, issuerValidator, audienceValidator));
            } else {
                decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                        timestampValidator, issuerValidator));
            }
            return decoder;
        });
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

        // Disambiguate by audience when multiple providers share the same issuer (same tenant).
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

        String matchedIds = audMatches.stream().map(AuthProperties.Provider::getId).collect(Collectors.joining(","));
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
        if (provider.getIssuerUri() != null) {
            String norm = normalizeIssuer(provider.getIssuerUri());
            if (norm != null && !norm.isEmpty()) {
                issuers.add(norm);
            }
        }
        if (provider.getIssuerAliases() != null) {
            for (String alias : provider.getIssuerAliases()) {
                String norm = normalizeIssuer(alias);
                if (norm != null && !norm.isEmpty()) {
                    issuers.add(norm);
                }
            }
        }
        return issuers;
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
        String s = issuer.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
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
        if (providerAudiences == null || providerAudiences.isEmpty() || tokenAudiences == null || tokenAudiences.isEmpty()) {
            return false;
        }
        Set<String> tokenSet = tokenAudiences.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        for (String a : providerAudiences) {
            if (a != null && tokenSet.contains(a)) {
                return true;
            }
        }
        return false;
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
            String iss = token.getIssuer() != null ? token.getIssuer().toString() : null;
            String normalized = iss == null ? null : iss.trim().replaceAll("/+$", "");
            if (normalized != null && allowedIssuersNormalized.contains(normalized)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error("invalid_token",
                    "Invalid issuer (iss). Expected one of " + allowedIssuersNormalized + " but was " + iss, null);
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
            if (aud != null) {
                for (String a : aud) {
                    if (allowedAudiences.contains(a)) {
                        return OAuth2TokenValidatorResult.success();
                    }
                }
            }
            OAuth2Error error = new OAuth2Error("invalid_token",
                    "Invalid audience (aud). Expected one of " + allowedAudiences + " but was " + aud, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
