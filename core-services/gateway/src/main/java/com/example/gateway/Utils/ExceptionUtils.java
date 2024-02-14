package com.example.gateway.Utils;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;

@Component
public class ExceptionUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    private static String getObjectJSONString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private static HashMap<String, Object> getErrorInfoObject(String code, String message, String description) {
        HashMap<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("ResponseInfo", null);

        HashMap<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("description", description);

        List<HashMap<String, Object>> errors = List.of(error);
        errorInfo.put("Errors", errors);

        return errorInfo;
    }

    public static void setCustomException(HttpStatus status, String message) {
        try {
            _setExceptionBody(status, getErrorInfoObject("CustomException", message, message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static void _setExceptionBody(HttpStatus status, Object body) throws JsonProcessingException {
        _setExceptionBody(status, getObjectJSONString(body));
    }

    private static void _setExceptionBody(HttpStatus status, String body) {
        // Handle setting response body based on Spring Cloud Gateway's ServerWebExchange
    }

    public static void RaiseException(Throwable ex) {
        throw new RuntimeException(ex);
    }

    public static void raiseCustomException(HttpStatus status, String message) {
        throw new RuntimeException(new CustomException());
    }

    public static void raiseErrorFilterException(ServerWebExchange exchange) {
        // Handle raising error filter exception based on Spring Cloud Gateway's ServerWebExchange
    }
}
