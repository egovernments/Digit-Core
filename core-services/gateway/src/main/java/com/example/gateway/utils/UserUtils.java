package com.example.gateway.utils;

import com.example.gateway.config.ApplicationProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.UserSearchRequest;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class UserUtils {

    @Getter
    @Value("#{${egov.statelevel.tenant.map:{}}}")
    private Map<String, String> stateLevelTenantMap;

    @Getter
    @Value("${egov.statelevel.tenant}")
    private String stateLevelTenant;


    private RestTemplate restTemplate;

    private ApplicationProperties applicationProperties;

    private MultiStateInstanceUtil multiStateInstanceUtil;

    public UserUtils(RestTemplate restTemplate, ApplicationProperties applicationProperties, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    public User getUser(String authToken, ServerWebExchange exchange) {
        User user;
        String authURL = String.format("%s%s%s", applicationProperties.getAuthServiceHost(), applicationProperties.getAuthUri(), authToken);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CORRELATION_ID_HEADER_NAME, (String) exchange.getAttributes().get(CORRELATION_ID_KEY));
        if (multiStateInstanceUtil.getIsEnvironmentCentralInstance())
            headers.add(REQUEST_TENANT_ID_KEY, (String) exchange.getAttributes().get(TENANTID_MDC));
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);

        try {
            user = restTemplate.postForObject(authURL, httpEntity, User.class);
        } catch (Exception e) {
            throw new CustomException("Exception occurred while fetching user: ", e.getMessage());
        }

        return user;
    }

    // TODO: test this once for actual data
    @Cacheable(value = "systemUser", sync = true)
    public User fetchSystemUser(String tenantId, String correlationId) {

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setRoleCodes(Collections.singletonList("ANONYMOUS"));
        userSearchRequest.setUserType("SYSTEM");
        userSearchRequest.setPageSize(1);
        userSearchRequest.setTenantId(tenantId);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(CORRELATION_ID_HEADER_NAME, correlationId);
        if (multiStateInstanceUtil.getIsEnvironmentCentralInstance())
            headers.add(REQUEST_TENANT_ID_KEY, tenantId);
        final HttpEntity<Object> httpEntity = new HttpEntity<>(userSearchRequest, headers);

        StringBuilder uri = new StringBuilder(applicationProperties.getUserSearchURI());
        User user = null;
        try {
            UserDetailResponse response = restTemplate.postForObject(uri.toString(), httpEntity, UserDetailResponse.class);
            if (!CollectionUtils.isEmpty(response.getUser()))
                user = response.getUser().get(0);
        } catch (Exception e) {
            log.error("Exception while fetching system user: ", e);
        }

        /*if(user == null)
            throw new CustomException("NO_SYSTEUSER_FOUND","No system user found");*/

        return user;
    }

}
