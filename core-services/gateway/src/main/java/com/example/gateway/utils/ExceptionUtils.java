package com.example.gateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;

public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    public static Mono<Void> raiseErrorFilterException(ServerWebExchange exchange , Throwable e) {

        try {
            if (e == null) {
                HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                if (status == HttpStatus.NOT_FOUND) {
                    return _setExceptionBody(exchange,HttpStatus.NOT_FOUND, getErrorInfoObject("ResourceNotFoundException",
                            "The resource - " + exchange.getRequest().getPath() + " not found", null));
                } else if (status == HttpStatus.BAD_REQUEST) {
                    String existingResponse = exchange.getResponse().toString();
                    if (existingResponse != null && existingResponse.contains("InvalidAccessTokenException"))
                        return _setExceptionBody(exchange,HttpStatus.UNAUTHORIZED, existingResponse);
                }
                return null;
            }

            while ((e instanceof NotFoundException || e instanceof ResponseStatusException) && e.getCause() != null)
                e = e.getCause();

            String exceptionName = e.getClass().getSimpleName();
            String exceptionMessage = e.getMessage();

            if (exceptionName.equalsIgnoreCase("HttpHostConnectException") ||
                    exceptionName.equalsIgnoreCase("ResourceAccessException")) {
                return _setExceptionBody(exchange,HttpStatus.BAD_GATEWAY, getErrorInfoObject(exceptionName, "The backend service is unreachable", null));
            } else if (exceptionName.equalsIgnoreCase("NullPointerException")) {
                e.printStackTrace();
                return _setExceptionBody(exchange,HttpStatus.INTERNAL_SERVER_ERROR, getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage));
            } else if (exceptionName.equalsIgnoreCase("HttpClientErrorException")) {
                String existingResponse = ((HttpClientErrorException) e).getResponseBodyAsString();
                if (existingResponse.contains("InvalidAccessTokenException"))
                    return _setExceptionBody(exchange,HttpStatus.UNAUTHORIZED, existingResponse);
                else
                    return _setExceptionBody(exchange,(HttpStatus) ((HttpClientErrorException) e).getStatusCode(), existingResponse);
            } else if (exceptionName.equalsIgnoreCase("InvalidAccessTokenException")) {
                return _setExceptionBody(exchange,HttpStatus.UNAUTHORIZED, getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage));
            } else if (exceptionName.equalsIgnoreCase("RateLimitExceededException")) {
                return _setExceptionBody(exchange,HttpStatus.TOO_MANY_REQUESTS, getErrorInfoObject(exceptionName, "Rate limit exceeded", null));
            } else if (exceptionName.equalsIgnoreCase("JsonParseException")) {
                return _setExceptionBody(exchange,HttpStatus.BAD_REQUEST, getErrorInfoObject(exceptionName, "Bad request", null));
            } else if (exceptionName.equalsIgnoreCase("CustomException")) {
                CustomException ce = (CustomException) e;
//                HttpStatus.valueOf(ce.getCode());
                return _setExceptionBody(exchange,HttpStatus.valueOf(401), getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage));
            } else {
                return _setExceptionBody(exchange,HttpStatus.INTERNAL_SERVER_ERROR, getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage));
            }
        } catch (Exception e1) {
            logger.error("Exception while raising error filter exception: " + e1.getMessage());
        }
        return null;
    }

    private static Mono<Void> _setExceptionBody(ServerWebExchange exchange , HttpStatus status, Object body) throws JsonProcessingException {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(getObjectJSONString(body).getBytes())));

    }

    private static String getObjectJSONString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private static HashMap<String, Object> getErrorInfoObject(String code, String message, String description) {
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
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HashMap<String, Object> errorInfo = objectMapper.readValue(errorTemplate, new TypeReference<HashMap<String, Object>>() {
            });
            HashMap<String, Object> error = (HashMap<String, Object>) ((List<Object>) errorInfo.get("Errors")).get(0);
            error.put("code", code);
            error.put("message", message);
            error.put("description", description);
            return errorInfo;
        } catch (IOException e) {
            logger.error("IO Exception while getting errorInfo object: " + e.getMessage());
        }

        return null;
    }

}
