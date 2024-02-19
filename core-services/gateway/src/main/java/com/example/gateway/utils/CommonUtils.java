package com.example.gateway.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import static com.example.gateway.constants.GatewayConstants.*;

@Component
public class CommonUtils {

    private ObjectMapper objectMapper;

    public CommonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static boolean isRequestBodyCompatible(ServerHttpRequest serverHttpRequest) {
        return (POST.equalsIgnoreCase(getRequestMethod(serverHttpRequest))
                        || PUT.equalsIgnoreCase(getRequestMethod(serverHttpRequest))
                        || PATCH.equalsIgnoreCase(getRequestMethod(serverHttpRequest)))
                && getRequestContentType(serverHttpRequest).contains(JSON_TYPE);
    }

    private static String getRequestMethod(ServerHttpRequest serverHttpRequest) {
        return serverHttpRequest.getMethod().toString();
    }

    public static String getRequestContentType(ServerHttpRequest serverHttpRequest) {
        List<String> contentTypeHeaders = serverHttpRequest.getHeaders()
                .get(HttpHeaders.CONTENT_TYPE)
                .stream()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(contentTypeHeaders)) {
            return EMPTY_STRING;
        }

        return contentTypeHeaders.get(0).toLowerCase();
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


//    public Set<String> validateRequestAndSetRequestTenantId() {
//
//        RequestContext ctx = RequestContext.getCurrentContext();
//
//        return getTenantIdForRequest(ctx);
//    }

//    private Set<String> getTenantIdForRequest(RequestContext ctx) {
//        HttpServletRequest request = ctx.getRequest();
//        Map<String, String[]> queryParams = request.getParameterMap();
//
//        Set<String> tenantIds = new HashSet<>();
//
//
//        if (Utils.isRequestBodyCompatible(request)) {
//
//            try {
//                ObjectNode requestBody = (ObjectNode) objectMapper.readTree(request.getInputStream());
//
//                stripRequestInfo(requestBody);
//
//                List<String> tenants = new LinkedList<>();
//
//                for (JsonNode node : requestBody.findValues(REQUEST_TENANT_ID_KEY)) {
//                    if (node.getNodeType() == JsonNodeType.ARRAY)
//                    {
//                        node.elements().forEachRemaining(n -> tenants.add(n.asText()));
//                    } else if (node.getNodeType() == JsonNodeType.STRING) {
//                        tenants.add(node.asText());
//                    }
//                }
//                if (!tenants.isEmpty()) {
//                    /*
//                     * Filtering null tenantids will be removed once fix is done in TL service.
//                     */
//                    tenants.forEach(tenant -> {
//                        if (tenant != null && !tenant.equalsIgnoreCase("null"))
//                            tenantIds.add(tenant);
//                    });
//                }
//            } catch (IOException e) {
//                throw new RuntimeException( new CustomException("REQUEST_PARSE_FAILED", HttpStatus.UNAUTHORIZED.value() ,"Failed to parse request at" +
//                        " API gateway"));
//            }
//        }
//
//        if (tenantIds.isEmpty()) {
//            setTenantIdsFromQueryParams(queryParams, tenantIds);
//            tenantIds.add(((User) ctx.get(USER_INFO_KEY)).getTenantId());
//        }
//
//        return tenantIds;
//    }

//    private void setTenantIdsFromQueryParams(Map<String, String[]> queryParams, Set<String> tenantIds) {
//
//        if (!isNull(queryParams) && queryParams.containsKey(REQUEST_TENANT_ID_KEY)
//                && queryParams.get(REQUEST_TENANT_ID_KEY).length > 0) {
//            String tenantId = queryParams.get(REQUEST_TENANT_ID_KEY)[0];
//            if (tenantId.contains(",")) {
//                tenantIds.addAll(Arrays.asList(tenantId.split(",")));
//            } else
//                tenantIds.add(tenantId);
//        }
//    }
//
//    private void stripRequestInfo(ObjectNode requestBody) {
//        if (requestBody.has(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))
//            requestBody.remove(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);
//
//        else if (requestBody.has(REQUEST_INFO_FIELD_NAME_CAMEL_CASE))
//            requestBody.remove(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);
//
//    }
//
//    /**
//     * Picks the lowest level tenantId from the set of state all levels of tenants
//     *
//     * @param tenants
//     * @return
//     */
////    public String getLowLevelTenatFromSet(Set<String> tenants) {
////
////        String lowLevelTenant = null;
////        int countOfSubTenantsPresent = 0;
////
////        for (String tenant : tenants) {
////            int currentCount = tenant.split("\\.").length;
////            if (currentCount >= countOfSubTenantsPresent) {
////                countOfSubTenantsPresent = currentCount;
////                lowLevelTenant = tenant;
////            }
////        }
////        return lowLevelTenant;
////    }
}
