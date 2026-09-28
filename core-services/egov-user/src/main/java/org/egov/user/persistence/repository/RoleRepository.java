package org.egov.user.persistence.repository;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.Role;
import org.egov.user.repository.builder.RoleQueryBuilder;
import org.egov.user.repository.rowmapper.RoleRowMapper;
import org.egov.user.repository.rowmapper.UserRoleRowMapper;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@Setter
public class RoleRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private DatabaseSchemaUtils databaseSchemaUtils;


    @Value("${mdms.roles.masterName}")
    private String roleMasterName;

    @Value("${mdms.roles.moduleName}")
    private String roleModuleName;

    @Value("${mdms.host}")
    private String host;

    @Value("${mdms.path}")
    private String path;

    @Value("${mdms.roles.cache-ttl-ms:300000}")
    private long rolesCacheTtlMs;

    @Value("${mdms.roles.retry-after-failure-ms:30000}")
    private long rolesRetryAfterFailureMs;

    private final Map<String, CachedRoles> rolesCache = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> rolesLocks = new ConcurrentHashMap<>();

    /**
     * Constructs an instance of RoleRepository with the specified dependencies.
     *
     * @param namedParameterJdbcTemplate the NamedParameterJdbcTemplate instance used for database operations
     * @param restTemplate the RestTemplate instance used for making REST calls
     * @param objectMapper the ObjectMapper instance used for JSON processing
     * @param databaseSchemaUtils the DatabaseSchemaUtils instance used for database schema utilities
     */
    public RoleRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, RestTemplate restTemplate,
                          ObjectMapper objectMapper, DatabaseSchemaUtils databaseSchemaUtils) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.databaseSchemaUtils = databaseSchemaUtils;
    }

    /**
     * Get UserRoles By UserId And TenantId
     *
     * @param userId
     * @param tenantId
     * @return
     */
    public List<Role> getUserRoles(final long userId, final String tenantId) {

        final Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("userId", userId);
        parametersMap.put("tenantId", tenantId);
        String query = RoleQueryBuilder.GET_ROLES_BY_ID_TENANTID;
        // replaced schema placeholder with tenant specific schema name
        query = databaseSchemaUtils.replaceSchemaPlaceholder(query, tenantId);
        List<Role> roleList = namedParameterJdbcTemplate.query(query, parametersMap,
                new UserRoleRowMapper());
        List<Long> roleIdList = new ArrayList<Long>();
        String tenantid = null;
        if (!roleList.isEmpty()) {
            for (Role role : roleList) {
                tenantid = role.getTenantId();
            }
        }
        List<Role> roles = new ArrayList<Role>();
        if (!roleIdList.isEmpty()) {

            final Map<String, Object> Map = new HashMap<String, Object>();
            Map.put("id", roleIdList);
            Map.put("tenantId", tenantid);
            // replaced schema placeholder with tenant specific schema name
            query = databaseSchemaUtils.replaceSchemaPlaceholder(RoleQueryBuilder.GET_ROLES_BY_ROLEIDS, tenantId);
            roles = namedParameterJdbcTemplate.query(query, Map, new RoleRowMapper());
        }

        return roles;
    }

    /**
     * Get Role By role code and tenantId
     *
     * @param tenantId
     * @param code
     * @return
     */
    public Role findByTenantIdAndCode(String tenantId, String code) {

        final Map<String, Object> parametersMap = new HashMap<String, Object>();
        parametersMap.put("code", code);
        parametersMap.put("tenantId", tenantId);
        Role role = null;
        // replaced schema placeholder with tenant specific schema name
        String query = databaseSchemaUtils.replaceSchemaPlaceholder(RoleQueryBuilder.GET_ROLE_BYTENANT_ANDCODE, tenantId);
        List<Role> roleList = namedParameterJdbcTemplate
                .query(query, parametersMap, new RoleRowMapper());

        if (!roleList.isEmpty()) {
            role = roleList.get(0);
        }
        return role;
    }

    Set<Role> findRolesByCode(Set<String> roles, String tenantId) {
        Map<String, JsonNode> rolesByCode = cachedRoles(tenantId, roles.contains(UserServiceConstants.CITIZEN_ROLE_CODE));
        Set<Role> validatedRoles = new HashSet<>();
        for (String code : roles) {
            JsonNode node = rolesByCode.get(code);
            if (node == null) {
                continue;
            }
            try {
                validatedRoles.add(objectMapper.treeToValue(node, Role.class));
            } catch (JsonProcessingException e) {
                log.error("Failed to fetch roles from MDMS", e);
                throw new CustomException("MDMS_ROLE_FETCH_FAILED", "Unable to fetch roles from MDMS");
            }
        }
        return validatedRoles;
    }

    private Map<String, JsonNode> cachedRoles(String tenantId, boolean withTenantHeader) {
        String key = tenantId + (withTenantHeader ? "|tenant-header" : "");
        CachedRoles cached = rolesCache.get(key);
        if (cached != null && cached.isFresh()) {
            return cached.rolesByCode;
        }
        ReentrantLock lock = rolesLocks.computeIfAbsent(key, k -> new ReentrantLock());
        if (cached != null && !lock.tryLock()) {
            return cached.rolesByCode;
        }
        if (cached == null) {
            lock.lock();
        }
        try {
            CachedRoles current = rolesCache.get(key);
            if (current != null && current.isFresh()) {
                return current.rolesByCode;
            }
            Map<String, JsonNode> fetched;
            try {
                fetched = fetchAllRoles(tenantId, withTenantHeader);
            } catch (RuntimeException e) {
                if (current == null) {
                    throw e;
                }
                log.warn("MDMS roles refresh failed for tenant {}, serving cached roles", tenantId, e);
                rolesCache.put(key, new CachedRoles(current.rolesByCode, rolesRetryAfterFailureMs));
                return current.rolesByCode;
            }
            if (fetched.isEmpty() && current != null) {
                log.warn("MDMS returned no roles for tenant {}, serving cached roles", tenantId);
                rolesCache.put(key, new CachedRoles(current.rolesByCode, rolesRetryAfterFailureMs));
                return current.rolesByCode;
            }
            if (!fetched.isEmpty()) {
                rolesCache.put(key, new CachedRoles(fetched, rolesCacheTtlMs));
            }
            return fetched;
        } finally {
            lock.unlock();
        }
    }

    private Map<String, JsonNode> fetchAllRoles(String tenantId, boolean withTenantHeader) {
        MasterDetail rolesMasterDetail = MasterDetail.builder().name(roleMasterName).build();
        MdmsCriteria mc = new MdmsCriteria();
        mc.setTenantId(tenantId);
        mc.setModuleDetails(Collections.singletonList(ModuleDetail.builder().moduleName(roleModuleName)
                .masterDetails(Collections.singletonList(rolesMasterDetail)).build()));
        MdmsCriteriaReq mcq = new MdmsCriteriaReq();
        mcq.setRequestInfo(new RequestInfo());
        mcq.setMdmsCriteria(mc);

        HttpHeaders headers = new HttpHeaders();
        if (withTenantHeader)
            headers.set("tenantId", tenantId);

        JsonNode response = restTemplate.postForObject(host + path, new HttpEntity<>(mcq, headers), JsonNode.class)
                .findValue(roleMasterName);
        Map<String, JsonNode> rolesByCode = new HashMap<>();
        if (!isNull(response) && response.isArray()) {
            for (JsonNode node : response) {
                if (node.hasNonNull("code")) {
                    rolesByCode.put(node.get("code").asText(), node);
                }
            }
        }
        log.info("MDMS roles cached for tenant {}: {} role(s)", tenantId, rolesByCode.size());
        return Collections.unmodifiableMap(rolesByCode);
    }

    private static final class CachedRoles {
        private final Map<String, JsonNode> rolesByCode;
        private final long expiresAt;

        private CachedRoles(Map<String, JsonNode> rolesByCode, long ttlMs) {
            this.rolesByCode = rolesByCode;
            this.expiresAt = System.currentTimeMillis() + ttlMs;
        }

        private boolean isFresh() {
            return System.currentTimeMillis() < expiresAt;
        }
    }
}
