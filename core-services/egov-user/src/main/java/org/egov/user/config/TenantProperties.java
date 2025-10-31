package org.egov.user.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "state.level.tenant")
public class TenantProperties {

    private Map<String, String> ids = new HashMap<>();

    public void setIds(Map<String, List<String>> ids) {
        if(ids != null) {
            for(Map.Entry<String, List<String>> entry : ids.entrySet()) {
                for(String value : entry.getValue()) {
                    this.ids.put(value, entry.getKey());
                }
            }
        }
    }

    public String getStateLevelTenant(String tenantId, String defaultTenantId) {
        return ids.getOrDefault(tenantId, defaultTenantId);
    }
}
