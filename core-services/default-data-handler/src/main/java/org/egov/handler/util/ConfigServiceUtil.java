package org.egov.handler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ConfigServiceUtil {

    private final RestTemplate restTemplate;
    private final ServiceConfiguration config;
    private final ObjectMapper objectMapper;

    public ConfigServiceUtil(RestTemplate restTemplate, ServiceConfiguration config, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Copies config-service data from the default tenant to a target tenant for the given schema codes.
     */
    public void copyConfigData(RequestInfo requestInfo, String targetTenantId, List<String> schemaCodes) {
        String defaultTenantId = config.getDefaultTenantId();

        for (String schemaCode : schemaCodes) {
            log.info("Copying config data: schemaCode={}, from={}, to={}", schemaCode, defaultTenantId, targetTenantId);

            List<Map<String, Object>> records = searchConfigData(requestInfo, defaultTenantId, schemaCode);
            if (records == null || records.isEmpty()) {
                log.warn("No config data found for schemaCode={} in default tenant={}", schemaCode, defaultTenantId);
                continue;
            }

            log.info("Found {} records for schemaCode={} in default tenant", records.size(), schemaCode);

            for (Map<String, Object> record : records) {
                createConfigData(requestInfo, targetTenantId, schemaCode, record);
            }
        }
    }

    /**
     * Searches config data from config-service for a given tenant and schema code.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchConfigData(RequestInfo requestInfo, String tenantId, String schemaCode) {
        String url = config.getConfigServiceHost() + config.getConfigServiceSearchPath();

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("schemaCode", schemaCode);

        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", requestInfo);
        payload.put("criteria", criteria);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, payload, Map.class);
            if (response != null && response.get("configData") != null) {
                return (List<Map<String, Object>>) response.get("configData");
            }
        } catch (Exception e) {
            log.error("Failed to search config data: schemaCode={}, tenantId={}", schemaCode, tenantId, e);
        }
        return null;
    }

    /**
     * Creates a config data record in config-service for the target tenant.
     * Replaces the tenantId in the record and removes id/audit fields so config-service generates new ones.
     */
    @SuppressWarnings("unchecked")
    private void createConfigData(RequestInfo requestInfo, String targetTenantId, String schemaCode, Map<String, Object> record) {
        String url = config.getConfigServiceHost() + config.getConfigServiceCreatePath() + "/" + schemaCode;

        try {
            // Build configData for the target tenant
            Map<String, Object> configData = new HashMap<>();
            configData.put("tenantId", targetTenantId);
            configData.put("data", record.get("data"));

            // Preserve isActive flag
            if (record.get("isActive") != null) {
                configData.put("isActive", record.get("isActive"));
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", requestInfo);
            payload.put("configData", configData);

            restTemplate.postForObject(url, payload, Map.class);

            String uniqueId = record.get("uniqueIdentifier") != null ? record.get("uniqueIdentifier").toString() : "unknown";
            log.info("Created config data: schemaCode={}, uniqueIdentifier={}, targetTenant={}", schemaCode, uniqueId, targetTenantId);

        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            if (body.contains("DUPLICATE_RECORD")) {
                log.info("Config data already exists: schemaCode={}, targetTenant={}, skipping", schemaCode, targetTenantId);
            } else {
                log.error("Failed to create config data: schemaCode={}, targetTenant={}, error={}", schemaCode, targetTenantId, body);
            }
        } catch (Exception e) {
            log.error("Failed to create config data: schemaCode={}, targetTenant={}", schemaCode, targetTenantId, e);
        }
    }
}
