package org.egov.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.exceptions.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class ExceptionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HashMap<String, Object> getErrorInfoObject(String code, String message, String description) {
        String errorTemplate = "{\n" +
            "    \"ResponseInfo\": null,\n" +
            "    \"Errors\": [\n" +
            "        {\n" +
            "            \"code\": \"Exception\",\n" +
            "            \"message\": null,\n" +
            "            \"description\": null,\n" +
            "            \"params\": null\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        try {
            HashMap<String, Object> errorInfo = objectMapper.readValue(errorTemplate, new TypeReference<HashMap<String, Object>>() {
            });
            @SuppressWarnings("unchecked")
            HashMap<String, Object> error = (HashMap<String, Object>) ((List<Object>) errorInfo.get("Errors")).get(0);
            error.put("code", code);
            error.put("message", message);
            error.put("description", description);
            return errorInfo;
        } catch (Exception e) {
            logger.error("Exception while getting errorInfo object: " + e.getMessage());
        }
        return null;
    }

    /**
     * Write a JSON error response to the exchange and short-circuit the filter chain.
     */
    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        HashMap<String, Object> errorBody = getErrorInfoObject(code, message, message);
        String body;
        try {
            body = errorBody != null ? objectMapper.writeValueAsString(errorBody) : "{}";
        } catch (JsonProcessingException e) {
            body = "{}";
        }
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static void RaiseException(Throwable ex) {
        throw new RuntimeException(ex);
    }

    public static void raiseCustomException(HttpStatus status, String message) {
        throw new RuntimeException(new CustomException(message, status.value(), "CustomException"));
    }
}
