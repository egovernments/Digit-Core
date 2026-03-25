package org.egov.filters.global;

import lombok.extern.slf4j.Slf4j;
import org.egov.Utils.ExceptionUtils;
import org.egov.Utils.Utils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.config.GatewayProperties;
import org.egov.contract.User;
import org.egov.model.AuthorizationRequest;
import org.egov.model.AuthorizationRequestWrapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashSet;
import java.util.Set;

import static org.egov.constants.RequestContextConstants.*;

/**
 * 5th pre filter. If the RBAC flag is enabled, checks if the URI is authorized for the user's roles.
 */
@Component
@Slf4j
public class RbacFilter implements GlobalFilter, Ordered {

    private static final String FORBIDDEN_MESSAGE = "Not authorized to access this resource";

    @Autowired
    private Utils utils;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    private WebClient webClient;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Boolean shouldDoRbac = exchange.getAttribute(RBAC_BOOLEAN_FLAG_NAME);
        if (shouldDoRbac == null || !shouldDoRbac) {
            return chain.filter(exchange);
        }

        String requestUri = exchange.getRequest().getURI().getPath();
        User user = exchange.getAttribute(USER_INFO_KEY);

        if (user == null) {
            return ExceptionUtils.writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "AUTH_ERROR", "User information not found. Can't execute RBAC filter");
        }

        byte[] bodyBytes = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        Set<String> tenantIds = utils.validateRequestAndSetRequestTenantId(exchange, bodyBytes);

        if (centralInstanceUtil.getIsEnvironmentCentralInstance()
                && !StringUtils.hasText((String) exchange.getAttribute(TENANTID_MDC))) {
            String singleTenantId = utils.getLowLevelTenantFromSet(tenantIds);
            MDC.put(TENANTID_MDC, singleTenantId);
            exchange.getAttributes().put(TENANTID_MDC, singleTenantId);
        }

        exchange.getAttributes().put(CURRENT_REQUEST_TENANTID, String.join(",", tenantIds));

        AuthorizationRequest authRequest = AuthorizationRequest.builder()
                .roles(new HashSet<>(user.getRoles()))
                .uri(requestUri)
                .tenantIds(tenantIds)
                .build();

        return isUriAuthorized(exchange, authRequest)
                .flatMap(authorized -> {
                    if (authorized) {
                        return chain.filter(exchange);
                    }
                    return ExceptionUtils.writeErrorResponse(exchange, HttpStatus.FORBIDDEN,
                            "FORBIDDEN", FORBIDDEN_MESSAGE);
                });
    }

    private Mono<Boolean> isUriAuthorized(ServerWebExchange exchange, AuthorizationRequest authorizationRequest) {
        AuthorizationRequestWrapper wrapper = new AuthorizationRequestWrapper(new RequestInfo(), authorizationRequest);

        String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
        String tenantIdMdc = exchange.getAttribute(TENANTID_MDC);

        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(gatewayProperties.getAuthorizationUrl())
                .header(CORRELATION_ID_HEADER_NAME, correlationId);

        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && tenantIdMdc != null) {
            requestSpec = requestSpec.header(REQUEST_TENANT_ID_KEY, tenantIdMdc);
        }

        return requestSpec
                .bodyValue(wrapper)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn("Exception while attempting to authorize via access control", e);
                    return Mono.just(false);
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unknown exception occurred while attempting to authorize via access control", e);
                    return Mono.just(false);
                });
    }
}
