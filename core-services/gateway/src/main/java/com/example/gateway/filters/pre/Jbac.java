package com.example.gateway.filters.pre;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.model.AuthorizationRequest;
import com.example.gateway.model.AuthorizationRequestWrapper;
import com.example.gateway.utils.CommonUtils;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.example.gateway.constants.GatewayConstants.*;

@Slf4j
@Component
public class Jbac implements GlobalFilter, Ordered {

    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private ApplicationProperties configs;
    private CommonUtils utils;
    private MultiStateInstanceUtil centralInstanceUtil;
    private CommonUtils commonUtils;
    private UserUtils userUtils;

    public Jbac(ObjectMapper objectMapper, RestTemplate restTemplate, ApplicationProperties applicationProperties, CommonUtils utils, MultiStateInstanceUtil centralInstanceUtil, CommonUtils commonUtils, UserUtils userUtils) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.configs = applicationProperties;
        this.utils = utils;
        this.centralInstanceUtil = centralInstanceUtil;
        this.commonUtils = commonUtils;
        this.userUtils = userUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String boundaryCode, hierarchyType, tenantId;
        Boolean jbacFlag = exchange.getAttribute(JBAC_BOOLEAN_FLAG_NAME);

        // skip JBAC if disabled
        if (Boolean.FALSE.equals(jbacFlag)) return chain.filter(exchange);

        String JurisdictionId = exchange.getRequest().getHeaders().getFirst(BOUNDARY_ID);
        if (JurisdictionId != null) {
            String[] parts = JurisdictionId.split("\\.");

            if (parts.length >= 3) {
                boundaryCode = parts[parts.length - 1];
                hierarchyType = parts[parts.length - 2];
                tenantId = String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 2));

            } else {
                throw new CustomException("INVALID_JURISDICTION_ID", "Invalid JurisdictionId");
            }
        } else {
            throw new CustomException("JURISDICTION_ID_NOT_FOUND", "Jurisdiction Id not found");
        }

        try {

            String userInfo = MDC.get(USER_INFO_KEY);
            User user = objectMapper.readValue(userInfo, User.class);

            // Searching hrms for user info
            String response = userUtils.fetchHrmsUserData(user, user.getTenantId());

            // validate access to requested JurisdictionId
            checkAccessToJurisdiction(response, tenantId, hierarchyType, boundaryCode);

            // checks employee access to resource under given jurisdiction
            commonUtils.validateUriJurisdictionAuthorization(exchange, user, JurisdictionId);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return chain.filter(exchange);
    }

    // assume that jurisdiction entity validation happens at employee creation level
    public void checkAccessToJurisdiction(String employee, String tenantId, String hierarchyType, String boundaryCode) {
        List<Map<String, Object>> jurisdictions = JsonPath.read(employee, Jurisdiction_JSON_PATH);
        boolean hasAccess = jurisdictions.stream().anyMatch(j ->
                tenantId.equals(j.get("tenantId")) &&
                        hierarchyType.equalsIgnoreCase((String) j.get("hierarchy")) &&
                        boundaryCode.equalsIgnoreCase((String) j.get("boundary"))
        );

        if (!hasAccess)
            throw new CustomException("UNAUTHORIZED_JURISDICTION_ACCESS", "User don't have access to requested JurisdictionId");
    }


    @Override
    public int getOrder() {
        return 7;
    }
}
