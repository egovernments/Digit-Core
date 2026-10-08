package com.example.gateway.utils;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.exception.UserDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class UserUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        String idToken = getIdToken(exchange);
        if (StringUtils.hasText(idToken))
            headers.add(ID_TOKEN, idToken);
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);

        try {
            user = restTemplate.postForObject(authURL, httpEntity, User.class);
        } catch (HttpClientErrorException e) {
            throw toUserDetailsException(e).orElseGet(() -> userFetchFailure(e));
        } catch (RestClientResponseException e) {
            throw userFetchFailure(e);
        } catch (Exception e) {
            // Never return the transport failure text (it names the internal egov-user URL) to the client
            log.error("Fetching user details failed", e);
            throw new CustomException(USER_FETCH_FAILURE_CODE, USER_FETCH_FAILURE_MESSAGE);
        }

        return user;
    }

    /**
     * egov-user's error text is never echoed to the client. The one signal that has to survive is the
     * InvalidAccessTokenException marker: the UI logs the user out when an error message contains it.
     */
    private CustomException userFetchFailure(RestClientResponseException e) {
        log.error("Fetching user details failed with HTTP {}", e.getStatusCode().value(), e);
        if (e.getResponseBodyAsString().contains(INVALID_ACCESS_TOKEN_MARKER))
            return new CustomException(USER_FETCH_FAILURE_CODE, INVALID_ACCESS_TOKEN_MESSAGE);
        // The echoed text used to contain "Internal Server Error" (egov-user 500s), which sends the UI to its
        // maintenance page; keep that behaviour without echoing anything
        if (String.valueOf(e.getMessage()).toLowerCase().contains("internal server error"))
            return new CustomException(USER_FETCH_FAILURE_CODE, USER_SERVICE_ERROR_MESSAGE);
        return new CustomException(USER_FETCH_FAILURE_CODE, USER_FETCH_FAILURE_MESSAGE);
    }

    private String getIdToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ID_TOKEN);
        if (cookie != null && StringUtils.hasText(cookie.getValue()))
            return cookie.getValue();
        return exchange.getRequest().getHeaders().getFirst(ID_TOKEN);
    }

    private Optional<CustomException> toUserDetailsException(HttpClientErrorException e) {
        try {
            JsonNode body = OBJECT_MAPPER.readTree(e.getResponseBodyAsString());
            JsonNode error = body.path("Errors").path(0);
            if (error.hasNonNull("code"))
                return Optional.of(new UserDetailsException(error.path("code").asText(),
                        error.path("message").asText(null), error.path("description").asText(null)));
            if (body.hasNonNull("error")) {
                String description = body.path("error_description").asText(null);
                return Optional.of(new UserDetailsException(body.path("error").asText(), description, description));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
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
