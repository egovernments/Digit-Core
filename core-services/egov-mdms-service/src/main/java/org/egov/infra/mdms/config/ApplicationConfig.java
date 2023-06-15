package org.egov.infra.mdms.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@ToString
@Setter
@Getter
public class ApplicationConfig {

    @Value("${egov.mdms.schema.definition.save.topic}")
    private String saveSchemaDefinitionTopicName;

    @Value("${egov.mdms.data.save.topic}")
    private String saveMdmsDataTopicName;

}
