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
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

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
    }
}
