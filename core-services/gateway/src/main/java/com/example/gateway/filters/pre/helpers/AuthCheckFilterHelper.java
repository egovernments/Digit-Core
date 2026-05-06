package com.example.gateway.filters.pre.helpers;

import com.example.gateway.utils.CommonUtils;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;

import java.util.Set;

import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;
import static com.example.gateway.constants.GatewayConstants.TENANTID_MDC;

@Slf4j
@Component
public class AuthCheckFilterHelper implements RewriteFunction<Map, Map> {

    private ObjectMapper objectMapper;

    private UserUtils userUtils;

    private CommonUtils commonUtils;

    private MultiStateInstanceUtil centralInstanceUtil;

    public AuthCheckFilterHelper(ObjectMapper objectMapper, UserUtils userUtils, CommonUtils commonUtils, MultiStateInstanceUtil centralInstanceUtil) {
        this.objectMapper = objectMapper;
        this.userUtils = userUtils;
        this.commonUtils = commonUtils;
        this.centralInstanceUtil = centralInstanceUtil;
    }

    @Override
    public Publisher<Map> apply(ServerWebExchange serverWebExchange, Map body) {
        try {
            RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
            requestInfo.setUserInfo(userUtils.getUser(requestInfo.getAuthToken(), serverWebExchange));
            body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);

            if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {

                Set<String> tenantIds = commonUtils.validateRequestAndSetRequestTenantId(serverWebExchange, body);
                /*
                 * Adding tenantId to header for tracer logging with correlation-id and routing
                 */
                String singleTenantId = commonUtils.getLowLevelTenantIdFromSet(tenantIds);
                MDC.put(TENANTID_MDC, singleTenantId);
                serverWebExchange.getAttributes().put(TENANTID_MDC, singleTenantId);
            }

            return Mono.just(body);
        } catch (Exception ex) {
            log.error("An error occurred in Auth check filter", ex);

            // Throw a custom exception
            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
        }
    }

}
