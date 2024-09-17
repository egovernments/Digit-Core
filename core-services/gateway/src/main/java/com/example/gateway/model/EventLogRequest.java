package com.example.gateway.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.gateway.constants.GatewayConstants.*;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EventLogRequest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    public static Object getResponseBody(Map body) {
        System.out.println(body);
        return body;
    }

    private static Boolean isJsonResponse(ServerWebExchange exchange) {
        MediaType contentType = exchange.getResponse().getHeaders().getContentType();
        return contentType != null && contentType.includes(MediaType.APPLICATION_JSON);
    }

    public static EventLogRequest fromRequestContext(ServerWebExchange exchange, Map body, RequestCaptureCriteria criteria) {

        Object reqBody = null;
        if (criteria.isCaptureInputBody()) {
            reqBody = body.get(CURRENT_REQUEST_SANITIZED_BODY_STR);

            if (body == null) {
                reqBody = exchange.getRequest().getBody().toString();
            }
        }


        String referer = exchange.getRequest().getHeaders().getFirst("referer");
        String method = exchange.getRequest().getMethod().toString();
        Long startTime = exchange.getAttribute("CURRENT_REQUEST_START_TIME");
        Long endTime = System.currentTimeMillis();
        exchange.getAttributes().put(CURRENT_REQUEST_END_TIME, endTime);

        Object responseBody = null;
        int statusCode = exchange.getResponse().getStatusCode().value();
        boolean isErrorStatusCode = !(statusCode >= 200 && statusCode < 300);
        if (
                criteria.isCaptureOutputBody() ||
                        (criteria.isCaptureOutputBodyOnlyForError() && isErrorStatusCode)
        ) {
            try {
                responseBody = getResponseBody(body);
                if (isJsonResponse(exchange)) {
                    responseBody = objectMapper.readValue((String) responseBody,
                            new TypeReference<HashMap<String, Object>>() {
                            });
                }
            } catch (Exception e) {
                log.error("Exception while reading body", e);
            }
        }

        RequestInfo requestInfo = objectMapper.convertValue(body.get(REQUEST_INFO_FIELD_NAME_PASCAL_CASE), RequestInfo.class);
        User user = requestInfo.getUserInfo();

        String uuid = "";
        String userType = "";
        String userTenantId = "";
        String userName = "";
        Integer userId = 0;

        if (user != null) {
            uuid = user.getUuid();
            userType = user.getType();
            userTenantId = user.getTenantId();
            userName = user.getUserName();
            userId = Math.toIntExact(user.getId());
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        Date date = new Date(startTime);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        EventLogRequest req = EventLogRequest.builder()
                .requestBody(body)
                .method(method)
                .referer(referer)
                .username(userName)
                .uid(userId)
                .userType(userType)
                .responseBody(responseBody)
                .queryParams(exchange.getRequest().getQueryParams().toString())
                .correlationId(exchange.getAttribute(CORRELATION_ID_KEY))
                .statusCode(statusCode)
                .timestamp(formatter.format(date))
                .requestDuration(endTime - startTime)
                .userId(uuid)
                .userTenantId(userTenantId)
                .tenantId(exchange.getAttribute(CURRENT_REQUEST_TENANTID))
                .url(exchange.getRequest().getURI().toString()).build();

        return req;
    }
}
