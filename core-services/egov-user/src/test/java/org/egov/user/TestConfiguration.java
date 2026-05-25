package org.egov.user;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.egov.encryption.util.MdmsFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

@Configuration
public class TestConfiguration {

    @Bean
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    public MdmsFetcher mdmsFetcher() {
        MdmsFetcher mock = mock(MdmsFetcher.class);

        when(mock.getSecurityMdmsForFilter(anyString()))
                .thenReturn(Resources.getFileJSONArrayContents("getSecurityMdmsForFilterResponse.json"));

        when(mock.getMaskingMdmsForFilter(anyString()))
                .thenReturn(Resources.getFileJSONArrayContents("getMaskingMdmsForFilterResponse.json"));

        return mock;
    }

}
