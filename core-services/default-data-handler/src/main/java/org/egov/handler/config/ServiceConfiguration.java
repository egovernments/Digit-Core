package org.egov.handler.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ServiceConfiguration {

    @Value("${kafka.topics.create.tenant}")
    private String createTopic;

    //MDMS Configs
    @Value("${egov.mdms.host}${egov.mdms.default.data.create.endpoint}")
    private String mdmsDefaultDataCreateURI;

    @Value("${egov.mdms.host}${egov.mdms.schema.create.endpoint}")
    private String mdmsSchemaCreateURI;

    @Value("${egov.mdms.host}${egov.mdms.schema.search.endpoint}")
    private String mdmsSchemaSearchURI;

    @Value("${egov.mdms.host}${egov.mdms.data.create.endpoint}")
    private String mdmsDataCreateURI;

    @Value("${egov.mdms.host}${egov.mdms.data.search.endpoint}")
    private String mdmsDataSearchURI;

    @Value("#{'${default.mdms.schema.create.list}'.split(',')}")
    private List<String> defaultMdmsSchemaList;

    @Value("#{${mdms.schemacode.map}}")
    private Map<String, List<String>> mdmsSchemacodeMap;

    //Localization Configs
    @Value("${egov.localization.host}${egov.localization.default.data.create.endpoint}")
    private String localizationDefaultDataCreateURI;

    @Value("${egov.localization.host}${egov.localization.upsert.path}")
    private String upsertLocalizationURI;

    @Value("#{'${default.localization.locale.list}'.split(',')}")
    private List<String> defaultLocalizationLocaleList;

    @Value("#{'${default.localization.module.create.list}'.split(',')}")
    private List<String> defaultLocalizationModuleList;

    @Value("${tenant.localization.module}")
    private String tenantLocalizationModule;

    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    // User OTP Configuration
    @Value("${egov.user.otp.host}")
    private String userOtpHost;

    @Value("${egov.user.otp.send.endpoint}")
    private String userOtpSendEndpoint;

    // Tenant Management Configuration
    @Value("${egov.tenant.management.host}${egov.tenant.management.context.path}${egov.tenant.management.config.create.path}")
    private String tenantConfigCreateURI;

    @Value("${egov.tenant.management.host}${egov.tenant.management.context.path}${egov.tenant.management.config.search.path}")
    private String tenantConfigSearchURI;

    // Default Tenant Id
    @Value("${default.tenant.id}")
    private String defaultTenantId;

    // Workflow Configuration
    @Value("${egov.workflow.host}${egov.workflow.businessservice.create.path}")
    private String wfBusinessServiceCreateURI;

    // HRMS configuration
    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.path}")
    private String hrmsCreatePath;

    // Elastic Search Configuration
    @Value("${egov.indexer.es.username}")
    private String EsUsername;

    @Value("${egov.indexer.es.password}")
    private String EsPassword;

    @Value("${egov.infra.indexer.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private int elasticsearchPort;

    @Value("${egov.bulk.index.path}")
    private String bulkIndexPath;

    @Value("${topic.notification.mail}")
    private String emailTopic;
}
