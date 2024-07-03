package org.egov.infra.mdms.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ToString
@Setter
@Getter
@Import({MultiStateInstanceUtil.class})
public class ApplicationConfig {

    @Value("${egov.mdms.schema.definition.save.topic}")
    private String saveSchemaDefinitionTopicName;

    @Value("${egov.mdms.data.save.topic}")
    private String saveMdmsDataTopicName;

    @Value("${egov.create.tenant.index}")
    private String createTenantIndex;

    @Value("${egov.update.tenant.index}")
    private String updateTenantIndex;

    @Value("${egov.create.rate.index}")
    private String createRateIndex;

    @Value("${egov.update.rate.index}")
    private String updateRateIndex;

    @Value("${egov.mdms.data.update.topic}")
    private String updateMdmsDataTopicName;

    @Value("${mdms.default.offset}")
    private Integer defaultOffset;

    @Value("${mdms.default.limit}")
    private Integer defaultLimit;



}
