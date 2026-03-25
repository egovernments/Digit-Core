package org.egov.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.contract.User;
import org.springframework.web.server.ServerWebExchange;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.egov.constants.RequestContextConstants.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EventLogRequest {

    Object requestBody;
    Object responseBody;
    String method;
    String referer;
    String url;
    String responseContentType;
    String queryParams;
    Integer uid;
    String username;
    int statusCode;
    String timestamp;
    String userType;
    Long requestDuration;
    String correlationId;
    String userTenantId;
    String userId;
    String tenantId;

    public static EventLogRequest fromExchange(ServerWebExchange exchange, RequestCaptureCriteria criteria) {
        Object body = null;
        if (criteria.isCaptureInputBody()) {
            String sanitizedBody = exchange.getAttribute(CURRENT_REQUEST_SANITIZED_BODY_STR);
            if (sanitizedBody != null) {
                body = sanitizedBody;
            } else {
                byte[] cachedBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
                if (cachedBody != null) {
                    body = new String(cachedBody);
                }
            }
        }

        String referer = exchange.getRequest().getHeaders().getFirst("referer");
        String method = exchange.getRequest().getMethod().name();
        Long startTime = exchange.getAttribute(CURRENT_REQUEST_START_TIME);
        Long endTime = System.currentTimeMillis();
        exchange.getAttributes().put(CURRENT_REQUEST_END_TIME, endTime);

        int statusCode = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value() : 0;

        User user = exchange.getAttribute(USER_INFO_KEY);

        String uuid = "";
        String userTypeVal = "";
        String userTenantId = "";
        String userName = "";
        Integer userId = 0;

        if (user != null) {
            uuid = user.getUuid();
            userTypeVal = user.getType();
            userTenantId = user.getTenantId();
            userName = user.getUserName();
            userId = user.getId();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        Date date = new Date(startTime != null ? startTime : System.currentTimeMillis());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String correlationId = exchange.getAttribute(CORRELATION_ID_KEY);
        String tenantId = exchange.getAttribute(CURRENT_REQUEST_TENANTID);

        return EventLogRequest.builder()
                .requestBody(body)
                .method(method)
                .referer(referer)
                .username(userName)
                .uid(userId)
                .userType(userTypeVal)
                .queryParams(exchange.getRequest().getURI().getQuery())
                .correlationId(correlationId)
                .statusCode(statusCode)
                .timestamp(formatter.format(date))
                .requestDuration(endTime - (startTime != null ? startTime : endTime))
                .userId(uuid)
                .userTenantId(userTenantId)
                .tenantId(tenantId)
                .url(exchange.getRequest().getURI().getPath())
                .build();
    }
}
