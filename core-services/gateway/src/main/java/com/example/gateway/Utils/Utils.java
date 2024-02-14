package com.example.gateway.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

@Component
public class Utils {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REQUEST_TENANT_ID_KEY = "tenantId";

    public Set<String> validateRequestAndSetRequestTenantId(ServerWebExchange exchange) {

        return getTenantIdForRequest(exchange);
    }

    private Set<String> getTenantIdForRequest(ServerWebExchange exchange) {

        Set<String> tenantIds = new HashSet<>();

        if (isRequestBodyCompatible(exchange)) {
            exchange.getRequest().getBody()
                    .flatMap(body -> {
                        try {
                            ObjectNode requestBody = (ObjectNode) objectMapper.readTree(body.asInputStream());
                            stripRequestInfo(requestBody);
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
                                    if (tenant != null && !tenant.equalsIgnoreCase("null")) {
                                        tenantIds.add(tenant);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            // Handle exception
                        }
                        return Mono.empty();
                    }).subscribe();
        }

        if (tenantIds.isEmpty()) {
            setTenantIdsFromQueryParams(exchange.getRequest().getQueryParams(), tenantIds);
            // You may need to adapt this part based on your specific context
            // tenantIds.add(((User) ctx.get(USER_INFO_KEY)).getTenantId());
        }

        return tenantIds;
    }

    private void setTenantIdsFromQueryParams(Map<String, List<String>> queryParams, Set<String> tenantIds) {
        if (queryParams.containsKey(REQUEST_TENANT_ID_KEY) && !queryParams.get(REQUEST_TENANT_ID_KEY).isEmpty()) {
            String tenantId = queryParams.get(REQUEST_TENANT_ID_KEY).get(0);
            if (tenantId.contains(",")) {
                tenantIds.addAll(Arrays.asList(tenantId.split(",")));
            } else {
                tenantIds.add(tenantId);
            }
        }
    }

    private void stripRequestInfo(ObjectNode requestBody) {
        // Adjust this part according to your requirements
        // This method seems to remove specific fields from the request body
    }

    public static boolean isRequestBodyCompatible(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        return (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) &&
                exchange.getRequest().getHeaders().getContentType() != null &&
                exchange.getRequest().getHeaders().getContentType().includes(MediaType.APPLICATION_JSON);
    }
    public String getLowLevelTenatFromSet(Set<String> tenants) {

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
}
