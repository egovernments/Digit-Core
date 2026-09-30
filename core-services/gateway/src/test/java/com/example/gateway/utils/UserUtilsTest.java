package com.example.gateway.utils;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.exception.UserDetailsException;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static com.example.gateway.constants.GatewayConstants.INVALID_ACCESS_TOKEN_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.USER_FETCH_FAILURE_CODE;
import static com.example.gateway.constants.GatewayConstants.USER_FETCH_FAILURE_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.USER_SERVICE_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserUtilsTest {

    private RestTemplate restTemplate;
    private UserUtils userUtils;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        ApplicationProperties props = mock(ApplicationProperties.class);
        when(props.getAuthServiceHost()).thenReturn("http://user");
        when(props.getAuthUri()).thenReturn("/user/_details?access_token=");
        MultiStateInstanceUtil msi = mock(MultiStateInstanceUtil.class);
        when(msi.getIsEnvironmentCentralInstance()).thenReturn(false);
        userUtils = new UserUtils(restTemplate, props, msi);
    }

    private String sentIdToken(MockServerHttpRequest.BaseBuilder<?> request) {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(User.class))).thenReturn(new User());
        userUtils.getUser("at", MockServerWebExchange.from(request));
        ArgumentCaptor<HttpEntity> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(anyString(), entity.capture(), eq(User.class));
        return entity.getValue().getHeaders().getFirst("x-id-token");
    }

    @Test
    void cookie_wins_over_header() {
        assertEquals("fromCookie", sentIdToken(MockServerHttpRequest.post("/x")
                .cookie(new org.springframework.http.HttpCookie("x-id-token", "fromCookie"))
                .header("x-id-token", "fromHeader")));
    }

    @Test
    void header_used_when_no_cookie() {
        assertEquals("fromHeader", sentIdToken(MockServerHttpRequest.post("/x").header("x-id-token", "fromHeader")));
    }

    @Test
    void no_id_token_sends_no_header() {
        assertNull(sentIdToken(MockServerHttpRequest.post("/x")));
    }

    private CustomException failWith(HttpStatus status, String body) {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(User.class))).thenThrow(
                HttpClientErrorException.create(status, status.getReasonPhrase(), null,
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), null));
        return assertThrows(CustomException.class,
                () -> userUtils.getUser("at", MockServerWebExchange.from(MockServerHttpRequest.post("/x"))));
    }

    @Test
    void oauth_shape_error_keeps_egov_user_code() {
        UserDetailsException e = (UserDetailsException) failWith(HttpStatus.UNAUTHORIZED,
                "{\"error\":\"sso.id_token.missing\",\"error_description\":\"ID token is required\"}");
        assertEquals("sso.id_token.missing", e.getCode());
        assertEquals("ID token is required", e.getMessage());
        assertEquals("ID token is required", e.getDescription());
    }

    @Test
    void errors_shape_error_keeps_egov_user_code() {
        UserDetailsException e = (UserDetailsException) failWith(HttpStatus.BAD_REQUEST,
                "{\"ResponseInfo\":null,\"Errors\":[{\"code\":\"InvalidAccessTokenException\",\"message\":\"m\",\"description\":\"Invalid Access Token Exception\"}]}");
        assertEquals("InvalidAccessTokenException", e.getCode());
        assertEquals("m", e.getMessage());
        assertEquals("Invalid Access Token Exception", e.getDescription());
    }

    @Test
    void unparseable_or_empty_body_stays_generic() {
        assertFalse(failWith(HttpStatus.UNAUTHORIZED, null) instanceof UserDetailsException);
        setUp();
        CustomException e = failWith(HttpStatus.UNAUTHORIZED, "<html>nope</html>");
        assertFalse(e instanceof UserDetailsException);
        assertEquals("Exception occurred while fetching user: ", e.getCode());
        assertEquals(USER_FETCH_FAILURE_MESSAGE, e.getMessage());
    }

    private CustomException failWithException(RuntimeException toThrow) {
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(User.class))).thenThrow(toThrow);
        return assertThrows(CustomException.class,
                () -> userUtils.getUser("at", MockServerWebExchange.from(MockServerHttpRequest.post("/x"))));
    }

    // egov-user (Spring Boot 1.5) answers an invalid token with its default 500 error body naming the exception
    private static final String BOOT_DEFAULT_INVALID_TOKEN_BODY = "{\"timestamp\":1,\"status\":500,\"error\":\"Internal Server Error\","
            + "\"exception\":\"org.egov.user.domain.exception.InvalidAccessTokenException\",\"message\":\"<script>alert(1)</script>\","
            + "\"path\":\"/user/_details\"}";

    @Test
    void server_error_naming_invalid_token_keeps_the_logout_marker_without_echo() {
        CustomException e = failWithException(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", null, BOOT_DEFAULT_INVALID_TOKEN_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        assertFalse(e instanceof UserDetailsException);
        assertEquals(USER_FETCH_FAILURE_CODE, e.getCode());
        assertEquals(INVALID_ACCESS_TOKEN_MESSAGE, e.getMessage());
        assertTrue(e.getMessage().contains("InvalidAccessTokenException"));
        assertFalse(e.getMessage().contains("alert(1)"));
    }

    @Test
    void server_error_without_marker_gets_fixed_text() {
        CustomException e = failWithException(HttpServerErrorException.create(HttpStatus.BAD_GATEWAY,
                "Bad Gateway", null, "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        assertEquals(USER_FETCH_FAILURE_CODE, e.getCode());
        assertEquals(USER_FETCH_FAILURE_MESSAGE, e.getMessage());
    }

    @Test
    void unparseable_client_error_naming_invalid_token_keeps_the_logout_marker() {
        CustomException e = failWith(HttpStatus.UNAUTHORIZED, "InvalidAccessTokenException <b>x</b>");
        assertFalse(e instanceof UserDetailsException);
        assertEquals(INVALID_ACCESS_TOKEN_MESSAGE, e.getMessage());
    }

    @Test
    void transport_failure_does_not_echo_the_internal_url() {
        // the message shape RestTemplate produces (query string already stripped)
        CustomException e = failWithException(new ResourceAccessException(
                "I/O error on POST request for \"http://egov-user.egov:8080/user/_details\": Connection refused"));
        assertEquals(USER_FETCH_FAILURE_CODE, e.getCode());
        assertEquals(USER_FETCH_FAILURE_MESSAGE, e.getMessage());
    }

    @Test
    void egov_user_500_keeps_the_ui_maintenance_redirect_without_echo() {
        CustomException e = failWithException(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", null, "<b>stack</b>".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        assertEquals(USER_SERVICE_ERROR_MESSAGE, e.getMessage());
        assertTrue(e.getMessage().toLowerCase().contains("internal server error"));
        assertFalse(e.getMessage().contains("stack"));
    }
}
