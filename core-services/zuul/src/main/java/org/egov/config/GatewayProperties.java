package org.egov.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Getter
public class GatewayProperties {

    @Setter
    private List<String> openEndpointsWhitelist;

    @Setter
    private List<String> mixedModeEndpointsWhitelist;

    @Value("${egov.auth-service-host}")
    private String authServiceHost;

    @Value("${egov.auth-service-uri}")
    private String authServiceUri;

    @Value("${egov.authorize.access.control.host}${egov.authorize.access.control.uri}")
    private String authorizationUrl;

    @Value("${egov.open-endpoints-whitelist}")
    public void setOpenEndpointsWhitelistValues(List<String> openUrlListFromProperties) {
        this.openEndpointsWhitelist = Collections.unmodifiableList(openUrlListFromProperties);
    }

    @Value("${egov.mixed-mode-endpoints-whitelist}")
    public void setMixedModeEndpointListValues(List<String> mixModeUrlListFromProperties) {
        this.mixedModeEndpointsWhitelist = Collections.unmodifiableList(mixModeUrlListFromProperties);
    }
}
