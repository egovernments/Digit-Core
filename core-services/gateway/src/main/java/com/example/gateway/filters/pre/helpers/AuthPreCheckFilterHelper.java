package com.example.gateway.filters.pre.helpers;

import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class AuthPreCheckFilterHelper implements RewriteFunction<Map, Map> {

    public static final String AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE = "Retrieving of auth token failed";
    public static final String ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE = "Routing to anonymous endpoint: {}";
    public static final String ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE =
            "Routing to protected endpoint {} restricted - No auth token";
    public static final String UNAUTHORIZED_USER_MESSAGE = "You are not authorized to access this resource";
    public static final String PROCEED_ROUTING_MESSAGE = "Routing to an endpoint: {} - auth provided";
    private List<String> openEndpointsWhitelist;
    private List<String> mixedModeEndpointsWhitelist;
    private ObjectMapper objectMapper;

    private UserUtils userUtils;
    public AuthPreCheckFilterHelper(List<String> openEndpointsWhitelist, List<String> mixedModeEndpointsWhitelist,
                                    ObjectMapper objectMapper, UserUtils userUtils) {

        this.openEndpointsWhitelist = openEndpointsWhitelist;
        this.mixedModeEndpointsWhitelist = mixedModeEndpointsWhitelist;
        this.objectMapper = objectMapper;
    }


    @Override
    public Publisher<Map> apply(ServerWebExchange exchange, Map body) {

        String authToken;
        String endPointPath = exchange.getRequest().getPath().value();

        if (openEndpointsWhitelist.contains(endPointPath)) {
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
            log.info(OPEN_ENDPOINT_MESSAGE, endPointPath);
            return Mono.just(body);
        }

        try {
            RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
            authToken = requestInfo.getAuthToken();
        } catch (Exception e) {
            log.error(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, e);
            throw new CustomException(AUTH_TOKEN_RETRIEVE_FAILURE_MESSAGE, e.getMessage());
        }

        if (ObjectUtils.isEmpty(authToken)) {
            if (mixedModeEndpointsWhitelist.contains(endPointPath)) {
                log.info(ROUTING_TO_ANONYMOUS_ENDPOINT_MESSAGE, endPointPath);
                exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.FALSE);
//                User systemUser = userUtils.fetchSystemUser(requestInfo.getTenantId(), exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER_NAME));
            } else {
                log.info(ROUTING_TO_PROTECTED_ENDPOINT_RESTRICTED_MESSAGE, endPointPath);
                CustomException customException = new CustomException(UNAUTHORIZED_USER_MESSAGE, UNAUTHORIZED_USER_MESSAGE);
                customException.setCode(HttpStatus.UNAUTHORIZED.toString());
                throw customException;
            }
        } else {
            log.info(PROCEED_ROUTING_MESSAGE, endPointPath);
            exchange.getAttributes().put(AUTH_BOOLEAN_FLAG_NAME, Boolean.TRUE);

        }

        return Mono.just(body);
    }
}
