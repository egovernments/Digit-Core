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
@ConfigurationProperties(prefix = "state.level.tenant")
public class TenantProperties {

    private Map<String, String> ids = new HashMap<>();

    public void setIds(Map<String, List<String>> ids) {
        if (ids != null) {
            for (Map.Entry<String, List<String>> entry : ids.entrySet()) {
                String stateKey = entry.getKey().toLowerCase(Locale.ROOT);
                for (String value : entry.getValue()) {
                    if (value != null) {
                        this.ids.put(value.toLowerCase(Locale.ROOT), stateKey);
                    }
                }
            }
        }
    }

    public String getStateLevelTenant(String tenantId, String defaultTenantId) {
        if (tenantId == null) {
            return defaultTenantId;
        }
        return ids.getOrDefault(tenantId.toLowerCase(Locale.ROOT), defaultTenantId);
    }
}
