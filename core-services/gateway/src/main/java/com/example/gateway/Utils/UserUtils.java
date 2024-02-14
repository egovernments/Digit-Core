package com.example.gateway.Utils;

import com.example.gateway.model.UserDetailResponse;
import com.example.gateway.model.UserSearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

import static com.example.gateway.constants.RequestContextConstants.CORRELATION_ID_HEADER_NAME;
import static com.example.gateway.constants.RequestContextConstants.REQUEST_TENANT_ID_KEY;

@Slf4j
@Repository
public class UserUtils {

    @Getter
    @Value("#{${egov.statelevel.tenant.map:{}}}")
    private Map<String, String> stateLevelTenantMap;

    @Getter
    @Value("${egov.statelevel.tenant}")
    private String stateLevelTenant;

    @Value("${egov.auth-service-host}${egov.user.search.path}")
    private String userSearchURI;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    private WebClient webClient;

    private ObjectMapper objectMapper;

    @Autowired
    public UserUtils(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = "systemUser", sync = true)
    public User fetchSystemUser(String tenantId) {
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setRoleCodes(Collections.singletonList("ANONYMOUS"));
        userSearchRequest.setUserType("SYSTEM");
        userSearchRequest.setPageSize(1);
        userSearchRequest.setTenantId(tenantId);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(CORRELATION_ID_HEADER_NAME, "correlationId"); // Replace with actual correlation ID
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            headers.add(REQUEST_TENANT_ID_KEY, "tenantId"); // Replace with actual tenant ID
        }
        final HttpEntity<UserSearchRequest> httpEntity = new HttpEntity<>(userSearchRequest, headers);

        User user = null;
        try {
            UserDetailResponse response = webClient.post()
                    .uri(userSearchURI)
                    .bodyValue(userSearchRequest)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(UserDetailResponse.class)
                    .block();
            if (response != null && !CollectionUtils.isEmpty(response.getUser())) {
                user = response.getUser().get(0);
            }
        } catch (Exception e) {
            log.error("Exception while fetching system user: ", e);
        }

        /*if(user == null)
            throw new CustomException("NO_SYSTEUSER_FOUND","No system user found");*/

        return user;
    }
}
