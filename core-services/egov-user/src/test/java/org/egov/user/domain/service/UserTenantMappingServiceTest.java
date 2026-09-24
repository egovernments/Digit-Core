package org.egov.user.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.persistence.repository.UserRepository;
import org.egov.user.persistence.repository.UserTenantMappingRepository;
import org.egov.user.web.contract.TenantMappingUpsertResponse.Result;
import org.egov.user.web.contract.TenantMappingUpsertResponse.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
public class UserTenantMappingServiceTest {

    private static final String TENANT = "chad";
    private static final String USERNAME = "sso-cm-001";
    private static final String MAPPING_KEY = "encrypted-key";

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTenantMappingRepository userTenantMappingRepository;

    @Mock
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    private UserTenantMappingService service;
    private RequestInfo requestInfo;

    @Before
    public void setup() {
        service = new UserTenantMappingService(userService, userRepository, userTenantMappingRepository,
                encryptionDecryptionUtil);
        requestInfo = RequestInfo.builder().apiId("test").build();
        when(encryptionDecryptionUtil.tenantMappingKey(USERNAME)).thenReturn(MAPPING_KEY);
    }

    private User user(Long id, boolean active) {
        return User.builder().id(id).uuid("uuid-" + id).username(USERNAME).type(UserType.EMPLOYEE)
                .tenantId(TENANT).active(active).build();
    }

    @Test
    public void createsTheMappingWhenItIsMissing() {
        when(userService.getUniqueUser(USERNAME, TENANT, UserType.EMPLOYEE)).thenReturn(user(42L, true));
        when(userTenantMappingRepository.exists(42L, UserType.EMPLOYEE, TENANT)).thenReturn(false);

        List<Result> results = service.upsertMappings(TENANT, UserType.EMPLOYEE,
                Collections.singletonList(USERNAME), null, requestInfo);

        assertEquals(1, results.size());
        assertEquals(Status.CREATED, results.get(0).getStatus());
        assertEquals(Long.valueOf(42L), results.get(0).getUserId());
        verify(userTenantMappingRepository).upsert(42L, UserType.EMPLOYEE, TENANT, MAPPING_KEY, "uuid-42", true);
    }

    @Test
    public void reportsAnExistingMappingWithoutPretendingItWasCreated() {
        when(userService.getUniqueUser(USERNAME, TENANT, UserType.EMPLOYEE)).thenReturn(user(42L, true));
        when(userTenantMappingRepository.exists(42L, UserType.EMPLOYEE, TENANT)).thenReturn(true);

        List<Result> results = service.upsertMappings(TENANT, null,
                Collections.singletonList(USERNAME), null, requestInfo);

        assertEquals(Status.ALREADY_PRESENT, results.get(0).getStatus());
        // still refreshed, so a stale key or uuid heals
        verify(userTenantMappingRepository).upsert(42L, UserType.EMPLOYEE, TENANT, MAPPING_KEY, "uuid-42", true);
    }

    @Test
    public void reportsAnUnknownUsername() {
        when(userService.getUniqueUser("ghost", TENANT, UserType.EMPLOYEE))
                .thenThrow(new UserNotFoundException(UserSearchCriteria.builder().build()));

        List<Result> results = service.upsertMappings(TENANT, UserType.EMPLOYEE,
                Collections.singletonList("ghost"), null, requestInfo);

        assertEquals(Status.USER_NOT_FOUND, results.get(0).getStatus());
        assertNull(results.get(0).getUserId());
        verify(userTenantMappingRepository, never()).upsert(any(), any(), anyString(), anyString(), anyString(),
                org.mockito.Matchers.anyBoolean());
    }

    @Test
    public void mapsAnInactiveUserAsInactive() {
        when(userService.getUniqueUser(USERNAME, TENANT, UserType.EMPLOYEE)).thenReturn(user(7L, false));
        when(userTenantMappingRepository.exists(7L, UserType.EMPLOYEE, TENANT)).thenReturn(false);

        service.upsertMappings(TENANT, UserType.EMPLOYEE, Collections.singletonList(USERNAME), null, requestInfo);

        verify(userTenantMappingRepository).upsert(7L, UserType.EMPLOYEE, TENANT, MAPPING_KEY, "uuid-7", false);
    }

    @Test
    public void resolvesByUserIdAndDecryptsToDeriveTheKey() {
        User encrypted = user(99L, true);
        when(userRepository.findAll(any(UserSearchCriteria.class))).thenReturn(Collections.singletonList(encrypted));
        when(encryptionDecryptionUtil.decryptObject(eq(encrypted), eq("UserSelf"), eq(User.class), eq(requestInfo)))
                .thenReturn(user(99L, true));
        when(userTenantMappingRepository.exists(99L, UserType.EMPLOYEE, TENANT)).thenReturn(false);

        List<Result> results = service.upsertMappings(TENANT, UserType.EMPLOYEE, null,
                Collections.singletonList(99L), requestInfo);

        assertEquals("99", results.get(0).getIdentifier());
        assertEquals(Status.CREATED, results.get(0).getStatus());
        verify(userTenantMappingRepository).upsert(99L, UserType.EMPLOYEE, TENANT, MAPPING_KEY, "uuid-99", true);
    }

    @Test
    public void reportsAnUnknownUserId() {
        when(userRepository.findAll(any(UserSearchCriteria.class))).thenReturn(Collections.emptyList());

        List<Result> results = service.upsertMappings(TENANT, UserType.EMPLOYEE, null,
                Collections.singletonList(404L), requestInfo);

        assertEquals(Status.USER_NOT_FOUND, results.get(0).getStatus());
        verify(userTenantMappingRepository, never()).upsert(any(), any(), anyString(), anyString(), anyString(),
                org.mockito.Matchers.anyBoolean());
    }

    @Test
    public void handlesUsernamesAndIdsInOneCall() {
        when(userService.getUniqueUser(USERNAME, TENANT, UserType.EMPLOYEE)).thenReturn(user(1L, true));
        when(userRepository.findAll(any(UserSearchCriteria.class))).thenReturn(Collections.singletonList(user(2L, true)));
        when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                .thenReturn(user(2L, true));

        List<Result> results = service.upsertMappings(TENANT, UserType.EMPLOYEE,
                Collections.singletonList(USERNAME), Collections.singletonList(2L), requestInfo);

        assertEquals(2, results.size());
        assertEquals(USERNAME, results.get(0).getIdentifier());
        assertEquals("2", results.get(1).getIdentifier());
    }

}
