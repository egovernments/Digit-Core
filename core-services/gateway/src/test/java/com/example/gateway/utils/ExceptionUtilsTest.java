package com.example.gateway.utils;

import com.example.gateway.exception.UserDetailsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static com.example.gateway.constants.GatewayConstants.GATEWAY_UNEXPECTED_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionUtilsTest {

    private static final String PAYLOAD = "135ta8ws<script>alert(1)</script>whlq4";

    private final ObjectMapper mapper = new ObjectMapper();

    private MockServerHttpResponse raise(Throwable e) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/access/v1/actions/mdms/_get"));
        ExceptionUtils.raiseErrorFilterException(exchange, e).block();
        return exchange.getResponse();
    }

    private static String body(MockServerHttpResponse response) {
        return response.getBodyAsString().block();
    }

    private JsonNode firstError(MockServerHttpResponse response) throws Exception {
        return mapper.readTree(body(response)).path("Errors").path(0);
    }

    private static void assertSecurityHeaders(MockServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(headers.getContentType()));
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
    }

    private static void assertNoRawMarkup(String body) {
        assertFalse(body.contains("<"), body);
        assertFalse(body.contains(">"), body);
    }

    @Test
    void custom_exception_keeps_status_and_text_but_never_emits_raw_markup() throws Exception {
        MockServerHttpResponse response = raise(new CustomException("SOME_CODE", "value " + PAYLOAD));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertSecurityHeaders(response);
        assertNoRawMarkup(body(response));
        // lossless for JSON clients: the parsed text is unchanged
        assertEquals("value " + PAYLOAD, firstError(response).path("message").asText());
        assertEquals("CustomException", firstError(response).path("code").asText());
    }

    @Test
    void unexpected_exception_returns_fixed_text_not_its_message() throws Exception {
        MockServerHttpResponse response = raise(new IllegalArgumentException("Cannot deserialize value of type `java.lang.Long` from String \"" + PAYLOAD + "\""));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertSecurityHeaders(response);
        String body = body(response);
        assertFalse(body.contains("135ta8ws"), body);
        assertFalse(body.contains("alert(1)"), body);
        assertEquals(GATEWAY_UNEXPECTED_ERROR_MESSAGE, firstError(response).path("message").asText());
        assertEquals(GATEWAY_UNEXPECTED_ERROR_MESSAGE, firstError(response).path("description").asText());
        assertEquals("IllegalArgumentException", firstError(response).path("code").asText());
    }

    @Test
    void null_pointer_exception_returns_fixed_text() throws Exception {
        MockServerHttpResponse response = raise(new NullPointerException("Cannot invoke \"" + PAYLOAD + "\""));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(body(response).contains("135ta8ws"));
        assertEquals(GATEWAY_UNEXPECTED_ERROR_MESSAGE, firstError(response).path("message").asText());
    }

    @Test
    void generic_error_text_does_not_trigger_the_ui_maintenance_redirect() {
        String text = GATEWAY_UNEXPECTED_ERROR_MESSAGE.toLowerCase();
        assertFalse(text.contains("internal server error"));
        assertFalse(text.contains("some error occured"));
    }

    @Test
    void user_details_exception_passes_egov_user_fields_through_escaped() throws Exception {
        MockServerHttpResponse response = raise(new UserDetailsException("InvalidAccessTokenException",
                "InvalidAccessTokenException <img src=x onerror=alert(1)>", "d & 'e'"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertSecurityHeaders(response);
        assertNoRawMarkup(body(response));
        JsonNode error = firstError(response);
        assertEquals("InvalidAccessTokenException", error.path("code").asText());
        assertEquals("InvalidAccessTokenException <img src=x onerror=alert(1)>", error.path("message").asText());
        assertEquals("d & 'e'", error.path("description").asText());
    }

    @Test
    void passed_through_downstream_error_body_is_escaped() {
        HttpClientErrorException e = HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", null,
                ("{\"Errors\":[{\"message\":\"" + PAYLOAD + "\"}]}").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        MockServerHttpResponse response = raise(e);

        assertSecurityHeaders(response);
        assertNoRawMarkup(body(response));
    }


    @Test
    void existing_status_codes_are_unchanged() {
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, raise(new com.example.gateway.utils.ExceptionUtilsTest.RateLimitExceededException()).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, raise(new org.springframework.web.client.ResourceAccessException("down")).getStatusCode());
    }

    /** Stand-in with the simple name the gateway dispatches on. */
    static class RateLimitExceededException extends RuntimeException {
    }
}
