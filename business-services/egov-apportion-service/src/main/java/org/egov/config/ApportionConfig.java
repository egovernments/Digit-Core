package org.egov.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Data
@Component
public class ApportionConfig {

    @Value("${app.timezone}")
    private String timeZone;

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    /*@Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).setTimeZone(TimeZone.getTimeZone(timeZone));
    }*/

    @Bean
    @Autowired
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



    //Persister Config
    @Value("${persister.save.bill.apportion.request.topic}")
    private String billRequestTopic;

    @Value("${persister.save.bill.apportion.response.topic}")
    private String billResponseTopic;

    @Value("${persister.save.demand.apportion.request.topic}")
    private String demandRequestTopic;

    @Value("${persister.save.demand.apportion.response.topic}")
    private String demandResponseTopic;

    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    //Billing Service
    @Value("${egov.billing.host}")
    private String billingHost;

    @Value("${egov.billing.business.service.endpoint}")
    private String businessServiceEndpoint;

    @Value("${egov.billing.tax.head.endpoint}")
    private String taxHeadEndpoint;

    //Default implementation switch
    @Value("${egov.apportion.default.value.order}")
    private Boolean apportionByValueAndOrder;




}
