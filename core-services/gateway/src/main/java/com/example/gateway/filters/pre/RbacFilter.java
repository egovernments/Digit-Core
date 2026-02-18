package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.filters.pre.helpers.RbacFilterHelper;
import com.example.gateway.model.AuthorizationRequest;
import com.example.gateway.model.AuthorizationRequestWrapper;
import com.example.gateway.utils.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHeaders;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class RbacFilter implements GlobalFilter, Ordered {

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
    private RbacFilterHelper rbacFilterHelper;
    private CommonUtils commonUtils;
    private ObjectMapper objectMapper;
    private MultiStateInstanceUtil centralInstanceUtil;
    private RestTemplate restTemplate;
    private ApplicationProperties applicationProperties;

    public RbacFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, RbacFilterHelper rbacFilterHelper, CommonUtils commonUtils, ObjectMapper objectMapper, MultiStateInstanceUtil centralInstanceUtil, RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.rbacFilterHelper = rbacFilterHelper;
        this.commonUtils = commonUtils;
        this.objectMapper = objectMapper;
        this.centralInstanceUtil = centralInstanceUtil;
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        boolean isGetRequest = HttpMethod.GET.equals(exchange.getRequest().getMethod());
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        Boolean rbacFlag = exchange.getAttribute(RBAC_BOOLEAN_FLAG_NAME);

        if (Boolean.TRUE.equals(rbacFlag)) {
            if (isGetRequest || commonUtils.isFormContentType(contentType)) {
                isIncomingURIInAuthorizedActionList(exchange);
            } else {
                return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config().setRewriteFunction(Map.class, Map.class, rbacFilterHelper)).filter(exchange, chain);
            }

        }

        return chain.filter(exchange);
    }

    private void isIncomingURIInAuthorizedActionList(ServerWebExchange exchange) {

        String requestUri = exchange.getRequest().getURI().getPath();
        String userInfo = MDC.get(USER_INFO_KEY);
        User user = null;
        if (!ObjectUtils.isEmpty(userInfo)) {
            try {
                user = objectMapper.readValue(userInfo, User.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }
        if (user == null) {
            throw new RuntimeException("User information not found. Can't execute RBAC filter");
        }

        Set<String> tenantIds = new HashSet<>();
        commonUtils.setTenantIdsFromQueryParams(exchange.getRequest().getQueryParams(), tenantIds);

        /*
         * Adding tenantId to header for tracer logging with correlation-id
         */
        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && StringUtils.isEmpty(exchange.getAttributes().get(TENANTID_MDC))) {
            String singleTenantId = commonUtils.getLowLevelTenantIdFromSet(tenantIds);
            MDC.put(TENANTID_MDC, singleTenantId);
            exchange.getAttributes().put(TENANTID_MDC, singleTenantId);
        }

        exchange.getAttributes().put(CURRENT_REQUEST_TENANTID, String.join(",", tenantIds));

        AuthorizationRequest request = AuthorizationRequest.builder().roles(new HashSet<>(user.getRoles())).uri(requestUri).tenantIds(tenantIds).build();

        boolean isUriAuthorised = isUriAuthorized(request, exchange);

        if (!isUriAuthorised) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.toString(), "You are not authorized to access this resource");
        }

    }

    private boolean isUriAuthorized(AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {

        AuthorizationRequestWrapper authorizationRequestWrapper = new AuthorizationRequestWrapper(new RequestInfo(), authorizationRequest);

        final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();

        headers.add(CORRELATION_ID_HEADER_NAME, (String) exchange.getAttributes().get(CORRELATION_ID_KEY));

        if (centralInstanceUtil.getIsEnvironmentCentralInstance())
            headers.add(REQUEST_TENANT_ID_KEY, (String) exchange.getAttributes().get(TENANTID_MDC));

        final HttpEntity<Object> httpEntity = new HttpEntity<>(authorizationRequestWrapper, headers);

        try {

            ResponseEntity<Void> responseEntity = restTemplate.postForEntity(applicationProperties.getAuthorizationUrl(), httpEntity, Void.class);

            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.warn("Exception while attempting to authorize via access control", e);
            return false;
        } catch (Exception e) {
            log.error("Unknown exception occurred while attempting to authorize via access control", e);
            return false;
        }

    }


    @Override
    public int getOrder() {
        return 5;
    }
}
