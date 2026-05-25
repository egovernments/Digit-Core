package org.egov.access.persistence.repository;

import static java.util.Objects.isNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.egov.access.domain.model.Action;
import org.egov.access.domain.model.ActionContainer;
import org.egov.access.domain.model.RoleAction;
import org.egov.access.util.Utils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class MdmsRepository {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.mdms.host}${egov.mdms.path}")
    private String mdmsUrl;

    @Value("${mdms.roleactionmodule.name}")
    private String roleActionModule;

    @Value("${mdms.actionstestmodule.name}")
    private String actionModule;

    @Value("${mdms.roleactionmaster.names}")
    private String roleActionMaster;

    @Value("${mdms.actiontestmaster.names}")
    private String actionMaster;

    @Value("${action.master.mdms.filter}")
    private String actionFilter;

    @Value("${cache.expiry.action.minutes}")
    private long actionsExpiryMinutes;

    @Value("${cache.expiry.role.action.minutes}")
    private long roleActionsExpiryMinutes;

    @Value("${cache.expiry.jitter.minutes:3}")
    private long jitterMinutes;

    @Value("${cache.local.entry.capacity:2000}")
    private long entryCapacity;

    // key: tenantId — raw action list (id + url only), shared across all role lookups for the tenant
    private Cache<String, List<Action>> actionsCache;

    // key: tenantId — raw role-action mappings, shared across all role lookups for the tenant
    private Cache<String, List<RoleAction>> rawRoleActionsCache;

    // key: tenantId:roleCode — ready-to-use ActionContainer for the specific role
    private Cache<String, ActionContainer> roleActionsCache;

    @PostConstruct
    public void initCaches() {
        actionsCache = new Cache2kBuilder<String, List<Action>>() {}
                .name("actions-local")
                .expiryPolicy((key, value, loadTime, oldEntry) -> jitterExpiryTime(loadTime, actionsExpiryMinutes))
                .refreshAhead(true)
                .entryCapacity(entryCapacity)
                .loader(this::loadActionsFromMdms)
                .build();

        rawRoleActionsCache = new Cache2kBuilder<String, List<RoleAction>>() {}
                .name("rawRoleActions-local")
                .expiryPolicy((key, value, loadTime, oldEntry) -> jitterExpiryTime(loadTime, roleActionsExpiryMinutes))
                .refreshAhead(true)
                .entryCapacity(entryCapacity)
                .loader(this::loadRawRoleActionsFromMdms)
                .build();

        // Loader splits the composite key and delegates to buildContainer.
        // cache2k guarantees only one loader thread per unique key — concurrent misses
        // for the same tenantId:roleCode block on the first load rather than each hitting MDMS.
        roleActionsCache = new Cache2kBuilder<String, ActionContainer>() {}
                .name("roleActions-local")
                .expiryPolicy((key, value, loadTime, oldEntry) -> jitterExpiryTime(loadTime, roleActionsExpiryMinutes))
                .refreshAhead(true)
                .entryCapacity(entryCapacity)
                .loader(key -> {
                    int sep = key.indexOf(':');
                    return buildContainer(key.substring(0, sep), key.substring(sep + 1));
                })
                .build();
    }

    @PreDestroy
    public void destroyCaches() {
        if (actionsCache != null) actionsCache.close();
        if (rawRoleActionsCache != null) rawRoleActionsCache.close();
        if (roleActionsCache != null) roleActionsCache.close();
    }

    /**
     * Returns the ActionContainer for the given tenant and role.
     * Key: tenantId:roleCode — one entry per (tenant, role) pair.
     *
     * On L1 hit: returns cached object reference (~200 ns), compiled regex patterns reused.
     * On miss: loads actions + rawRoleActions for the tenant (each fetched once regardless of
     * how many roles miss simultaneously), builds the container, and caches it.
     */
    private long jitterExpiryTime(long loadTime, long ttlMinutes) {
        long jitterMs = (long)(Math.random() * TimeUnit.MINUTES.toMillis(jitterMinutes));
        return loadTime + TimeUnit.MINUTES.toMillis(ttlMinutes) + jitterMs;
    }

    public ActionContainer fetchRoleActionData(String tenantId, String roleCode) {
        return roleActionsCache.get(tenantId + ":" + roleCode);
    }

    private ActionContainer buildContainer(String tenantId, String roleCode) {
        // actionsCache and rawRoleActionsCache loaders are also protected by cache2k:
        // concurrent misses for the same tenantId result in exactly one MDMS call each.
        List<Action> actions = actionsCache.get(tenantId);
        List<RoleAction> roleActions = rawRoleActionsCache.get(tenantId);

        Map<Long, String> actionUrlMap = actions.stream()
                .collect(Collectors.toMap(Action::getId, Action::getUrl, (a, b) -> a));

        ActionContainer container = new ActionContainer();
        for (RoleAction ra : roleActions) {
            if (!roleCode.equals(ra.getRoleCode())) continue;
            String url = actionUrlMap.get(ra.getActionId());
            if (url == null) continue;
            if (Utils.isRegexUri(url))
                container.getRegexUris().add(url);
            else
                container.getUris().add(url);
        }
        return container;
    }

    private List<Action> loadActionsFromMdms(String tenantId) {
        log.debug("Loading actions from MDMS for tenant: {}", tenantId);
        MasterDetail masterDetail = MasterDetail.builder().name(actionMaster).filter(actionFilter).build();
        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(actionModule)
                .masterDetails(Collections.singletonList(masterDetail))
                .build();

        Map<String, Map<String, List>> response = callMdms(tenantId, Collections.singletonList(moduleDetail));

        if (isNull(response.get(actionModule)) || isNull(response.get(actionModule).get(actionMaster)))
            throw new CustomException("DATA_NOT_AVAILABLE", "Actions data not available for tenant: " + tenantId);

        return Arrays.asList(objectMapper.convertValue(response.get(actionModule).get(actionMaster), Action[].class));
    }

    private List<RoleAction> loadRawRoleActionsFromMdms(String tenantId) {
        log.debug("Loading role-actions from MDMS for tenant: {}", tenantId);
        MasterDetail masterDetail = MasterDetail.builder().name(roleActionMaster).build();
        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(roleActionModule)
                .masterDetails(Collections.singletonList(masterDetail))
                .build();

        Map<String, Map<String, List>> response = callMdms(tenantId, Collections.singletonList(moduleDetail));

        if (isNull(response.get(roleActionModule)) || isNull(response.get(roleActionModule).get(roleActionMaster)))
            throw new CustomException("DATA_NOT_AVAILABLE", "RoleActions data not available for tenant: " + tenantId);

        return Arrays.asList(objectMapper.convertValue(response.get(roleActionModule).get(roleActionMaster), RoleAction[].class));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, List>> callMdms(String tenantId, List<ModuleDetail> moduleDetails) {
        MdmsCriteria mc = new MdmsCriteria();
        mc.setTenantId(tenantId);
        mc.setModuleDetails(moduleDetails);

        MdmsCriteriaReq mcq = new MdmsCriteriaReq();
        mcq.setRequestInfo(new RequestInfo());
        mcq.setMdmsCriteria(mc);

        HttpHeaders headers = new HttpHeaders();
        headers.set("tenantId", tenantId);

        return (Map<String, Map<String, List>>) restTemplate
                .postForObject(mdmsUrl, new HttpEntity<>(mcq, headers), Map.class)
                .get("MdmsRes");
    }
}
