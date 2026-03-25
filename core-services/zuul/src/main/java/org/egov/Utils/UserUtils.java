package org.egov.Utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.egov.contract.User;
import org.egov.model.UserDetailResponse;
import org.egov.model.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

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

    private final WebClient webClient;

    @Autowired
    public UserUtils(WebClient webClient) {
        this.webClient = webClient;
    }

    @Cacheable(value = "systemUser", sync = true)
    public User fetchSystemUser(String tenantId) {
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setRoleCodes(Collections.singletonList("ANONYMOUS"));
        userSearchRequest.setUserType("SYSTEM");
        userSearchRequest.setPageSize(1);
        userSearchRequest.setTenantId(tenantId);

        User user = null;
        try {
            UserDetailResponse response = webClient.post()
                    .uri(userSearchURI)
                    .bodyValue(userSearchRequest)
                    .retrieve()
                    .bodyToMono(UserDetailResponse.class)
                    .block();
            if (response != null && !CollectionUtils.isEmpty(response.getUser()))
                user = response.getUser().get(0);
        } catch (Exception e) {
            log.error("Exception while fetching system user: ", e);
        }

        return user;
    }
}
