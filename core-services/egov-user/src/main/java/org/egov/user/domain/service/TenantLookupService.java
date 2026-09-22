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

import java.util.List;

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
        Object claim = jwt.getClaims().get(provider.getUsernameClaimKey());
        if (claim == null || !StringUtils.hasText(claim.toString())) {
            throw SsoMissingParamException.usernameClaimMissing(provider.getUsernameClaimKey());
        }
        String username = claim.toString().trim();
        List<UserTenantMapping> tenants = mappingRepository.findActiveByUsernameKeyAndType(
                encryptionDecryptionUtil.tenantMappingKey(username), UserType.fromValue(provider.getUserType()));
        return TenantLookupResponse.builder().username(username).tenants(tenants).build();
    }
}
