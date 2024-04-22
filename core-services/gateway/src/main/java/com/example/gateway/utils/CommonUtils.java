package com.example.gateway.utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.egov.tracer.model.CustomException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import static com.example.gateway.constants.GatewayConstants.*;

@Component
public class CommonUtils {

    private ObjectMapper objectMapper;

    public CommonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static boolean isRequestBodyCompatible(ServerHttpRequest serverHttpRequest) {
        String requestMethod = getRequestMethod(serverHttpRequest);
        String contentType = getRequestContentType(serverHttpRequest);

        return (POST.equalsIgnoreCase(requestMethod)
                || PUT.equalsIgnoreCase(requestMethod)
                || PATCH.equalsIgnoreCase(requestMethod))
                && (contentType.contains(JSON_TYPE)
                || contentType.contains(X_WWW_FORM_URLENCODED_TYPE));
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
        return contentTypeOptional.get().stream()
                .findFirst()
                .map(String::toLowerCase)
                .orElse("");
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


    public Set<String> validateRequestAndSetRequestTenantId(ServerWebExchange exchange , Map body) {

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
                        if (tenant != null && !tenant.equalsIgnoreCase("null"))
                            tenantIds.add(tenant);
                    });
                } else {
                    setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
                }

            } catch (Exception e) {
                CustomException customException = new CustomException("REQUEST_PARSE_FAILED", "Failed to parse request at API gateway");
                customException.setCode(HttpStatus.UNAUTHORIZED.toString());
                throw customException;
            }
        }
        else {
            setTenantIdsFromQueryParams(request.getQueryParams(), tenantIds);
        }

        return tenantIds;
    }
    public void setTenantIdsFromQueryParams(MultiValueMap<String, String> queryParams, Set<String> tenantIds) throws CustomException {

        if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(REQUEST_TENANT_ID_KEY)
                && queryParams.get(REQUEST_TENANT_ID_KEY).size() > 0) {
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

}