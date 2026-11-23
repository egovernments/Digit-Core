package org.egov.user.web.controller;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.service.UserSessionService;
import org.egov.user.web.contract.RevokeSessionRequest;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers AC9 (admin revoke immediately frees the user ID) and validation item 12
 * (admin can revoke an active session) at the controller/API level, complementing the
 * service-level coverage already in UserSessionServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSessionControllerTest {

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    private UserSessionController userSessionController;

    private static final String USER_UUID = "user-uuid-1";
    private static final String TENANT_ID = "pb.amritsar";
    private static final String ADMIN_UUID = "admin-uuid-1";

    @Before
    public void setUp() {
        userSessionController = new UserSessionController(userSessionService, responseInfoFactory);
    }

    // Backward compatibility: a single userUuid string uses the original revoke() path and
    // response shape (ResponseInfo only), and never goes through the bulk path.
    @Test
    public void test_should_revoke_session_and_return_ok_with_actor_from_requestInfo() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        ResponseInfo responseInfo = ResponseInfo.builder().status("successful").build();
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), eq(true)))
                .thenReturn(responseInfo);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singleton("ResponseInfo"), response.getBody().keySet());
        assertEquals(responseInfo, response.getBody().get("ResponseInfo"));
        verify(userSessionService).revoke(USER_UUID, TENANT_ID, ADMIN_UUID);
        verify(userSessionService, never()).revokeAll(any(List.class), any(String.class), any(String.class));
    }

    // No caller identity on the request: falls back to a sentinel actor rather than failing.
    @Test
    public void test_should_fall_back_to_unknown_admin_when_requestInfo_missing() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        request.setRequestInfo(null);

        userSessionController.revoke(request);

        verify(userSessionService).revoke(USER_UUID, TENANT_ID, "UNKNOWN_ADMIN");
    }

    @Test
    public void test_should_fall_back_to_unknown_admin_when_userInfo_missing() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        request.setRequestInfo(RequestInfo.builder().build());

        userSessionController.revoke(request);

        verify(userSessionService).revoke(USER_UUID, TENANT_ID, "UNKNOWN_ADMIN");
    }

    // Backward compatibility: single-user revoke with no active session must not silently
    // succeed. The exception propagates to the standard error handler, as before bulk support.
    @Test(expected = CustomException.class)
    public void test_should_propagate_exception_when_no_active_session_to_revoke() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        doThrow(new CustomException("NO_ACTIVE_SESSION", "No active session found"))
                .when(userSessionService).revoke(eq(USER_UUID), eq(TENANT_ID), any(String.class));

        userSessionController.revoke(request);
    }

    // Bulk: every user revoked -> 200.
    @Test
    public void test_bulk_revoke_all_succeeded_returns_ok() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        request.setUserUuid(Arrays.asList("u1", "u2", "u1"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revoked", Arrays.asList("u1", "u2"));
        result.put("failed", Collections.emptyList());
        when(userSessionService.revokeAll(Arrays.asList("u1", "u2"), TENANT_ID, ADMIN_UUID)).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Arrays.asList("ResponseInfo", "revoked", "failed"), new ArrayList<>(response.getBody().keySet()));
        verify(responseInfoFactory).createResponseInfoFromRequestInfo(any(RequestInfo.class), eq(true));
        verify(userSessionService, never()).revoke(any(String.class), any(String.class), any(String.class));
    }

    // Bulk: some users failed -> 207 Multi-Status with per-user outcomes, ResponseInfo not successful.
    @Test
    public void test_bulk_revoke_with_failures_returns_multi_status() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        request.setUserUuid(Arrays.asList("u1", "u2"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revoked", Collections.singletonList("u1"));
        result.put("failed", Collections.singletonList(Collections.singletonMap("userUuid", "u2")));
        when(userSessionService.revokeAll(Arrays.asList("u1", "u2"), TENANT_ID, ADMIN_UUID)).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.MULTI_STATUS, response.getStatusCode());
        assertEquals(result.get("revoked"), response.getBody().get("revoked"));
        assertEquals(result.get("failed"), response.getBody().get("failed"));
        verify(responseInfoFactory).createResponseInfoFromRequestInfo(any(RequestInfo.class), eq(false));
    }

    private RevokeSessionRequest revokeRequest(String adminUuid) {
        org.egov.common.contract.request.User adminInfo = org.egov.common.contract.request.User.builder()
                .uuid(adminUuid).build();
        RevokeSessionRequest request = new RevokeSessionRequest();
        request.setUserUuid(USER_UUID);
        request.setTenantId(TENANT_ID);
        request.setRequestInfo(RequestInfo.builder().userInfo(adminInfo).build());
        return request;
    }
}
