package com.example.gateway.utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.model.*;
import digit.models.coremodels.mdms.MasterDetail;
import digit.models.coremodels.mdms.MdmsCriteria;
import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.ModuleDetail;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.slf4j.MDC;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import static com.example.gateway.constants.GatewayConstants.*;
import static java.util.Objects.isNull;

@Component
@Slf4j
public class CommonUtils {

    private ObjectMapper objectMapper;

    private MultiStateInstanceUtil centralInstanceUtil;

    private UserUtils userUtils;

    private ApplicationProperties properties;

    private RestTemplate restTemplate;

    private static final String OPENING_BRACES = "{";
    private static final String CLOSING_BRACES = "}";
    private static final String PARAMETER_PLACEHOLDER_REGEX = "\\{\\w+\\}";
    private static final String ANY_WORD_REGEX = "\\\\w+";


    public CommonUtils(ObjectMapper objectMapper, MultiStateInstanceUtil centralInstanceUtil, UserUtils userUtils, ApplicationProperties properties, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.centralInstanceUtil = centralInstanceUtil;
        this.userUtils = userUtils;
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public static boolean isRequestBodyCompatible(ServerHttpRequest serverHttpRequest) {
        String requestMethod = getRequestMethod(serverHttpRequest);
        String contentType = getRequestContentType(serverHttpRequest);

        return (POST.equalsIgnoreCase(requestMethod) || PUT.equalsIgnoreCase(requestMethod) || PATCH.equalsIgnoreCase(requestMethod)) && (contentType.contains(JSON_TYPE) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE));
    }

    public boolean isFormContentType(String contentType) {
        return contentType == null || contentType.contains(FORM_DATA) || contentType.contains(X_WWW_FORM_URLENCODED_TYPE);
    }

    public void handleCentralInstanceLogic(ServerWebExchange exchange, String requestURI, boolean isOpenRequest, boolean isMixedModeRequest, Map body) {
        if (centralInstanceUtil.getIsEnvironmentCentralInstance() && (isOpenRequest || isMixedModeRequest) && !requestURI.equalsIgnoreCase("/user/oauth/token")) {

            Set<String> tenantIds = new HashSet<>();
            if (HttpMethod.GET.equals(exchange.getRequest().getMethod()) || ObjectUtils.isEmpty(body)) {
                setTenantIdsFromQueryParams(exchange.getRequest().getQueryParams(), tenantIds);
            } else {
                tenantIds = getTenantIdsFromRequest(exchange.getRequest(), body);

            }

            if (CollectionUtils.isEmpty(tenantIds) && isOpenRequest) {
                throw new CustomException("INVALID_TENANT_ID", "No tenantId in the request");
            }

            String tenantId = getLowLevelTenantIdFromSet(tenantIds);
            MDC.put(TENANTID_MDC, tenantId);
            exchange.getAttributes().put(TENANTID_MDC, tenantId);
        }
    }

    private static String getRequestMethod(ServerHttpRequest serverHttpRequest) {
        return serverHttpRequest.getMethod().toString();
    }


    public static String getRequestContentType(ServerHttpRequest serverHttpRequest) {
        List<String> contentTypeHeaders = serverHttpRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE);

        // Wrap the list in an Optional
        Optional<List<String>> contentTypeOptional = Optional.ofNullable(contentTypeHeaders);

        // If the Optional is empty, return an empty string
        if (contentTypeOptional.isEmpty()) {
            return "";
        }

        // Get the first content type header, convert it to lowercase, and return it
        return contentTypeOptional.get().stream().findFirst().map(String::toLowerCase).orElse("");
    }


    public String getLowLevelTenantIdFromSet(Set<String> tenants) {

        String lowLevelTenant = null;
        int countOfSubTenantsPresent = 0;

        for (String tenant : tenants) {
            int currentCount = tenant.split("\\.").length;
            if (currentCount >= countOfSubTenantsPresent) {
                countOfSubTenantsPresent = currentCount;
                lowLevelTenant = tenant;
            }
        }
        return lowLevelTenant;
    }


    public Set<String> validateRequestAndSetRequestTenantId(ServerWebExchange exchange, Map body) {

        return getTenantIdsFromRequest(exchange.getRequest(), body);
    }

    public Set<String> getTenantIdsFromRequest(ServerHttpRequest request, Map body) throws CustomException {

        Set<String> tenantIds = new HashSet<>();

        if (CommonUtils.isRequestBodyCompatible(request)) {

            try {
                ObjectNode requestBody = objectMapper.convertValue(body, ObjectNode.class);

                if (requestBody.has(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))
                    requestBody.remove(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);

                else if (requestBody.has(REQUEST_INFO_FIELD_NAME_CAMEL_CASE))
                    requestBody.remove(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);

                List<String> tenants = new LinkedList<>();

                for (JsonNode node : requestBody.findValues(REQUEST_TENANT_ID_KEY)) {
                    if (node.getNodeType() == JsonNodeType.ARRAY) {
                        node.elements().forEachRemaining(n -> tenants.add(n.asText()));
                    } else if (node.getNodeType() == JsonNodeType.STRING) {
                        tenants.add(node.asText());
                    }
                }

                if (!tenants.isEmpty()) {
                    tenants.forEach(tenant -> {
                        if (tenant != null && !tenant.equalsIgnoreCase("null")) tenantIds.add(tenant);
                    });
                } else {
                    setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
                }

            } catch (Exception e) {
                CustomException customException = new CustomException("REQUEST_PARSE_FAILED", "Failed to parse request at API gateway");
                customException.setCode(HttpStatus.UNAUTHORIZED.toString());
                throw customException;
            }
        } else {
            setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
        }

        return tenantIds;
    }

    public void setTenantIdsFromQueryParams(MultiValueMap<String, String> queryParams, Set<String> tenantIds) throws CustomException {

        if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(REQUEST_TENANT_ID_KEY) && queryParams.get(REQUEST_TENANT_ID_KEY).size() > 0) {
            String tenantId = queryParams.get(REQUEST_TENANT_ID_KEY).get(0);
            if (tenantId.contains(",")) {
                tenantIds.addAll(Arrays.asList(tenantId.split(",")));
            } else {
                tenantIds.add(tenantId);
            }
        } else {
            throw new CustomException("TENANT_ID_MANDATORY", "TenantId is mandatory in URL for non json requests");
        }

    }

    public String getRequestURL(ServerHttpRequest request) {

        // Manually construct the full request URL
        String scheme = request.getURI().getScheme(); // e.g., "http" or "https"
        String host = request.getURI().getHost();     // e.g., "example.com"
        int port = request.getURI().getPort();        // e.g., 80 or 443 (can be -1 if default port is used)
        String path = request.getURI().getPath();     // e.g., "/api/users"

        // Construct the full URL
        StringBuilder requestURL = new StringBuilder(scheme).append("://").append(host);

        // Add the port if it's not the default (80 for HTTP, 443 for HTTPS)
        if (port != -1) {
            requestURL.append(":").append(port);
        }

        // Append the request path
        requestURL.append(path);

        return requestURL.toString();
    }

    /**
     * method to fetch state level tenant-id based on whether the server is a
     * multi-state instance or single-state instance
     *
     * @return
     */
    public String getStateLevelTenantForHost(ServerHttpRequest request) {
        String tenantId = "";
        if (centralInstanceUtil.getIsEnvironmentCentralInstance()) {
            String requestURL = getRequestURL(request);
            String host = requestURL.replace(request.getURI().getPath(), "").replace("https://", "").replace("http://", "");
            tenantId = userUtils.getStateLevelTenantMap().get(host);
        } else {
            tenantId = userUtils.getStateLevelTenant();
        }
        return tenantId;
    }

    public void setAnonymousUser(ServerWebExchange exchange, Map body) {
        ServerHttpRequest request = exchange.getRequest();
        String CorrelationId = exchange.getAttributes().get(CORRELATION_ID_KEY).toString();
        String tenantId = getStateLevelTenantForHost(request);
        User systemUser = userUtils.fetchSystemUser(tenantId, CorrelationId);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
            requestInfo.setUserInfo(systemUser);
            body.put(REQUEST_INFO_FIELD_NAME_PASCAL_CASE, requestInfo);
        } catch (Exception ex) {
            log.error("An error occured while transforming the request body to set Anonymous User {}", ex);

            // Throw a custom exception
            throw new CustomException("AUTHENTICATION_ERROR", ex.getMessage());
        }
    }


    public void validateUriJurisdictionAuthorization(ServerWebExchange exchange, User user, String JurisdictionId) {

        String requestUri = exchange.getRequest().getURI().getPath();

        AuthorizationRequest request = AuthorizationRequest.builder().roles(new HashSet<>(user.getRoles())).uri(requestUri).tenantIds(Collections.singleton(MDC.get(TENANT_ID_KEY))).JurisdictionId(JurisdictionId).build();

        boolean isUriAuthorised = isUriAuthorized(request, exchange);

        if (!isUriAuthorised) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.toString(), "You are not authorized to access this resource");
        }

    }

    private boolean isUriAuthorized(AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {

        AuthorizationRequestWrapper authorizationRequestWrapper = new AuthorizationRequestWrapper(new RequestInfo(), authorizationRequest);

        final HttpHeaders headers = new HttpHeaders();

        headers.add(CORRELATION_ID_HEADER_NAME, (String) exchange.getAttributes().get(CORRELATION_ID_KEY));

        if (centralInstanceUtil.getIsEnvironmentCentralInstance())
            headers.add(REQUEST_TENANT_ID_KEY, (String) exchange.getAttributes().get(TENANTID_MDC));

        final HttpEntity<Object> httpEntity = new HttpEntity<>(authorizationRequestWrapper, headers);

        try {

            ResponseEntity<Void> responseEntity = restTemplate.postForEntity(properties.getAuthorizationUrl(), httpEntity, Void.class);

            return responseEntity.getStatusCode().equals(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.warn("Exception while attempting to authorize via access control", e);
            return false;
        } catch (Exception e) {
            log.error("Unknown exception occurred while attempting to authorize via access control", e);
            return false;
        }

    }


}