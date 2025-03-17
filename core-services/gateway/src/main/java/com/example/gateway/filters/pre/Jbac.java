package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class Jbac implements GlobalFilter, Ordered {

    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private ApplicationProperties configs;

    public Jbac(ObjectMapper objectMapper, RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.configs = applicationProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Boolean jbacFlag = exchange.getAttribute(JBAC_BOOLEAN_FLAG_NAME);
        if (Boolean.FALSE.equals(jbacFlag)) chain.filter(exchange);

        // extract the code {boundary_id} from header
        String boundaryId = exchange.getRequest().getHeaders().getFirst(BOUNDARY_ID);

        // call the hrms employee search with user details
        String userInfo = MDC.get(USER_INFO_KEY);
        User user;
        List<String> boundaries;
        try {

            user = objectMapper.readValue(userInfo, User.class);
            RequestInfo requestInfo = RequestInfo.builder().userInfo(user).apiId("Rainmaker").build();
            String url = UriComponentsBuilder.fromHttpUrl(configs.getHrmsSearch()).queryParam(REQUEST_TENANT_ID_KEY, MDC.get(TENANT_ID_KEY)).queryParam(USER_SERVICE_UUIDS, user.getUuid()).toUriString();

            log.info(url);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("RequestInfo", requestInfo);
            String response = restTemplate.postForObject(url, requestBody, String.class);
            boundaries = JsonPath.read(response, Jurisdiction_JSON_PATH);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // validate the boundary_id with the hrms response
        if (!boundaries.contains(boundaryId)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.toString(), "Request is not under your jurisdiction");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 7;
    }
}
