package com.example.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
@ToString
@Setter
@Getter
public class ApplicationProperties {

    @Value("${egov.auth-service-host}")
    private  String authServiceHost;

    @Value("${egov.auth-service-uri}")
    private  String authUri;

    @Value("${egov.auth-service-host}${egov.user.search.path}")
    private String userSearchURI;

    private List<String> encryptedUrlSet;

    private List<String> openEndpointsWhitelist;

    private List<String> mixedModeEndpointsWhitelist;
    @Value("${egov.encrypted-endpoints-list}")
    public void setEncryptedUrlListValues(List<String> encryptedListFromProperties) {
        this.encryptedUrlSet = Collections.unmodifiableList(encryptedListFromProperties);
    }

    @Value("${egov.open-endpoints-whitelist}")
    public void setOpenEndpointsWhitelistValues(List<String> openUrlListFromProperties) {
        this.openEndpointsWhitelist = Collections.unmodifiableList(openUrlListFromProperties);
    }

    @Value("${egov.mixed-mode-endpoints-whitelist}")
    public void setMixModeEndpointListValues(List<String> mixModeUrlListFromProperties) {
        this.mixedModeEndpointsWhitelist = Collections.unmodifiableList(mixModeUrlListFromProperties);
    }


}
