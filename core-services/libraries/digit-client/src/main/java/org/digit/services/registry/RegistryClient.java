package org.digit.services.registry;

import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.registry.model.RegistryCacheEntry;
import org.digit.services.registry.model.RegistryData;
import org.digit.services.registry.model.RegistryDataResponse;
import org.digit.util.HeaderStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RegistryClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final RedisTemplate<String, String> registryCacheTemplate;
    private static final ObjectMapper CACHE_MAPPER = new ObjectMapper();

    public RegistryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this(restTemplate, apiProperties, null);
    }

    public RegistryClient(RestTemplate restTemplate, ApiProperties apiProperties, RedisTemplate<String, String> registryCacheTemplate) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.registryCacheTemplate = registryCacheTemplate;
    }

    public RegistryDataResponse createRegistryData(String schemaCode, RegistryData registryData) {
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }
        try {
            log.debug("Creating registry data with schema code: {}", (Object)schemaCode);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.postForEntity(url, (Object)new HttpEntity((Object)registryData, headers), RegistryDataResponse.class, new Object[0]);
            log.debug("Successfully created registry data with schema code: {}", (Object)schemaCode);
            RegistryDataResponse result = (RegistryDataResponse) response.getBody();
            cacheAllFields(schemaCode, registryData.getData(), result);
            return result;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to create registry data: " + e.getMessage(), e);
        }
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId, boolean history) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryId == null || registryId.trim().isEmpty()) {
            throw new DigitClientException("Registry ID cannot be null or empty");
        }
        try {
            log.debug("Searching registry data with schema code: {}, registry ID: {}, history: {}", new Object[]{schemaCode, registryId, history});
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data/_registry?registryId=" + registryId + "&history=" + history;
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(new HttpHeaders()), RegistryDataResponse.class, new Object[0]);
            log.debug("Successfully retrieved registry data with schema code: {}, registry ID: {}", (Object)schemaCode, (Object)registryId);
            return (RegistryDataResponse)response.getBody();
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to retrieve registry data: " + e.getMessage(), e);
        }
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String registryId) {
        return this.searchRegistryData(schemaCode, registryId, false);
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String key, String value) {
        return this.searchRegistryData(schemaCode, key, value, null, null);
    }

    public RegistryDataResponse searchRegistryData(String schemaCode, String key, String value, Integer limit, Integer offset) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new DigitClientException("Search key cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new DigitClientException("Search value cannot be null or empty");
        }
        try {
            int actualLimit = limit != null ? limit : 5;
            int actualOffset = offset != null ? offset : 0;
            log.debug("Searching registry data with schema code: {}, key: {}, value: {}, limit: {}, offset: {}", new Object[]{schemaCode, key, value, actualLimit, actualOffset});
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data/_search";
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("contains", Map.of(key, value));
            searchRequest.put("limit", actualLimit);
            searchRequest.put("offset", actualOffset);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.POST, new HttpEntity(searchRequest, headers), RegistryDataResponse.class, new Object[0]);
            log.debug("Successfully searched registry data with schema code: {}, key: {}, value: {}", new Object[]{schemaCode, key, value});
            return (RegistryDataResponse)response.getBody();
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to search registry data: " + e.getMessage(), e);
        }
    }

    public RegistryDataResponse updateRegistryData(String schemaCode, RegistryData registryData, String key, String value) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (registryData == null) {
            throw new DigitClientException("Registry data cannot be null");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new DigitClientException("Search key cannot be null or empty");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new DigitClientException("Search value cannot be null or empty");
        }
        if (registryData.getData() == null) {
            throw new DigitClientException("Data cannot be null");
        }
        try {
            log.debug("Updating registry data with schema code: {}, key: {}, value: {}", new Object[]{schemaCode, key, value});
            RegistryCacheEntry cached = getCachedEntry(schemaCode, key, value);
            String registryId;
            Integer currentVersion;
            if (cached != null) {
                log.debug("Cache hit for registry update: schemaCode={}, key={}, value={}", schemaCode, key, value);
                registryId = cached.getRegistryId();
                currentVersion = cached.getVersion();
            } else {
                RegistryDataResponse searchResponse = this.searchRegistryData(schemaCode, key, value);
                if (searchResponse == null || searchResponse.getData() == null) {
                    throw new DigitClientException("Registry data not found for key: " + key + " and value: " + value);
                }
                currentVersion = this.extractVersionFromResponse(searchResponse);
                if (currentVersion == null) {
                    throw new DigitClientException("Could not extract version from existing registry data");
                }
                registryId = this.extractRegistryIdFromResponse(searchResponse);
                if (registryId == null || registryId.trim().isEmpty()) {
                    throw new DigitClientException("Could not extract registry ID from existing registry data");
                }
            }
            registryData.setVersion(currentVersion);
            String url = this.apiProperties.getRegistryServiceUrl() + "/registry/v3/" + schemaCode + "/data?id=" + registryId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            ResponseEntity response = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity((Object)registryData, headers), RegistryDataResponse.class, new Object[0]);
            log.debug("Successfully updated registry data with schema code: {}, key: {}, value: {}", new Object[]{schemaCode, key, value});
            RegistryDataResponse updateResult = (RegistryDataResponse) response.getBody();
            Integer updatedVersion = extractVersionFromResponse(updateResult);
            if (updatedVersion != null) {
                cacheEntry(schemaCode, HeaderStore.extractTenantId(), key, value, registryId, updatedVersion);
            }
            return updateResult;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("Failed to update registry data: " + e.getMessage(), e);
        }
    }

    private void cacheAllFields(String schemaCode, JsonNode dataNode, RegistryDataResponse response) {
        if (registryCacheTemplate == null || response == null) return;
        String registryId = extractRegistryIdFromResponse(response);
        Integer version = extractVersionFromResponse(response);
        if (registryId == null || version == null || dataNode == null || !dataNode.isObject()) return;
        String tenantId = HeaderStore.extractTenantId();
        Iterator<Map.Entry<String, JsonNode>> fields = dataNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isValueNode()) {
                cacheEntry(schemaCode, tenantId, field.getKey(), field.getValue().asText(), registryId, version);
            }
        }
    }

    private void cacheEntry(String schemaCode, String tenantId, String key, String value, String registryId, Integer version) {
        if (registryCacheTemplate == null) return;
        try {
            String cacheKey = "registry:" + schemaCode + ":" + (tenantId != null ? tenantId : "default") + ":" + key + ":" + value;
            String json = CACHE_MAPPER.writeValueAsString(new RegistryCacheEntry(registryId, version));
            registryCacheTemplate.opsForValue().set(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to write registry cache entry: {}", (Object) e.getMessage());
        }
    }

    private RegistryCacheEntry getCachedEntry(String schemaCode, String key, String value) {
        if (registryCacheTemplate == null) return null;
        try {
            String tenantId = HeaderStore.extractTenantId();
            String cacheKey = "registry:" + schemaCode + ":" + (tenantId != null ? tenantId : "default") + ":" + key + ":" + value;
            String json = registryCacheTemplate.opsForValue().get(cacheKey);
            if (json != null) return CACHE_MAPPER.readValue(json, RegistryCacheEntry.class);
        } catch (Exception e) {
            log.warn("Failed to read registry cache entry: {}", (Object) e.getMessage());
        }
        return null;
    }

    private Integer extractVersionFromResponse(RegistryDataResponse response) {
        try {
            List dataList;
            if (response.getData() instanceof Map) {
                Map dataMap = (Map)response.getData();
                Object versionObj = dataMap.get("version");
                if (versionObj instanceof Integer) {
                    return (Integer)versionObj;
                }
                if (versionObj instanceof Number) {
                    return ((Number)versionObj).intValue();
                }
            } else if (response.getData() instanceof List && !(dataList = (List)response.getData()).isEmpty()) {
                Object versionObj = ((Map)dataList.get(0)).get("version");
                if (versionObj instanceof Integer) {
                    return (Integer)versionObj;
                }
                if (versionObj instanceof Number) {
                    return ((Number)versionObj).intValue();
                }
            }
            return null;
        }
        catch (Exception e) {
            log.warn("Error extracting version from response: {}", (Object)e.getMessage());
            return null;
        }
    }

    private String extractRegistryIdFromResponse(RegistryDataResponse response) {
        try {
            List dataList;
            if (response.getData() instanceof Map) {
                Map dataMap = (Map)response.getData();
                Object idObj = dataMap.get("registryId");
                if (idObj instanceof String) {
                    return (String)idObj;
                }
                if (idObj != null) {
                    return idObj.toString();
                }
            } else if (response.getData() instanceof List && !(dataList = (List)response.getData()).isEmpty()) {
                Object idObj = ((Map)dataList.get(0)).get("registryId");
                if (idObj instanceof String) {
                    return (String)idObj;
                }
                if (idObj != null) {
                    return idObj.toString();
                }
            }
            return null;
        }
        catch (Exception e) {
            log.warn("Error extracting registry ID from response: {}", (Object)e.getMessage());
            return null;
        }
    }
}