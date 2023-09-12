package org.egov.elasticrequestcrypt.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ApplicationProperties {

    @Value("${es.host}")
    private String esHost;

    @Value("${index.name}")
    private String correlationIndexName;

    @Value("${index.type}")
    private String correlationIndexType;

    @Value("${enc.host}")
    private String encHost;

    @Value("${enc.context.path}")
    private String encContextPath;

    @Value("${enc.endpoint}")
    private String encEndpoint;

    @Value("${enc.root.tenantid}")
    private String encRootTenantId;


}
