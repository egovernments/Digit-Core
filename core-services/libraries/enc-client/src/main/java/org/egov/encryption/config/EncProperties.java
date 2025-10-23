package org.egov.encryption.config;


import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Configuration
@PropertySource("classpath:enc.properties")
@EnableConfigurationProperties(EncTenantSpecificProperties.class)
public class EncProperties {

    @Value("${kafka.topic.audit}")
    private String auditTopicName;

    @Value("${egov.mdms.host}")
    private String egovMdmsHost;
    
    @Value("${egov.mdms.search.endpoint}")
    private String egovMdmsSearchEndpoint;

    @Value("#{'${egov.enc.state.level.tenant.ids}'.split(',')}")
    private List<String> stateLevelTenantIds;

    @Value("${default.encrypt.data.type}")
    private String defaultEncryptDataType;

    @Value("${egov.enc.host}")
    private String egovEncHost;

    @Value("${egov.enc.encrypt.endpoint}")
    private String egovEncEncryptPath;
    
    @Value("${egov.enc.decrypt.endpoint}")
    private String egovEncDecryptPath;
}
