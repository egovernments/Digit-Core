package org.egov.user.domain.service;

import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.egov.user.domain.exception.sso.SsoMissingParamException;
import org.egov.user.domain.model.UserTenantMapping;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.persistence.repository.UserTenantMappingRepository;
import org.egov.user.security.oauth2.custom.jwt.JwtValidationService;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.egov.user.web.contract.auth.TenantLookupResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class TenantLookupService {

    private final JwtValidationService jwtValidationService;
    private final OidcProviderSupplier oidcProviderSupplier;
    private final UserTenantMappingRepository mappingRepository;
    private final AuthProperties authProperties;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    public TenantLookupService(JwtValidationService jwtValidationService,
            OidcProviderSupplier oidcProviderSupplier,
            UserTenantMappingRepository mappingRepository,
            AuthProperties authProperties,
            EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.jwtValidationService = jwtValidationService;
        this.oidcProviderSupplier = oidcProviderSupplier;
        this.mappingRepository = mappingRepository;
        this.authProperties = authProperties;
    }

    public TenantLookupResponse lookup(String assertion, String tenantId) {
        String shared = authProperties.getOidc().getSharedLoginTenantId();
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(shared) || !shared.equals(tenantId)) {
            throw SsoMissingParamException.tenantNotShared(tenantId);
        }
        OidcValidatedJwt jwt = jwtValidationService.validate(assertion, tenantId);
        AuthProperties.Provider provider = oidcProviderSupplier.getProviders().stream()
                .filter(p -> jwt.getProviderId().equals(p.getId()) && tenantId.equals(p.getTenantId()))
                .findFirst()
                .orElseThrow(() -> OidcProviderConfigException.providerNotFound(jwt.getProviderId()));
        String username = provider.resolveUsername(jwt.getClaims());
        if (!StringUtils.hasText(username)) {
            throw SsoMissingParamException.usernameClaimMissing(
                    provider.getUsernameClaimKey() != null ? provider.getUsernameClaimKey() : "unique_name|email");
        }
        List<UserTenantMapping> tenants = new ArrayList<>(mappingRepository.findActiveByUsernameKeyAndType(
                encryptionDecryptionUtil.tenantMappingKey(username), UserType.fromValue(provider.getUserType())));
        addJitOnlyTenants(tenants, jwt, provider);
        return TenantLookupResponse.builder().username(username).tenants(tenants).build();
    }

    private void addJitOnlyTenants(List<UserTenantMapping> tenants, OidcValidatedJwt jwt,
            AuthProperties.Provider resolvedProvider) {
        Set<String> mappedTenants = new LinkedHashSet<>();
        for (UserTenantMapping mapping : tenants) {
            mappedTenants.add(mapping.getTenantId());
        }
        String normalizedIssuer = normalizeIssuer(jwt.getIssuer());
        Object tokenAudience = jwt.getClaims().get("aud");
        Set<String> jitTenants = new LinkedHashSet<>();
        for (AuthProperties.Provider candidate : oidcProviderSupplier.getProviders()) {
            if (!candidate.isJitEnabled() || !matchesIssuer(candidate, normalizedIssuer)) {
                continue;
            }
            if (!Objects.equals(resolvedProvider.getUserType(), candidate.getUserType())) {
                continue;
            }
            if (!audienceMatches(candidate, tokenAudience)) {
                continue;
            }
            String candidateTenantId = candidate.getTenantId();
            if (StringUtils.hasText(candidateTenantId) && !mappedTenants.contains(candidateTenantId)) {
                jitTenants.add(candidateTenantId);
            }
        }
        for (String tenantId : jitTenants) {
            tenants.add(UserTenantMapping.builder().tenantId(tenantId).jit(true).build());
        }
    }

    private boolean audienceMatches(AuthProperties.Provider candidate, Object tokenAudience) {
        List<String> candidateAudiences = candidate.getAudiences();
        if (candidateAudiences == null || candidateAudiences.isEmpty()) {
            return true;
        }
        Set<String> tokenAudiences = toAudienceSet(tokenAudience);
        for (String aud : candidateAudiences) {
            if (tokenAudiences.contains(aud)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> toAudienceSet(Object aud) {
        if (aud instanceof String) {
            return Collections.singleton((String) aud);
        }
        if (aud instanceof Collection) {
            Set<String> result = new LinkedHashSet<>();
            for (Object value : (Collection<?>) aud) {
                if (value != null) {
                    result.add(value.toString());
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    private boolean matchesIssuer(AuthProperties.Provider provider, String normalizedIssuer) {
        if (normalizedIssuer == null) {
            return false;
        }
        if (normalizedIssuer.equals(normalizeIssuer(provider.getIssuerUri()))) {
            return true;
        }
        List<String> aliases = provider.getIssuerAliases();
        if (aliases == null) {
            return false;
        }
        for (String alias : aliases) {
            if (normalizedIssuer.equals(normalizeIssuer(alias))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeIssuer(String issuer) {
        if (issuer == null) {
            return null;
        }
        return issuer.trim().replaceAll("/+$", "");
    }
}
