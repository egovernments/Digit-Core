package org.egov.Utils;

import static java.util.Objects.isNull;
import static org.egov.constants.RequestContextConstants.*;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.contract.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class Utils {

    @Autowired
    private ObjectMapper objectMapper;

    public static final String JSON_TYPE = "json";

    /**
     * Checks if the request method is POST/PUT/PATCH and content type is JSON.
     */
    public static boolean isRequestBodyCompatible(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        boolean methodOk = HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.PATCH.equals(method);
        if (!methodOk) return false;

        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) return false;
        return contentType.toString().toLowerCase().contains(JSON_TYPE);
    }

    /**
     * Extract tenant IDs from the cached request body (already parsed as bytes) and query params.
     */
    public Set<String> extractTenantIdsFromBody(byte[] bodyBytes) {
        Set<String> tenantIds = new HashSet<>();
        if (bodyBytes == null || bodyBytes.length == 0) return tenantIds;

        try {
            ObjectNode requestBody = (ObjectNode) objectMapper.readTree(bodyBytes);
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
                    if (tenant != null && !tenant.equalsIgnoreCase("null"))
                        tenantIds.add(tenant);
                });
            }
        } catch (Exception e) {
            // ignore parse errors for tenant extraction
        }
        return tenantIds;
    }

    /**
     * Extract tenant IDs from query parameters.
     */
    public Set<String> extractTenantIdsFromQueryParams(ServerHttpRequest request) {
        Set<String> tenantIds = new HashSet<>();
        List<String> values = request.getQueryParams().get(REQUEST_TENANT_ID_KEY);
        if (values != null && !values.isEmpty()) {
            String tenantId = values.get(0);
            if (tenantId.contains(",")) {
                tenantIds.addAll(Arrays.asList(tenantId.split(",")));
            } else {
                tenantIds.add(tenantId);
            }
        }
        return tenantIds;
    }

    /**
     * Validates the request and extracts tenant IDs from body + query params + user info.
     */
    public Set<String> validateRequestAndSetRequestTenantId(ServerWebExchange exchange, byte[] bodyBytes) {
        ServerHttpRequest request = exchange.getRequest();

        Set<String> tenantIds = new HashSet<>();

        if (isRequestBodyCompatible(request) && bodyBytes != null && bodyBytes.length > 0) {
            tenantIds.addAll(extractTenantIdsFromBody(bodyBytes));
        }

        if (tenantIds.isEmpty()) {
            tenantIds.addAll(extractTenantIdsFromQueryParams(request));
            User user = exchange.getAttribute(USER_INFO_KEY);
            if (user != null && user.getTenantId() != null) {
                tenantIds.add(user.getTenantId());
            }
        }

        return tenantIds;
    }

    private void stripRequestInfo(ObjectNode requestBody) {
        if (requestBody.has(REQUEST_INFO_FIELD_NAME_PASCAL_CASE))
            requestBody.remove(REQUEST_INFO_FIELD_NAME_PASCAL_CASE);
        else if (requestBody.has(REQUEST_INFO_FIELD_NAME_CAMEL_CASE))
            requestBody.remove(REQUEST_INFO_FIELD_NAME_CAMEL_CASE);
    }

    /**
     * Picks the lowest level tenantId from the set of tenants (deepest in hierarchy).
     */
    public String getLowLevelTenantFromSet(Set<String> tenants) {
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
