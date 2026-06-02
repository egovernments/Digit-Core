package org.egov.user.domain.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.encryption.EncryptionService;
import org.egov.encryption.audit.AuditService;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionDecryptionUtilTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private MultiStateInstanceUtil centralInstanceUtil;

    @Mock
    private AuditService auditService;

    private EncryptionDecryptionUtil util;

    private static final String CONFIGURED_STATE_TENANT = "default";

    @Before
    public void setUp() {
        util = new EncryptionDecryptionUtil(encryptionService);
        ReflectionTestUtils.setField(util, "centralInstanceUtil", centralInstanceUtil);
        ReflectionTestUtils.setField(util, "stateLevelTenantId", CONFIGURED_STATE_TENANT);
        ReflectionTestUtils.setField(util, "abacEnabled", false);
        ReflectionTestUtils.setField(util, "auditService", auditService);
        ReflectionTestUtils.setField(util, "objectMapper", new ObjectMapper());
    }

    // ── encryptObject (tenant-aware overload) ────────────────────────────────

    @Test
    public void encryptObject_with_tenantId_resolves_state_root_and_encrypts() throws Exception {
        User user = User.builder().tenantId("ke.narok").type(UserType.CITIZEN).build();
        when(centralInstanceUtil.getStateLevelTenant("ke.narok")).thenReturn("ke");
        when(encryptionService.encryptJson(eq(user), eq("User"), eq("ke"), eq(User.class))).thenReturn(user);

        User result = util.encryptObject(user, "User", User.class, "ke.narok");

        verify(centralInstanceUtil).getStateLevelTenant("ke.narok");
        verify(encryptionService).encryptJson(eq(user), eq("User"), eq("ke"), eq(User.class));
        assertEquals(user, result);
    }

    @Test
    public void encryptObject_with_null_tenantId_falls_back_to_configured_state_tenant() throws Exception {
        User user = User.builder().build();
        when(encryptionService.encryptJson(eq(user), eq("User"), eq(CONFIGURED_STATE_TENANT), eq(User.class))).thenReturn(user);

        User result = util.encryptObject(user, "User", User.class, null);

        verify(centralInstanceUtil, never()).getStateLevelTenant(anyString());
        verify(encryptionService).encryptJson(eq(user), eq("User"), eq(CONFIGURED_STATE_TENANT), eq(User.class));
        assertEquals(user, result);
    }

    @Test
    public void encryptObject_with_tenantId_returns_null_for_null_input() {
        assertNull(util.encryptObject(null, "User", User.class, "ke.narok"));
    }

    @Test
    public void encryptObject_with_tenantId_uses_same_state_root_as_without_tenantId_on_single_state() throws Exception {
        // On a single-state deployment getStateLevelTenant("default") returns "default",
        // so both overloads should call encryptJson with the same tenant.
        User user = User.builder().tenantId("default").build();
        when(centralInstanceUtil.getStateLevelTenant("default")).thenReturn("default");
        when(encryptionService.encryptJson(eq(user), eq("User"), eq("default"), eq(User.class))).thenReturn(user);

        User result = util.encryptObject(user, "User", User.class, "default");

        verify(encryptionService).encryptJson(eq(user), eq("User"), eq("default"), eq(User.class));
        assertEquals(user, result);
    }

    // ── decryptObject (tenant-aware overload) ────────────────────────────────

    @Test
    public void decryptObject_with_tenantId_returns_same_result_as_without_tenantId() throws Exception {
        User user = User.builder().tenantId("ke.narok").type(UserType.CITIZEN).build();
        List<User> userList = Collections.singletonList(user);
        RequestInfo requestInfo = buildRequestInfo();

        when(encryptionService.decryptJson(any(RequestInfo.class), any(), anyString(), anyString(), eq(User.class)))
                .thenReturn(userList);

        List<User> without = util.decryptObject(userList, "UserSelf", User.class, requestInfo);
        List<User> withTenant = util.decryptObject(userList, "UserSelf", User.class, requestInfo, "ke.narok");

        assertEquals(without, withTenant);
    }

    @Test
    public void decryptObject_with_null_tenantId_still_succeeds() throws Exception {
        User user = User.builder().type(UserType.CITIZEN).build();
        List<User> userList = Collections.singletonList(user);
        RequestInfo requestInfo = buildRequestInfo();

        when(encryptionService.decryptJson(any(RequestInfo.class), any(), anyString(), anyString(), eq(User.class)))
                .thenReturn(userList);

        List<User> result = util.decryptObject(userList, "UserSelf", User.class, requestInfo, null);

        assertEquals(userList, result);
    }

    @Test
    public void decryptObject_with_tenantId_returns_null_for_null_input() {
        assertNull(util.decryptObject(null, "User", User.class, buildRequestInfo(), "ke.narok"));
    }

    @Test
    public void decryptObject_with_tenantId_handles_single_object_unwrapping() throws Exception {
        User user = User.builder().tenantId("ke.narok").type(UserType.CITIZEN).build();
        List<User> userList = Collections.singletonList(user);
        RequestInfo requestInfo = buildRequestInfo();

        when(encryptionService.decryptJson(any(RequestInfo.class), any(), anyString(), anyString(), eq(User.class)))
                .thenReturn(userList);

        // Passing a single User (not a List) — should be wrapped then unwrapped by the overload
        User result = util.decryptObject(user, "UserSelf", User.class, requestInfo, "ke.narok");

        assertEquals(user, result);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private RequestInfo buildRequestInfo() {
        org.egov.common.contract.request.Role role = org.egov.common.contract.request.Role.builder()
                .code("CITIZEN").build();
        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder()
                .uuid("test-uuid").type("CITIZEN")
                .roles(Collections.singletonList(role)).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }
}
