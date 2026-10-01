package org.egov.user.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.user.security.oauth2.custom.jwt.JwtConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.egov.user.config.AuthProperties.Provider.ROLE_MAPPING_MAPPER;

/**
 * Supplies OIDC providers from MDMS. Add provider config in MDMS master (any IdP: Azure, Google, etc.).
 * List is cached and refreshed periodically.
 * <p>
 * In a <b>central instance</b> (multiple tenants), set {@code mdms.oidcproviders.tenantId} to a
 * <b>comma-separated list</b> of state-level tenant IDs (e.g. {@code tg,pb,pg}). The supplier
 * fetches from MDMS for each tenant and merges the provider lists so each tenant's OIDC config
 * is applied. Each provider's {@code tenantId} in MDMS defines which DIGIT tenant the user
 * belongs to.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "auth.oidc.providers-source", havingValue = OidcConfigConstants.PROVIDERS_SOURCE_MDMS)
public class MdmsOidcProviderSupplier implements OidcProviderSupplier {

    private final RestTemplate restTemplate;
    private final String mdmsHost;
    private final String mdmsEndpoint;
    private final String moduleName;
    private final String masterName;
    /** Comma-separated tenant IDs for central instance; single tenant otherwise. */
    private final List<String> tenantIds;

    /**
     * Atomic cache entry holding both providers and timestamp to prevent race conditions
     */
    private static class CacheEntry {
        final List<AuthProperties.Provider> providers;
        final List<AuthProperties.Provider> disabled;
        final long timestamp;
        
        CacheEntry(List<AuthProperties.Provider> providers, List<AuthProperties.Provider> disabled, long timestamp) {
            this.providers = providers;
            this.disabled = disabled;
            this.timestamp = timestamp;
        }
    }

    private final AtomicReference<CacheEntry> cache = new AtomicReference<>(null);
    private final long cacheTtlMs;
    private final long retryAfterFailureMs;
    private final ReentrantLock refreshLock = new ReentrantLock();
    private volatile long lastFailureAt = 0L;

    public MdmsOidcProviderSupplier(
            RestTemplate restTemplate,
            @Value("${egov.mdms.host}") String mdmsHost,
            @Value("${egov.mdms.search.endpoint}") String mdmsEndpoint,
            @Value("${mdms.oidcproviders.moduleName}") String moduleName,
            @Value("${mdms.oidcproviders.masterName}") String masterName,
            @Value("${mdms.oidcproviders.tenantId}") String tenantIdConfig,
            @Value("${mdms.oidcproviders.cache-ttl-ms:300000}") long cacheTtlMs,
            @Value("${mdms.oidcproviders.retry-after-failure-ms:30000}") long retryAfterFailureMs) {
        this.restTemplate = restTemplate;
        this.mdmsHost = mdmsHost;
        this.mdmsEndpoint = mdmsEndpoint;
        this.moduleName = moduleName;
        this.masterName = masterName;
        this.tenantIds = parseTenantIds(tenantIdConfig);
        this.cacheTtlMs = cacheTtlMs;
        this.retryAfterFailureMs = retryAfterFailureMs;
        log.info("MDMS OIDC providers initialized with cache TTL: {} ms ({} minutes)", 
                cacheTtlMs, cacheTtlMs / 60000.0);
    }

    private static List<String> parseTenantIds(String tenantIdConfig) {
        if (tenantIdConfig == null || tenantIdConfig.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tenantIdConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthProperties.Provider> getProviders() {
        return copyOf(currentEntry());
    }

    @Override
    public List<AuthProperties.Provider> getDisabledProviders() {
        CacheEntry entry = currentEntry();
        return entry != null ? new ArrayList<>(entry.disabled) : Collections.emptyList();
    }

    private CacheEntry currentEntry() {
        CacheEntry entry = cache.get();
        if (isFresh(entry) || inFailureBackoff()) {
            return entry;
        }
        if (entry != null && !refreshLock.tryLock()) {
            return entry;
        }
        if (entry == null) {
            refreshLock.lock();
        }
        try {
            entry = cache.get();
            if (isFresh(entry) || inFailureBackoff()) {
                return entry;
            }
            List<AuthProperties.Provider> disabled = new ArrayList<>();
            List<AuthProperties.Provider> list = fetchFromMdms(disabled);
            if (list != null) {
                CacheEntry fresh = new CacheEntry(Collections.unmodifiableList(list),
                        Collections.unmodifiableList(disabled), System.currentTimeMillis());
                cache.set(fresh);
                lastFailureAt = 0L;
                return fresh;
            }
            lastFailureAt = System.currentTimeMillis();
            return entry;
        } finally {
            refreshLock.unlock();
        }
    }

    private boolean isFresh(CacheEntry entry) {
        return entry != null && (System.currentTimeMillis() - entry.timestamp) < cacheTtlMs;
    }

    private boolean inFailureBackoff() {
        return lastFailureAt != 0L && (System.currentTimeMillis() - lastFailureAt) < retryAfterFailureMs;
    }

    private static List<AuthProperties.Provider> copyOf(CacheEntry entry) {
        return entry != null ? new ArrayList<>(entry.providers) : Collections.emptyList();
    }

    /**
     * Fetches OIDC providers from MDMS. If multiple tenant IDs are configured (central instance),
     * fetches for each tenant and merges the lists (MDMS fallback applies per request).
     */
    private List<AuthProperties.Provider> fetchFromMdms(List<AuthProperties.Provider> disabledOut) {
        if (tenantIds.isEmpty()) {
            log.warn("MDMS OIDC providers: no tenant ID configured");
            return null;
        }
        List<AuthProperties.Provider> merged = new ArrayList<>();
        for (String tenantId : tenantIds) {
            List<AuthProperties.Provider> forTenant = fetchFromMdmsForTenant(tenantId, disabledOut);
            if (forTenant != null) {
                merged.addAll(forTenant);
            }
        }
        if (merged.isEmpty() && disabledOut.isEmpty()) {
            log.warn("MDMS OIDC providers: no providers loaded for any tenant");
            return null;
        }
        log.info("MDMS OIDC providers loaded: {} provider(s) across {} tenant(s)", merged.size(), tenantIds.size());
        return merged;
    }

    private List<AuthProperties.Provider> fetchFromMdmsForTenant(String tenantId, List<AuthProperties.Provider> disabledOut) {
        String url = mdmsHost + mdmsEndpoint;
        try {
            MasterDetail masterDetail = MasterDetail.builder().name(masterName).build();
            ModuleDetail moduleDetail = ModuleDetail.builder()
                    .moduleName(moduleName)
                    .masterDetails(Collections.singletonList(masterDetail))
                    .build();
            MdmsCriteria mdmsCriteria = new MdmsCriteria();
            mdmsCriteria.setTenantId(tenantId);
            mdmsCriteria.setModuleDetails(Collections.singletonList(moduleDetail));
            MdmsCriteriaReq req = new MdmsCriteriaReq();
            req.setRequestInfo(new RequestInfo());
            req.setMdmsCriteria(mdmsCriteria);

            JsonNode response = restTemplate.postForObject(url, req, JsonNode.class);
            if (response == null || !response.has(OidcConfigConstants.MDMS_RES)) {
                log.debug("MDMS OIDC providers: no MdmsRes for tenant {}", tenantId);
                return null;
            }
            JsonNode moduleNode = response.get(OidcConfigConstants.MDMS_RES).get(moduleName);
            if (moduleNode == null || !moduleNode.has(masterName)) {
                log.debug("MDMS OIDC providers: missing {}.{} for tenant {}", moduleName, masterName, tenantId);
                return null;
            }
            JsonNode masterNode = moduleNode.get(masterName);
            if (!masterNode.isArray()) {
                log.warn("MDMS OIDC providers: {} is not an array for tenant {}", masterName, tenantId);
                return null;
            }
            List<AuthProperties.Provider> providers = new ArrayList<>();
            for (JsonNode node : masterNode) {
                try {
                    AuthProperties.Provider p = mapNodeToProvider(node);
                    if (p != null && p.getId() != null && p.getIssuerUri() != null) {
                        (isProviderActive(node) ? providers : disabledOut).add(p);
                    }
                } catch (Exception e) {
                    log.warn("MDMS OIDC provider entry parse error for tenant {}: {}", tenantId, e.getMessage());
                }
            }
            return providers;
        } catch (Exception e) {
            log.error("Failed to fetch OIDC providers from MDMS for tenant {}: {}", tenantId, e.getMessage());
            return null;
        }
    }

    /** When true, provider is included; when false, provider is skipped (deactivated in MDMS). Missing = active. */
    private static boolean isProviderActive(JsonNode n) {
        if (n == null) return true;
        if (n.has(OidcConfigConstants.KEY_ACTIVE) && !n.get(OidcConfigConstants.KEY_ACTIVE).isNull())
            return n.get(OidcConfigConstants.KEY_ACTIVE).asBoolean(true);
        if (n.has(OidcConfigConstants.KEY_IS_ACTIVE) && !n.get(OidcConfigConstants.KEY_IS_ACTIVE).isNull())
            return n.get(OidcConfigConstants.KEY_IS_ACTIVE).asBoolean(true);
        return true;
    }

    private static String textOrNull(JsonNode n) {
        return n == null || n.isNull() ? null : n.asText();
    }

    private static String blankToNull(JsonNode n) {
        String text = textOrNull(n);
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AuthProperties.Provider mapNodeToProvider(JsonNode n) throws IOException {
        AuthProperties.Provider.Builder builder = AuthProperties.Provider.builder();
        
        if (n.has(OidcConfigConstants.KEY_ID)) {
            builder.id(textOrNull(n.get(OidcConfigConstants.KEY_ID)));
        }
        if (n.has(OidcConfigConstants.KEY_ISSUER_URI)) {
            builder.issuerUri(textOrNull(n.get(OidcConfigConstants.KEY_ISSUER_URI)));
        }
        if (n.has(OidcConfigConstants.KEY_ISSUER_ALIASES) && n.get(OidcConfigConstants.KEY_ISSUER_ALIASES).isArray()) {
            List<String> aliases = new ArrayList<>();
            n.get(OidcConfigConstants.KEY_ISSUER_ALIASES).forEach(a -> aliases.add(a.asText()));
            builder.issuerAliases(aliases);
        }
        if (n.has(OidcConfigConstants.KEY_JWK_SET_URI)) {
            builder.jwkSetUri(textOrNull(n.get(OidcConfigConstants.KEY_JWK_SET_URI)));
        }
        if (n.has(OidcConfigConstants.KEY_AUDIENCES) && n.get(OidcConfigConstants.KEY_AUDIENCES).isArray()) {
            List<String> aud = new ArrayList<>();
            n.get(OidcConfigConstants.KEY_AUDIENCES).forEach(a -> aud.add(a.asText()));
            builder.audiences(aud);
        }
        if (n.has(OidcConfigConstants.KEY_TENANT_ID)) {
            builder.tenantId(textOrNull(n.get(OidcConfigConstants.KEY_TENANT_ID)));
        }
        if (n.has(OidcConfigConstants.KEY_USER_TYPE)) {
            builder.userType(textOrNull(n.get(OidcConfigConstants.KEY_USER_TYPE)));
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_ROLE_CODES)) {
            builder.defaultRoleCodes(textOrNull(n.get(OidcConfigConstants.KEY_DEFAULT_ROLE_CODES)));
        }
        if (n.has(OidcConfigConstants.KEY_ROLE_CLAIM_KEY)) {
            builder.roleClaimKey(n.get(OidcConfigConstants.KEY_ROLE_CLAIM_KEY).asText(OidcConfigConstants.DEFAULT_ROLE_CLAIM_KEY));
        }
        if (n.has(OidcConfigConstants.KEY_ROLE_MAPPINGS) && n.get(OidcConfigConstants.KEY_ROLE_MAPPINGS).isArray()) {
            Map<String, String> roleMapping = new HashMap<>();
            JsonNode mappingsArray = n.get(OidcConfigConstants.KEY_ROLE_MAPPINGS);
            for (JsonNode mappingNode : mappingsArray) {
                if (mappingNode == null || !mappingNode.has(JwtConstants.KEY_SSO_ROLE)) {
                    continue;
                }
                String ssoRole = textOrNull(mappingNode.get(JwtConstants.KEY_SSO_ROLE));
                if (ssoRole == null || ssoRole.trim().isEmpty()) {
                    continue;
                }
                JsonNode digitRolesNode;
                if (mappingNode.has(JwtConstants.KEY_DIGIT_ROLES)) {
                    digitRolesNode = mappingNode.get(JwtConstants.KEY_DIGIT_ROLES);
                } else if (mappingNode.has(JwtConstants.KEY_DIGIT_ROLE)) {
                    digitRolesNode = mappingNode.get(JwtConstants.KEY_DIGIT_ROLE);
                } else {
                    continue;
                }

                String digitRolesValue;
                if (digitRolesNode.isArray()) {
                    List<String> digitRoles = new ArrayList<>();
                    digitRolesNode.forEach(drNode -> {
                        String value = textOrNull(drNode);
                        if (value != null && !value.trim().isEmpty()) {
                            digitRoles.add(value.trim());
                        }
                    });
                    if (digitRoles.isEmpty()) {
                        continue;
                    }
                    digitRolesValue = String.join(",", digitRoles);
                } else {
                    digitRolesValue = textOrNull(digitRolesNode);
                    if (digitRolesValue == null || digitRolesValue.trim().isEmpty()) {
                        continue;
                    }
                }

                roleMapping.merge(ssoRole, digitRolesValue,
                        (existing, additional) -> existing + "," + additional);
            }
            if (!roleMapping.isEmpty()) {
                builder.roleMapping(roleMapping);
            }
        } else if (n.has(OidcConfigConstants.KEY_ROLE_MAPPING)) {
            // Backward compatibility for legacy string-based roleMapping configuration
            JsonNode rm = n.get(OidcConfigConstants.KEY_ROLE_MAPPING);
            if (rm.isTextual()) {
                builder.roleMapping(ROLE_MAPPING_MAPPER.readValue(rm.asText(), new TypeReference<Map<String, String>>() {}));
            } else {
                builder.roleMapping(ROLE_MAPPING_MAPPER.readValue(rm.toString(), new TypeReference<Map<String, String>>() {}));
            }
        }
        if (n.has(OidcConfigConstants.KEY_DESIGNATION_MAPPINGS) && n.get(OidcConfigConstants.KEY_DESIGNATION_MAPPINGS).isArray()) {
            Map<String, String> designationMapping = new HashMap<>();
            JsonNode mappingsArray = n.get(OidcConfigConstants.KEY_DESIGNATION_MAPPINGS);
            for (JsonNode mappingNode : mappingsArray) {
                if (mappingNode == null || !mappingNode.has(JwtConstants.KEY_IDP_DESIGNATION)) {
                    continue;
                }
                String idpDesignation = textOrNull(mappingNode.get(JwtConstants.KEY_IDP_DESIGNATION));
                if (idpDesignation == null || idpDesignation.trim().isEmpty()) {
                    continue;
                }
                if (!mappingNode.has(JwtConstants.KEY_DIGIT_DESIGNATION_CODE)) {
                    continue;
                }
                String digitDesignationCode = textOrNull(mappingNode.get(JwtConstants.KEY_DIGIT_DESIGNATION_CODE));
                if (digitDesignationCode == null || digitDesignationCode.trim().isEmpty()) {
                    continue;
                }
                designationMapping.put(idpDesignation, digitDesignationCode);
            }
            if (!designationMapping.isEmpty()) {
                builder.designationMapping(designationMapping);
            }
        } else if (n.has(OidcConfigConstants.KEY_DESIGNATION_MAPPING)) {
            JsonNode dm = n.get(OidcConfigConstants.KEY_DESIGNATION_MAPPING);
            if (dm.isTextual()) {
                builder.designationMapping(ROLE_MAPPING_MAPPER.readValue(dm.asText(), new TypeReference<Map<String, String>>() {}));
            } else {
                builder.designationMapping(ROLE_MAPPING_MAPPER.readValue(dm.toString(), new TypeReference<Map<String, String>>() {}));
            }
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_DOB) && !n.get(OidcConfigConstants.KEY_DEFAULT_DOB).isNull()) {
            builder.defaultDob(n.get(OidcConfigConstants.KEY_DEFAULT_DOB).asLong());
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_EMPLOYEE_STATUS)) {
            builder.defaultEmployeeStatus(n.get(OidcConfigConstants.KEY_DEFAULT_EMPLOYEE_STATUS).asText(OidcConfigConstants.DEFAULT_EMPLOYED_STATUS));
        }
        if (n.has(OidcConfigConstants.KEY_ROLE_PREFIX)) {
            builder.rolePrefix(n.get(OidcConfigConstants.KEY_ROLE_PREFIX).asText(OidcConfigConstants.DEFAULT_ROLE_PREFIX));
        }
        if (n.has(OidcConfigConstants.KEY_DECRYPTION_PURPOSE)) {
            builder.decryptionPurpose(n.get(OidcConfigConstants.KEY_DECRYPTION_PURPOSE).asText(OidcConfigConstants.DEFAULT_DECRYPTION_PURPOSE));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_CLIENT_ID)) {
            builder.graphClientId(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_CLIENT_ID)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_TENANT_ID)) {
            builder.graphTenantId(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_TENANT_ID)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_METHODS_URL)) {
            builder.graphMethodsUrl(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_METHODS_URL)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_TOKEN_URL)) {
            builder.graphTokenUrl(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_TOKEN_URL)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_SCOPE)) {
            builder.graphScope(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_SCOPE)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_SERVICE_TYPE)) {
            builder.graphServiceType(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_SERVICE_TYPE)));
        }
        if (n.has(OidcConfigConstants.KEY_GRAPH_APP_RESOURCE_ID)) {
            builder.graphAppResourceId(textOrNull(n.get(OidcConfigConstants.KEY_GRAPH_APP_RESOURCE_ID)));
        }
        if (n.has(OidcConfigConstants.KEY_IDP_USER_VALIDATOR_TYPE)) {
            builder.idpUserValidatorType(textOrNull(n.get(OidcConfigConstants.KEY_IDP_USER_VALIDATOR_TYPE)));
        }
        if (n.has(OidcConfigConstants.KEY_PROVIDER_TYPE)) {
            builder.providerType(textOrNull(n.get(OidcConfigConstants.KEY_PROVIDER_TYPE)));
        }
        if (n.has(OidcConfigConstants.KEY_DESIGNATION_CLAIM_KEY)) {
            builder.designationClaimKey(textOrNull(n.get(OidcConfigConstants.KEY_DESIGNATION_CLAIM_KEY)));
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_DESIGNATION_CODE)) {
            builder.defaultDesignationCode(textOrNull(n.get(OidcConfigConstants.KEY_DEFAULT_DESIGNATION_CODE)));
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_DEPARTMENT_CODE)) {
            builder.defaultDepartmentCode(textOrNull(n.get(OidcConfigConstants.KEY_DEFAULT_DEPARTMENT_CODE)));
        }
        if (n.has(OidcConfigConstants.KEY_DEFAULT_BOUNDARY_HIERARCHY_TYPE)) {
            builder.defaultBoundaryHierarchyType(textOrNull(n.get(OidcConfigConstants.KEY_DEFAULT_BOUNDARY_HIERARCHY_TYPE)));
        }
        if (n.has(OidcConfigConstants.KEY_USERNAME_CLAIM_KEY)) {
            builder.usernameClaimKey(blankToNull(n.get(OidcConfigConstants.KEY_USERNAME_CLAIM_KEY)));
        }
        if (n.has(OidcConfigConstants.KEY_EMAIL_CLAIM_KEY)) {
            builder.emailClaimKey(blankToNull(n.get(OidcConfigConstants.KEY_EMAIL_CLAIM_KEY)));
        }
        if (n.has(OidcConfigConstants.KEY_MOBILE_NUMBER_CLAIM_KEY)) {
            builder.mobileNumberClaimKey(blankToNull(n.get(OidcConfigConstants.KEY_MOBILE_NUMBER_CLAIM_KEY)));
        }
        if (n.has(OidcConfigConstants.KEY_NAME_CLAIM_KEY)) {
            builder.nameClaimKey(blankToNull(n.get(OidcConfigConstants.KEY_NAME_CLAIM_KEY)));
        }
        if (n.has(OidcConfigConstants.KEY_JIT_ENABLED)) {
            builder.jitEnabled(n.get(OidcConfigConstants.KEY_JIT_ENABLED).asBoolean(OidcConfigConstants.DEFAULT_JIT_ENABLED));
        }

        return builder.build();
    }
}
