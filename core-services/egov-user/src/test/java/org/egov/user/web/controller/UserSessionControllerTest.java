package org.egov.user.web.controller;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
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
        when(userSessionService.revokeAll(any(List.class), any(String.class), any(String.class)))
                .thenReturn(new LinkedHashMap<String, Object>());
    }

    // A single userUuid string is revoked through the same path as a list.
    @Test
    public void test_should_revoke_session_and_return_ok_with_actor_from_requestInfo() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        ResponseInfo responseInfo = new ResponseInfo("", "", System.currentTimeMillis(), "", "", "successful");
        when(responseInfoFactory.createResponseInfoFromRequestInfo(eq(request.getRequestInfo()), eq(true)))
                .thenReturn(responseInfo);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseInfo, response.getBody().get("ResponseInfo"));
        verify(userSessionService).revokeAll(Collections.singletonList(USER_UUID), TENANT_ID, ADMIN_UUID);
    }

    // No caller identity on the request — falls back to a sentinel actor rather than failing
    // the revoke or attributing it to the wrong admin in the audit trail.
    @Test
    public void test_should_fallback_to_unknown_admin_when_requestInfo_has_no_userInfo() {
        RevokeSessionRequest request = new RevokeSessionRequest();
        request.setUserUuid(USER_UUID);
        request.setTenantId(TENANT_ID);
        request.setRequestInfo(RequestInfo.builder().build());

        userSessionController.revoke(request);

        verify(userSessionService).revokeAll(Collections.singletonList(USER_UUID), TENANT_ID, "UNKNOWN_ADMIN");
    }

    @Test
    public void test_should_fallback_to_unknown_admin_when_requestInfo_is_null() {
        RevokeSessionRequest request = new RevokeSessionRequest();
        request.setUserUuid(USER_UUID);
        request.setTenantId(TENANT_ID);

        userSessionController.revoke(request);

        verify(userSessionService).revokeAll(Collections.singletonList(USER_UUID), TENANT_ID, "UNKNOWN_ADMIN");
    }

    // A single user with no active session is reported in "failed", not thrown as an error.
    @Test
    public void test_single_user_without_active_session_is_reported_in_failed() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revoked", Collections.emptyList());
        result.put("failed", Collections.singletonList(Collections.singletonMap("userUuid", USER_UUID)));
        when(userSessionService.revokeAll(Collections.singletonList(USER_UUID), TENANT_ID, ADMIN_UUID)).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody().get("revoked"));
        assertEquals(result.get("failed"), response.getBody().get("failed"));
    }

    // Bulk: userUuid as a list goes through revokeAll and partial outcomes are returned, not thrown.
    @Test
    public void test_should_bulk_revoke_and_return_per_user_result() {
        RevokeSessionRequest request = revokeRequest(ADMIN_UUID);
        request.setUserUuid(Arrays.asList("u1", "u2", "u1"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revoked", Collections.singletonList("u1"));
        result.put("failed", Collections.singletonList(Collections.singletonMap("userUuid", "u2")));
        when(userSessionService.revokeAll(Arrays.asList("u1", "u2"), TENANT_ID, ADMIN_UUID)).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = userSessionController.revoke(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result.get("revoked"), response.getBody().get("revoked"));
        assertEquals(result.get("failed"), response.getBody().get("failed"));
        assertEquals(Arrays.asList("ResponseInfo", "revoked", "failed"), new ArrayList<>(response.getBody().keySet()));
        verify(userSessionService, never()).revoke(any(String.class), any(String.class), any(String.class));
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
