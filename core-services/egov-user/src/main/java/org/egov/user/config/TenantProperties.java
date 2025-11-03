package org.egov.user.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Data
@Component
@ConfigurationProperties(prefix = "state.level.tenant.tenant.ids")
public class TenantProperties {

    private Map<String, String> mappings = new HashMap<>();

    public void setMappings(Map<String, List<String>> mappings) {
        this.mappings = new HashMap<>();
        if (mappings != null) {
            for (Map.Entry<String, List<String>> entry : mappings.entrySet()) {
                String stateKey = entry.getKey().toLowerCase(Locale.ROOT);
                for (String value : entry.getValue()) {
                    if (value != null) {
                        this.mappings.put(value.toLowerCase(Locale.ROOT), stateKey);
                    }
                }
            }
        }
    }

    public String getStateLevelTenant(String tenantId, String defaultTenantId) {
        if (tenantId == null) {
            return defaultTenantId;
        }
        return mappings.getOrDefault(tenantId.toLowerCase(Locale.ROOT), defaultTenantId);
    }
}
