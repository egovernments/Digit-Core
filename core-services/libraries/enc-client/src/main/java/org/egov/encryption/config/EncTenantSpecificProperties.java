package org.egov.encryption.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "egov.enc.tenant")
public class EncTenantSpecificProperties {

    private boolean useDefaultValues;

    private Map<String, String> host = new HashMap<>();
    private Map<String, String> defaultEncryptDataType = new HashMap<>();
    private Map<String, String> encryptEndpoint = new HashMap<>();
    private Map<String, String> decryptEndpoint = new HashMap<>();
    private Map<String, String> auditTopicName = new HashMap<>();
    private Map<String, String> mdmsHost = new HashMap<>();
    private Map<String, String> mdmsSearchEndpoint = new HashMap<>();

    private String getValue(Map<String, String> map, String tenantId, String defaultValue) {
        if (useDefaultValues) {
            return map.getOrDefault(tenantId, defaultValue);
        }
        return map.get(tenantId);
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
