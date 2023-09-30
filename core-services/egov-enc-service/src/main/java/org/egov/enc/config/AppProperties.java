package org.egov.enc.config;


import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Getter
@ToString
@Configuration
@Component
@PropertySource("classpath:application.properties")
public class AppProperties {

    @Value("${size.key.symmetric}")
    private int symmetricKeySize;

    @Value("${size.initialvector}")
    private int initialVectorSize;

    @Value("${size.key.asymmetric}")
    private int asymmetricKeySize;

    @Value("${length.keyid}")
    private Integer keyIdLength;

    @Value("${method.symmetric}")
    private String symmetricMethod;

    @Value("${method.asymmetric}")
    private String asymmetricMethod;

    @Value("${method.signature}")
    private String signatureMathod;

    @Value("#{${type.to.method.map}}")
    private HashMap<String, String> typeToMethodMap;

    @Value(("${egov.state.level.tenant.id:default}"))
    private String stateLevelTenantId;

}
