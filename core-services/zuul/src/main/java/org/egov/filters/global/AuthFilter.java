package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.ExceptionUtils;
import org.egov.Utils.Utils;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.contract.User;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.egov.constants.RequestContextConstants.*;

/**
 * 4th pre filter. If the auth flag is enabled then the user is retrieved for the given auth token.
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String RETRIEVING_USER_FAILED_MESSAGE = "Retrieving user failed";

    @Value("${egov.auth-service-host}")
    private String authServiceHost;

    @Value("${egov.auth-service-uri}")
    private String authServiceUri;

    @Autowired
    private WebClient webClient;

    @Autowired
    private Utils utils;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Boolean shouldDoAuth = exchange.getAttribute(AUTH_BOOLEAN_FLAG_NAME);
        if (shouldDoAuth == null || !shouldDoAuth) {
            return chain.filter(exchange);
        }

        String authToken = exchange.getAttribute(AUTH_TOKEN_KEY);
        String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
        String tenantIdMdc = exchange.getAttribute(TENANTID_MDC);

        String authURL = String.format("%s%s%s", authServiceHost, authServiceUri, authToken);

        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(authURL)
                .header(CORRELATION_ID_HEADER_NAME, correlationId);

        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && tenantIdMdc != null) {
            requestSpec = requestSpec.header(REQUEST_TENANT_ID_KEY, tenantIdMdc);
        }

        return requestSpec
                .retrieve()
                .bodyToMono(User.class)
                .flatMap(user -> {
                    exchange.getAttributes().put(USER_INFO_KEY, user);

                    if (centralInstanceUtil.getIsEnvironmentCentralInstance()
                            && !StringUtils.hasText((String) exchange.getAttribute(TENANTID_MDC))) {
                        byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
                        Set<String> tenantIds = utils.validateRequestAndSetRequestTenantId(exchange, bodyBytes);
                        String singleTenantId = utils.getLowLevelTenantFromSet(tenantIds);
                        MDC.put(TENANTID_MDC, singleTenantId);
                        exchange.getAttributes().put(TENANTID_MDC, singleTenantId);
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error(RETRIEVING_USER_FAILED_MESSAGE, ex);
                    return ExceptionUtils.writeErrorResponse(exchange,
                            HttpStatus.valueOf(ex.getStatusCode().value()),
                            "AUTH_ERROR", RETRIEVING_USER_FAILED_MESSAGE);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error(RETRIEVING_USER_FAILED_MESSAGE, ex);
                    return ExceptionUtils.writeErrorResponse(exchange,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "AUTH_ERROR", "User authentication service is down");
                });
    }
}
