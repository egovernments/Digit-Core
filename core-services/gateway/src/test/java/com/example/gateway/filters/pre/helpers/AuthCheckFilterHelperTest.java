package com.example.gateway.filters.pre.helpers;

import com.example.gateway.exception.UserDetailsException;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.HashMap;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.AUTHENTICATION_FAILED_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.INVALID_ACCESS_TOKEN_MESSAGE;
import static com.example.gateway.constants.GatewayConstants.USER_FETCH_FAILURE_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthCheckFilterHelperTest {

    private UserUtils userUtils;
    private AuthCheckFilterHelper helper;

    @BeforeEach
    void setUp() {
        userUtils = mock(UserUtils.class);
        helper = new AuthCheckFilterHelper(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false), userUtils);
    }

    private Throwable apply() {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("authToken", "t");
        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        return assertThrows(RuntimeException.class,
                () -> helper.apply(MockServerWebExchange.from(MockServerHttpRequest.post("/x")), body));
    }

    @Test
    void invalid_token_failure_from_user_utils_is_rethrown_unchanged() {
        CustomException fromUserUtils = new CustomException(USER_FETCH_FAILURE_CODE, INVALID_ACCESS_TOKEN_MESSAGE);
        when(userUtils.getUser(anyString(), any())).thenThrow(fromUserUtils);
        assertSame(fromUserUtils, apply());
    }

    @Test
    void user_details_exception_is_rethrown_unchanged() {
        UserDetailsException fromUserUtils = new UserDetailsException("sso.id_token.missing", "ID token is required", "d");
        when(userUtils.getUser(anyString(), any())).thenThrow(fromUserUtils);
        assertSame(fromUserUtils, apply());
    }

    @Test
    void any_other_failure_gets_a_fixed_message() {
        when(userUtils.getUser(anyString(), any())).thenThrow(new IllegalStateException("<script>alert(1)</script>"));
        CustomException e = (CustomException) apply();
        assertEquals("AUTHENTICATION_ERROR", e.getCode());
        assertEquals(AUTHENTICATION_FAILED_MESSAGE, e.getMessage());
    }
}
