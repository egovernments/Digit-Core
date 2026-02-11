package org.egov.user.config;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
@ConditionalOnProperty(name = "auth.oidc.providers-source", havingValue = "mdms")
public class MdmsOidcProviderSupplier implements OidcProviderSupplier {

    private final RestTemplate restTemplate;
    private final String mdmsHost;
    private final String mdmsEndpoint;
    private final String moduleName;
    private final String masterName;
    /** Comma-separated tenant IDs for central instance; single tenant otherwise. */
    private final List<String> tenantIds;

    private final AtomicReference<List<AuthProperties.Provider>> cache = new AtomicReference<>(null);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private volatile long lastFetchTime = 0;

    public MdmsOidcProviderSupplier(
            RestTemplate restTemplate,
            @Value("${egov.mdms.host}") String mdmsHost,
            @Value("${egov.mdms.search.endpoint}") String mdmsEndpoint,
            @Value("${mdms.oidcproviders.moduleName}") String moduleName,
            @Value("${mdms.oidcproviders.masterName}") String masterName,
            @Value("${mdms.oidcproviders.tenantId}") String tenantIdConfig) {
        this.restTemplate = restTemplate;
        this.mdmsHost = mdmsHost;
        this.mdmsEndpoint = mdmsEndpoint;
        this.moduleName = moduleName;
        this.masterName = masterName;
        this.tenantIds = parseTenantIds(tenantIdConfig);
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
        long now = System.currentTimeMillis();
        if (cache.get() != null && (now - lastFetchTime) < CACHE_TTL_MS) {
            return cache.get();
        }
        List<AuthProperties.Provider> list = fetchFromMdms();
        if (list != null) {
            cache.set(list);
            lastFetchTime = now;
            return list;
        }
        // Keep previous cache on failure
        List<AuthProperties.Provider> existing = cache.get();
        return existing != null ? existing : Collections.emptyList();
    }

    /**
     * Fetches OIDC providers from MDMS. If multiple tenant IDs are configured (central instance),
     * fetches for each tenant and merges the lists (MDMS fallback applies per request).
     */
    private List<AuthProperties.Provider> fetchFromMdms() {
        if (tenantIds.isEmpty()) {
            log.warn("MDMS OIDC providers: no tenant ID configured");
            return null;
        }
        List<AuthProperties.Provider> merged = new ArrayList<>();
        for (String tenantId : tenantIds) {
            List<AuthProperties.Provider> forTenant = fetchFromMdmsForTenant(tenantId);
            if (forTenant != null) {
                merged.addAll(forTenant);
            }
        }
        if (merged.isEmpty()) {
            log.warn("MDMS OIDC providers: no providers loaded for any tenant");
            return null;
        }
        log.info("MDMS OIDC providers loaded: {} provider(s) across {} tenant(s)", merged.size(), tenantIds.size());
        return merged;
    }

    private List<AuthProperties.Provider> fetchFromMdmsForTenant(String tenantId) {
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
            if (response == null || !response.has("MdmsRes")) {
                log.debug("MDMS OIDC providers: no MdmsRes for tenant {}", tenantId);
                return null;
            }
            JsonNode moduleNode = response.get("MdmsRes").get(moduleName);
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
                    if (!isProviderActive(node)) {
                        continue;
                    }
                    AuthProperties.Provider p = mapNodeToProvider(node);
                    if (p != null && p.getId() != null && p.getIssuerUri() != null) {
                        providers.add(p);
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
        if (n.has("active") && !n.get("active").isNull()) return n.get("active").asBoolean(true);
        if (n.has("isActive") && !n.get("isActive").isNull()) return n.get("isActive").asBoolean(true);
        return true;
    }

    private static String textOrNull(JsonNode n) {
        return n == null || n.isNull() ? null : n.asText();
    }

    private AuthProperties.Provider mapNodeToProvider(JsonNode n) throws Exception {
        AuthProperties.Provider p = new AuthProperties.Provider();
        if (n.has("id")) p.setId(textOrNull(n.get("id")));
        if (n.has("issuerUri")) p.setIssuerUri(textOrNull(n.get("issuerUri")));
        if (n.has("issuerAliases") && n.get("issuerAliases").isArray()) {
            List<String> aliases = new ArrayList<>();
            n.get("issuerAliases").forEach(a -> aliases.add(a.asText()));
            p.setIssuerAliases(aliases);
        }
        if (n.has("jwkSetUri")) p.setJwkSetUri(textOrNull(n.get("jwkSetUri")));
        if (n.has("audiences") && n.get("audiences").isArray()) {
            List<String> aud = new ArrayList<>();
            n.get("audiences").forEach(a -> aud.add(a.asText()));
            p.setAudiences(aud);
        }
        if (n.has("tenantId")) p.setTenantId(textOrNull(n.get("tenantId")));
        if (n.has("userType")) p.setUserType(n.has("userType") ? n.get("userType").asText() : "EMPLOYEE");
        if (n.has("employeeType")) p.setEmployeeType(n.has("employeeType") ? n.get("employeeType").asText() : "PERMANENT");
        if (n.has("defaultRoleCodes")) p.setDefaultRoleCodes(textOrNull(n.get("defaultRoleCodes")));
        if (n.has("roleClaimKey")) p.setRoleClaimKey(n.get("roleClaimKey").asText("roles"));
        if (n.has("roleMapping")) {
            JsonNode rm = n.get("roleMapping");
            p.setRoleMapping(rm.isTextual() ? rm.asText() : rm.toString());
        }
        if (n.has("roleBoundaryMapping")) {
            JsonNode rbm = n.get("roleBoundaryMapping");
            p.setRoleBoundaryMapping(rbm.isTextual() ? rbm.asText() : rbm.toString());
        }
        if (n.has("defaultBoundaryCode")) p.setDefaultBoundaryCode(textOrNull(n.get("defaultBoundaryCode")));
        if (n.has("hierarchyType")) p.setHierarchyType(textOrNull(n.get("hierarchyType")));
        if (n.has("projectName")) p.setProjectName(textOrNull(n.get("projectName")));
        if (n.has("defaultPassword")) p.setDefaultPassword(textOrNull(n.get("defaultPassword")));
        if (n.has("defaultDob") && !n.get("defaultDob").isNull()) p.setDefaultDob(n.get("defaultDob").asLong());
        if (n.has("defaultDesignation")) p.setDefaultDesignation(textOrNull(n.get("defaultDesignation")));
        if (n.has("defaultDepartment")) p.setDefaultDepartment(textOrNull(n.get("defaultDepartment")));
        if (n.has("mobileNumberPrefix")) p.setMobileNumberPrefix(textOrNull(n.get("mobileNumberPrefix")));
        if (n.has("mobileNumberLength") && !n.get("mobileNumberLength").isNull()) p.setMobileNumberLength(n.get("mobileNumberLength").asInt());
        if (n.has("defaultEmployeeStatus")) p.setDefaultEmployeeStatus(n.get("defaultEmployeeStatus").asText("EMPLOYED"));
        if (n.has("rolePrefix")) p.setRolePrefix(n.get("rolePrefix").asText("ROLE_"));
        if (n.has("employeeUsernameFormat")) p.setEmployeeUsernameFormat(textOrNull(n.get("employeeUsernameFormat")));
        if (n.has("employeeUsernameProviderKey")) p.setEmployeeUsernameProviderKey(textOrNull(n.get("employeeUsernameProviderKey")));
        if (n.has("employeeUsernameNumberLength") && !n.get("employeeUsernameNumberLength").isNull())
            p.setEmployeeUsernameNumberLength(n.get("employeeUsernameNumberLength").asInt());
        if (n.has("decryptionPurpose")) p.setDecryptionPurpose(n.get("decryptionPurpose").asText("UserSelf"));
        if (n.has("graphClientId")) p.setGraphClientId(textOrNull(n.get("graphClientId")));
        if (n.has("graphClientSecret")) p.setGraphClientSecret(textOrNull(n.get("graphClientSecret")));
        if (n.has("graphTenantId")) p.setGraphTenantId(textOrNull(n.get("graphTenantId")));
        if (n.has("graphMethodsUrl")) p.setGraphMethodsUrl(textOrNull(n.get("graphMethodsUrl")));
        if (n.has("graphTokenUrl")) p.setGraphTokenUrl(textOrNull(n.get("graphTokenUrl")));
        if (n.has("graphScope")) p.setGraphScope(textOrNull(n.get("graphScope")));
        return p;
    }
}
