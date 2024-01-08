package com.example.gisService.utils;

import com.example.gisService.configs.GisServiceConfig;
import com.example.gisService.web.models.BoundaryDataRequest;
import com.example.gisService.web.models.BoundaryResponse;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class BoundaryServiceUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private GisServiceConfig config;

    public BoundaryResponse fetchResult(BoundaryDataRequest request){
        BoundaryResponse response = new BoundaryResponse();
        StringBuilder url = new StringBuilder().append(config.getBoundaryServiceHost()).append(config.getBoundaryServicePath());

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(url.toString())
                .queryParam("codes", request.getRequestBody().getCodes())
                .queryParam("tenantId", request.getRequestBody().getTenantId());

        try{
            response = restTemplate.postForObject(uri.toUriString(),request.getRequestInfo(), BoundaryResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ServiceCallException(e.getResponseBodyAsString());
        }
        return response;
    }


}