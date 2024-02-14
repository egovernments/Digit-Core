package com.example.gateway.filters.pre;

import com.example.gateway.model.AuthorizationRequest;
import org.egov.common.contract.request.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static com.example.gateway.constants.RequestContextConstants.TENANTID_MDC;
import static com.example.gateway.constants.RequestContextConstants.USER_INFO_KEY;

/**
 * 4th filter
 */
//@Component
@Order(4)
public class RbacFilter implements GlobalFilter {

    private static final String FORBIDDEN_MESSAGE = "Not authorized to access this resource";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RestTemplate restTemplate;

    @Value("${egov.authorize.access.control.host}${egov.authorize.access.control.uri}")
    private String authorizationUrl;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=============================================================================================RBAC_FILTER=====================================================================================================================");
        boolean isIncomingURIInAuthorizedActionList = isIncomingURIInAuthorizedActionList(exchange);

        if (isIncomingURIInAuthorizedActionList) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isIncomingURIInAuthorizedActionList(ServerWebExchange exchange) {
        String requestUri = exchange.getRequest().getURI().getPath();
        // Fetch user information from the exchange
        User user = exchange.getAttribute(USER_INFO_KEY);

        if (user == null) {
            // Handle case where user information is not found
            logger.error("User information not found. Can't execute RBAC filter");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().setComplete();
            return false;
        }

        // Extract tenant IDs
        Set<String> tenantIds = extractTenantIds(exchange);

        // Set tenant ID in MDC for logging
        MDC.put(TENANTID_MDC, StringUtils.collectionToCommaDelimitedString(tenantIds));

        // Prepare authorization request
        AuthorizationRequest request = AuthorizationRequest.builder()
                .roles(new HashSet<>(user.getRoles()))
                .uri(requestUri)
                .tenantIds(tenantIds)
                .build();

        // Check if URI is authorized
        return isUriAuthorized(request);
    }

    private Set<String> extractTenantIds(ServerWebExchange exchange) {
        // Extract tenant IDs from the exchange
        // You can implement your logic here to extract tenant IDs from headers, path variables, etc.
        return new HashSet<>();
    }

    private boolean isUriAuthorized(AuthorizationRequest authorizationRequest) {
        HttpHeaders headers = new HttpHeaders();
        // Add headers as needed

        try {
            // Make a request to the authorization service
            // You may need to modify this part to fit your specific authorization service
            RestTemplate restTemplate1 = new RestTemplate();
            ResponseEntity<Void> responseEntity = restTemplate1.postForEntity(authorizationUrl, authorizationRequest, Void.class);

            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.warn("Exception while attempting to authorize via access control", e);
            return false;
        } catch (Exception e) {
            logger.error("Unknown exception occurred while attempting to authorize via access control", e);
            return false;
        }
    }
}
