package com.example.gateway.utils;

import com.example.gateway.exception.UserDetailsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.GATEWAY_UNEXPECTED_ERROR_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.INVALID_ACCESS_TOKEN_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.NOSNIFF;
import static com.example.gateway.constants.GatewayConstants.REQUEST_INFO_FIELD_NAME_PASCAL_CASE;
import static com.example.gateway.constants.GatewayConstants.X_CONTENT_TYPE_OPTIONS_HEADER;

public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * Serialises error bodies with HTML-significant characters written as JSON unicode escapes (for example '<' becomes \\u003C).
     * JSON clients read back exactly the same strings, but no raw markup can ever appear in a gateway error
     * response, whatever text reaches it.
     */
    private static final ObjectMapper ERROR_BODY_MAPPER = new ObjectMapper();

    static {
        ERROR_BODY_MAPPER.getFactory().setCharacterEscapes(new HtmlSafeCharacterEscapes());
    }

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
                logger.error("Unexpected error at the API gateway", e);
                return _setExceptionBody(exchange,HttpStatus.INTERNAL_SERVER_ERROR, getErrorInfoObject(exceptionName, GATEWAY_UNEXPECTED_ERROR_MESSAGE, GATEWAY_UNEXPECTED_ERROR_MESSAGE));
            } else if (exceptionName.equalsIgnoreCase("HttpClientErrorException")) {
                String existingResponse = ((HttpClientErrorException) e).getResponseBodyAsString();
                if (existingResponse.contains("InvalidAccessTokenException"))
                    return _setExceptionBody(exchange,HttpStatus.UNAUTHORIZED, existingResponse);
                else
                    return _setExceptionBody(exchange,(HttpStatus) ((HttpClientErrorException) e).getStatusCode(), existingResponse);
            } else if (exceptionName.equalsIgnoreCase("InvalidAccessTokenException")) {
                return _setExceptionBody(exchange,HttpStatus.UNAUTHORIZED, getErrorInfoObject(exceptionName, INVALID_ACCESS_TOKEN_MESSAGE, INVALID_ACCESS_TOKEN_MESSAGE));
            } else if (exceptionName.equalsIgnoreCase("RateLimitExceededException")) {
                return _setExceptionBody(exchange,HttpStatus.TOO_MANY_REQUESTS, getErrorInfoObject(exceptionName, "Rate limit exceeded", null));
            } else if (exceptionName.equalsIgnoreCase("JsonParseException")) {
                return _setExceptionBody(exchange,HttpStatus.BAD_REQUEST, getErrorInfoObject(exceptionName, "Bad request", null));
            } else if (e instanceof UserDetailsException ude) {
                return _setExceptionBody(exchange, HttpStatus.UNAUTHORIZED, getErrorInfoObject(ude.getCode(), ude.getMessage(), ude.getDescription()));
            } else if (exceptionName.equalsIgnoreCase("CustomException")) {
                CustomException ce = (CustomException) e;
//                HttpStatus.valueOf(ce.getCode());
                return _setExceptionBody(exchange,HttpStatus.valueOf(401), getErrorInfoObject(exceptionName, exceptionMessage, exceptionMessage));
            } else {
                logUnexpected(e);
                return _setExceptionBody(exchange,HttpStatus.INTERNAL_SERVER_ERROR, getErrorInfoObject(exceptionName, GATEWAY_UNEXPECTED_ERROR_MESSAGE, GATEWAY_UNEXPECTED_ERROR_MESSAGE));
            }
        } catch (Exception e1) {
            logger.error("Exception while raising error filter exception: " + e1.getMessage());
        }
        return null;
    }

    /** Client-caused failures (malformed body, bad media type) at WARN without a stack trace; the rest at ERROR. */
    private static void logUnexpected(Throwable e) {
        if (e instanceof DecodingException || e instanceof ServerWebInputException || e instanceof InvalidMediaTypeException)
            logger.warn("Rejected request at the API gateway: {}", e.getClass().getSimpleName());
        else
            logger.error("Unexpected error at the API gateway", e);
    }

    private static Mono<Void> _setExceptionBody(ServerWebExchange exchange , HttpStatus status, Object body) throws JsonProcessingException {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        if (!response.isCommitted()) {
            HttpHeaders headers = response.getHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(X_CONTENT_TYPE_OPTIONS_HEADER, NOSNIFF);
        }
        return response.writeWith(Mono.just(response
                .bufferFactory().wrap(getObjectJSONString(body).getBytes(StandardCharsets.UTF_8))));

    }

    private static String getObjectJSONString(Object obj) throws JsonProcessingException {
        return ERROR_BODY_MAPPER.writeValueAsString(obj);
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

    /** Escapes < > & ' as \\u003C \\u003E \\u0026 \\u0027 on top of Jackson's standard JSON escaping. */
    private static final class HtmlSafeCharacterEscapes extends CharacterEscapes {

        private final int[] asciiEscapes;

        HtmlSafeCharacterEscapes() {
            int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
            escapes['<'] = CharacterEscapes.ESCAPE_STANDARD;
            escapes['>'] = CharacterEscapes.ESCAPE_STANDARD;
            escapes['&'] = CharacterEscapes.ESCAPE_STANDARD;
            escapes['\''] = CharacterEscapes.ESCAPE_STANDARD;
            this.asciiEscapes = escapes;
        }

        @Override
        public int[] getEscapeCodesForAscii() {
            return asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    }

}
