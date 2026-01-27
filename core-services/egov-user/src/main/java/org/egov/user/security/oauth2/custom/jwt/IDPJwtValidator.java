package org.egov.user.security.oauth2.custom.jwt;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.AuthProperties;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IDPJwtValidator implements JwtValidator {

    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    // cache by providerId
    private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

    public IDPJwtValidator(AuthProperties authProperties, ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String issuer) {
        return authProperties.getOidc().isEnabled();
    }

    @Override
    public OidcValidatedJwt validate(String token) {
        Map<String, Object> claims = null;
        Date expirationTime = null;
        Date issueTime = null;
        Set<String> roles = new HashSet<>();
        String boundary = null;
        String issuer = extractIssuerUnverified(token);
        AuthProperties.Provider provider = resolveProviderByIssuer(issuer)
                .orElseThrow(
                        () -> new CustomException("OIDC_PROVIDER_NOT_FOUND", "No OIDC provider configured for issuer"));
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
                provider.getHierarchyType(), boundary);
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty())
                return value;
        }
        return null;
    }

    private Set<String> extractRoles(AuthProperties.Provider provider, Map<String, Object> claims) {
        String roleClaimKey = provider.getRoleClaimKey();
        Map<String, String> rolesMapping = provider.getRoleMapping();
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
            return roles.stream().map(rolesMapping::get).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return defaultRoles;
    }

    private String extractBoundary(AuthProperties.Provider provider, Map<String, Object> claims) {
        String roleClaimKey = provider.getRoleClaimKey();
        Map<String, String> roleProjectMapping = provider.getRoleBoundaryMapping();
        Object rolesObject = claims.get(roleClaimKey);
        String boundaryCode = null;
        if (rolesObject instanceof List) {
            List<String> roles = (List<String>) rolesObject;
            boundaryCode = roles.stream().map(roleProjectMapping::get).filter(Objects::nonNull).findFirst()
                    .orElseThrow(() -> new OAuth2Exception("No boundaryCode mapping found for roles " + rolesObject));
        }
        if (boundaryCode == null) {
            log.error("No project mapping found for roles {}", rolesObject);
            throw new OAuth2Exception("No boundaryCode mapping found for roles " + rolesObject);
        }
        return boundaryCode;
    }

    private String extractIssuerUnverified(String jwt) {
        try {
            return JWTParser.parse(jwt).getJWTClaimsSet().getIssuer();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private JwtDecoder getDecoder(AuthProperties.Provider provider) {
        return decoders.computeIfAbsent(provider.getId(), id -> {
            if (provider.getJwkSetUri() == null || provider.getJwkSetUri().trim().isEmpty()) {
                throw new CustomException("OIDC_JWKS_MISSING",
                        "jwk-set-uri missing for providerId=" + provider.getId());
            }
            return NimbusJwtDecoder.withJwkSetUri(provider.getJwkSetUri().trim()).build();
        });
    }

    private Optional<AuthProperties.Provider> resolveProviderByIssuer(String issuer) {
        if (issuer == null)
            return Optional.empty();
        for (AuthProperties.Provider p : authProperties.getProviders()) {
            if (p.getIssuerUri() != null && issuer.equals(p.getIssuerUri().trim())) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}
