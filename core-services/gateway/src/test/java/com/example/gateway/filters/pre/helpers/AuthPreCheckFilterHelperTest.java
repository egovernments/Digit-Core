package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import com.example.gateway.utils.CommonUtils;
import com.example.gateway.utils.ExceptionUtils;
import com.example.gateway.utils.UserUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.example.gateway.constants.GatewayConstants.INVALID_REQUEST_INFO_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Reproduces the WHO AFRO VAPT requests (RequestInfo.userInfo.id carrying a script). */
class AuthPreCheckFilterHelperTest {

    private AuthPreCheckFilterHelper helper;

    @BeforeEach
    void setUp() {
        // Spring Boot's auto-configured mapper ignores unknown properties
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ApplicationProperties props = mock(ApplicationProperties.class);
        when(props.getOpenEndpointsWhitelist()).thenReturn(Collections.emptyList());
        when(props.getMixedModeEndpointsWhitelist()).thenReturn(java.util.List.of("/user/_search", "/access/v1/actions/mdms/_get"));
        helper = new AuthPreCheckFilterHelper(objectMapper, mock(MultiStateInstanceUtil.class), mock(UserUtils.class),
                props, mock(CommonUtils.class));
    }

    private static Map<String, Object> vaptBody(String userInfoId) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userInfoId);
        userInfo.put("uuid", "7ca04d28-90f5-4259-a341-e07d99e08e18");
        userInfo.put("type", "EMPLOYEE");
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("apiId", "Rainmaker");
        requestInfo.put("authToken", "some-token");
        requestInfo.put("userInfo", userInfo);
        requestInfo.put("plainAccessRequest", new HashMap<>());
        Map<String, Object> body = new HashMap<>();
        body.put("tenantId", "chaduat");
        body.put("RequestInfo", requestInfo);
        return body;
    }

    private CustomException reject(String path, String userInfoId) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));
        return assertThrows(CustomException.class, () -> helper.apply(exchange, vaptBody(userInfoId)));
    }

    @Test
    void f1_access_control_payload_is_not_echoed() {
        CustomException e = reject("/access/v1/actions/mdms/_get", "135ta8ws<script>alert(1)</script>whlq4");
        assertEquals(INVALID_REQUEST_INFO_MESSAGE, e.getMessage());
        assertFalse(e.getMessage().contains("alert(1)"));
    }

    @Test
    void f1b_user_search_payload_is_not_echoed_end_to_end() {
        CustomException e = reject("/user/_search", "135r6kfl<script>alert(1)</script>tnj7b");

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/user/_search"));
        ExceptionUtils.raiseErrorFilterException(exchange, e).block();
        String responseBody = exchange.getResponse().getBodyAsString().block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(responseBody.contains("135r6kfl"), responseBody);
        assertFalse(responseBody.contains("alert(1)"), responseBody);
    }

    @Test
    void numeric_user_info_id_still_passes() {
        Map<String, Object> body = vaptBody("42");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/user/_search"));
        assertEquals(body, reactor.core.publisher.Mono.from(helper.apply(exchange, body)).block());
    }
}
