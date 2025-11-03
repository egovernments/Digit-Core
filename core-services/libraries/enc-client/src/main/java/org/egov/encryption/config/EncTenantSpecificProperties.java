package org.egov.encryption.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Component
@ConfigurationProperties(prefix = "egov.enc.tenant")
public class EncTenantSpecificProperties {

    @Setter
    private boolean useDefaultValues;

    private Map<String, String> host = new HashMap<>();
    private Map<String, String> defaultEncryptDataType = new HashMap<>();
    private Map<String, String> encryptEndpoint = new HashMap<>();
    private Map<String, String> decryptEndpoint = new HashMap<>();
    private Map<String, String> auditTopicName = new HashMap<>();
    private Map<String, String> mdmsHost = new HashMap<>();
    private Map<String, String> mdmsSearchEndpoint = new HashMap<>();

    // Normalize keys to lowercase in all setters
    public void setHost(Map<String, String> host) {
        this.host = normalizeKeys(host);
    }

    public void setDefaultEncryptDataType(Map<String, String> map) {
        this.defaultEncryptDataType = normalizeKeys(map);
    }

    public void setEncryptEndpoint(Map<String, String> map) {
        this.encryptEndpoint = normalizeKeys(map);
    }

    public void setDecryptEndpoint(Map<String, String> map) {
        this.decryptEndpoint = normalizeKeys(map);
    }

    public void setAuditTopicName(Map<String, String> map) {
        this.auditTopicName = normalizeKeys(map);
    }

    public void setMdmsHost(Map<String, String> map) {
        this.mdmsHost = normalizeKeys(map);
    }

    public void setMdmsSearchEndpoint(Map<String, String> map) {
        this.mdmsSearchEndpoint = normalizeKeys(map);
    }

    private Map<String, String> normalizeKeys(Map<String, String> map) {
        if (map == null) return new HashMap<>();
        Map<String, String> normalized = new HashMap<>();
        map.forEach((k, v) -> normalized.put(k.toLowerCase(Locale.ROOT), v));
        return normalized;
    }

    private String getValue(Map<String, String> map, String tenantId, String defaultValue) {
        if (tenantId == null) return defaultValue;
        String key = tenantId.toLowerCase(Locale.ROOT);
        if (useDefaultValues) {
            return map.getOrDefault(key, defaultValue);
        }
        return map.get(key);
    }

    public String getHost(String tenantId, String defaultValue) {
        return getValue(host, tenantId, defaultValue);
    }

    public String getDefaultEncryptDataType(String tenantId, String defaultValue) {
        return getValue(defaultEncryptDataType, tenantId, defaultValue);
    }

    public String getEncryptEndpoint(String tenantId, String defaultValue) {
        return getValue(encryptEndpoint, tenantId, defaultValue);
    }

    public String getDecryptEndpoint(String tenantId, String defaultValue) {
        return getValue(decryptEndpoint, tenantId, defaultValue);
    }

    public String getAuditTopicName(String tenantId, String defaultValue) {
        return getValue(auditTopicName, tenantId, defaultValue);
    }

    public String getMdmsHost(String tenantId, String defaultValue) {
        return getValue(mdmsHost, tenantId, defaultValue);
    }

    public String getMdmsSearchEndpoint(String tenantId, String defaultValue) {
        return getValue(mdmsSearchEndpoint, tenantId, defaultValue);
    }

}
