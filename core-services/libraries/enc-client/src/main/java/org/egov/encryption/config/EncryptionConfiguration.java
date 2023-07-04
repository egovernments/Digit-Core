package org.egov.encryption.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {"org.egov.encryption"})
@Import({MultiStateInstanceUtil.class})
public class EncryptionConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(new JsonFactory());
    }

}
