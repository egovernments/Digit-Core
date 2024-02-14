package com.example.gateway.filters.pre;

import com.example.gateway.Utils.Utils;
import org.egov.common.contract.request.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.example.gateway.constants.RequestContextConstants.AUTH_TOKEN_KEY;
import static com.example.gateway.constants.RequestContextConstants.USER_INFO_KEY;

@Component
@Order(3)
public class AuthFilter implements GlobalFilter {

    @Value("${egov.auth-service-host}")
    private  String authServiceHost;

    @Value("${egov.auth-service-uri}")
    private  String authUri;

    private  RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Utils utils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=============================================================================================AUTH_FILTER=====================================================================================================================");
        String authToken = exchange.getAttribute(AUTH_TOKEN_KEY);
        if (authToken == null) {
            throw new IllegalArgumentException("Auth token is missing");
        }
        try {
            User user = getUser(authToken);
            exchange.getAttributes().put(USER_INFO_KEY, user);
        } catch (HttpClientErrorException ex) {
            logger.error("Retrieving user failed", ex);
            throw ex;
        } catch (ResourceAccessException ex) {
            logger.error("Retrieving user failed", ex);
            throw new RuntimeException("User authentication service is down", ex);
        }

        // Additional logic for tenant ID if needed
        // For example:
        // Set<String> tenantIds = utils.validateRequestAndSetRequestTenantId();
        // String singleTenantId = utils.getLowLevelTenatFromSet(tenantIds);
        // exchange.getRequest().mutate().header("TENANT_ID_HEADER_NAME", singleTenantId);

        return chain.filter(exchange);
    }

    private User getUser(String authToken) {
        String authURL = String.format("%s%s%s", authServiceHost, authUri, authToken);
        HttpHeaders headers = new HttpHeaders();
        // Add headers if needed
        RestTemplate restTemplate1 = new RestTemplate();
        return restTemplate1.postForObject(authURL, null, User.class);
    }

}
