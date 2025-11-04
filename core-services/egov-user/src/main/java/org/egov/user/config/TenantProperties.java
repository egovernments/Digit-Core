package org.egov.user.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tenant.state.level.tenant.id")
public class TenantProperties {

    private Map<String, String> mappings;

    public String getStateLevelTenant(String tenantId, String defaultTenantId) {
        if (tenantId == null) {
            return defaultTenantId;
        }
        return mappings.getOrDefault(tenantId.toLowerCase(Locale.ROOT), defaultTenantId);
    }
}
