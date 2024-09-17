package com.example.gateway.filters.pre.helpers;

import com.example.gateway.model.PreHookFilterRequest;
import com.example.gateway.utils.URLProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;

@Slf4j
@Component
public class PreHookFilterHelper implements RewriteFunction<Map, Map> {

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    public PreHookFilterHelper(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {
        String uri = exchange.getRequest().getURI().toString();
        PreHookFilterRequest preHookFilterRequest;
        try {
            preHookFilterRequest = PreHookFilterRequest.builder().Request(objectMapper.writeValueAsString(body)).build();
        } catch (JsonProcessingException e) {
            throw new CustomException("PARSING_ERROR", "Failed to parse request body");
        }

        String response;

        try {
            log.debug("Executing pre-hook filter. Sending request to - " + URLProvider.getUrlPreHooksMap().get(uri));
            response = restTemplate.postForObject(URLProvider.getUrlPreHooksMap().get(uri), preHookFilterRequest, String.class);
            return Mono.just(objectMapper.convertValue(response, Map.class));

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Pre-Hook - Http Exception Occurred", e);
            throw new CustomException("PRE_HOOK_ERROR", "Pre-hook url threw an error - " + e.getMessage());
        } catch (Exception e) {
            log.error("Pre-Hook - Exception Occurred", e);
            throw new CustomException("PRE_HOOK_ERROR", "Pre-hook url threw an error" + e.getMessage());
        }
    }

}
