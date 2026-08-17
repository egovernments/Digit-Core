package org.egov.user.v2;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.domain.service.utils.UserUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkUserServiceTest {

    @Mock private BulkUserRepository bulkUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EncryptionDecryptionUtil encryptionDecryptionUtil;
    @Mock private MultiStateInstanceUtil multiStateInstanceUtil;
    @Mock private UserUtils userUtils;

    private BulkUserService service;

    @Before
    public void setUp() {
        service = new BulkUserService(
                bulkUserRepository, passwordEncoder, encryptionDecryptionUtil,
                multiStateInstanceUtil, userUtils,
                Executors.newFixedThreadPool(2, r -> {
                    Thread t = new Thread(r, "test-bcrypt");
                    t.setDaemon(true);
                    return t;
                }));
        ReflectionTestUtils.setField(service, "defaultPasswordExpiryInDays", 90);
        ReflectionTestUtils.setField(service, "maxBulkSize", 100);

        when(multiStateInstanceUtil.getStateLevelTenant(anyString())).thenReturn("os");
        when(userUtils.getStateLevelTenantForCitizen(anyString(), any())).thenAnswer(inv -> inv.getArguments()[0]);
        // encryption is a passthrough for the tests — leaves usernames untouched so
        // we can assert on plaintext values.
        when(encryptionDecryptionUtil.encryptObject(anyString(), anyList(), eq("User"), eq(User.class)))
                .thenAnswer(inv -> inv.getArguments()[1]);
        when(encryptionDecryptionUtil.decryptObject(anyString(), anyList(), eq("UserSelf"), eq(User.class), any(RequestInfo.class)))
                .thenAnswer(inv -> inv.getArguments()[1]);
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "hashed-" + inv.getArguments()[0]);
        // uniqueness: default = no existing users in DB
        when(bulkUserRepository.findExistingUsernames(anyList(), anyString())).thenReturn(Collections.emptySet());
        // createBulk: stamps a fake id on each user
        doAnswer(inv -> {
            List<User> users = (List<User>) inv.getArguments()[0];
            long id = 1000L;
            for (User u : users) u.setId(id++);
            return null;
        }).when(bulkUserRepository).createBulk(anyList());
    }

    @Test
    public void empty_list_returns_empty_list_without_touching_repository() {
        BulkUserService.Result result = service.createUsersBulk(Collections.emptyList(), requestInfo());

        assertThat(result.users).isEmpty();
        assertThat(result.errors).isEmpty();
        verify(bulkUserRepository, never()).findExistingUsernames(anyList(), anyString());
        verify(bulkUserRepository, never()).createBulk(anyList());
    }

    @Test
    public void null_input_returns_empty_list() {
        BulkUserService.Result result = service.createUsersBulk(null, requestInfo());

        assertThat(result.users).isEmpty();
        assertThat(result.errors).isEmpty();
    }

    @Test
    public void oversize_batch_throws_coded_exception() {
        ReflectionTestUtils.setField(service, "maxBulkSize", 3);
        List<User> tooMany = Arrays.asList(user("a"), user("b"), user("c"), user("d"));

        assertThatThrownBy(() -> service.createUsersBulk(tooMany, requestInfo()))
                .isInstanceOf(org.egov.tracer.model.CustomException.class)
                .hasFieldOrPropertyWithValue("code", "EGOV_USER_V2_BULK_SIZE_EXCEEDED")
                .hasMessageContaining("exceeds configured maximum of 3");
    }

    @Test
    public void happy_path_creates_all_users_and_assigns_ids() {
        List<User> incoming = Arrays.asList(user("emp_1"), user("emp_2"), user("emp_3"));

        BulkUserService.Result result = service.createUsersBulk(incoming, requestInfo());

        // all three inserted
        verify(bulkUserRepository).createBulk(anyList());
        assertThat(result.users).hasSize(3);
        assertThat(result.errors).isEmpty();
        for (User u : result.users) {
            assertThat(u.getId()).isNotNull();
        }
    }

    @Test
    public void duplicate_username_against_db_is_dropped_from_batch() {
        Set<String> existing = new HashSet<>();
        existing.add("emp_2");
        when(bulkUserRepository.findExistingUsernames(anyList(), anyString())).thenReturn(existing);

        List<User> incoming = Arrays.asList(user("emp_1"), user("emp_2"), user("emp_3"));

        BulkUserService.Result result = service.createUsersBulk(incoming, requestInfo());

        // response order: survivors first (populated), then failures (id=null)
        assertThat(result.users).hasSize(3);
        long successes = result.users.stream().filter(u -> u.getId() != null).count();
        long failures = result.users.stream().filter(u -> u.getId() == null).count();
        assertThat(successes).isEqualTo(2);
        assertThat(failures).isEqualTo(1);
        User failed = result.users.stream().filter(u -> u.getId() == null).findFirst().get();
        assertThat(failed.getUsername()).isEqualTo("emp_2");
        // error info is attached with a specific code identifying the dedup reason
        assertThat(result.errors).hasSize(1);
        java.util.Map<String, Object> err = result.errors.get(0);
        assertThat(err.get("username")).isEqualTo("emp_2");
        assertThat(err.get("code")).isEqualTo("EGOV_USER_V2_BULK_USERNAME_ALREADY_EXISTS_IN_DB");
        assertThat((String) err.get("message")).contains("already exists");
    }

    @Test
    public void duplicate_within_batch_keeps_first_drops_rest() {
        List<User> incoming = Arrays.asList(
                user("emp_1"),
                user("emp_2"),
                user("emp_1"),  // in-batch duplicate
                user("emp_3"));

        BulkUserService.Result result = service.createUsersBulk(incoming, requestInfo());

        // Only 3 unique usernames → 3 users go through the INSERT.
        long successes = result.users.stream().filter(u -> u.getId() != null).count();
        assertThat(successes).isEqualTo(3);
        // 1 in-batch duplicate error
        assertThat(result.errors).hasSize(1);
        assertThat(result.errors.get(0).get("code"))
                .isEqualTo("EGOV_USER_V2_BULK_USERNAME_DUPLICATE_IN_REQUEST");
    }

    @Test
    public void server_owned_fields_are_nulled_from_incoming_users() {
        User bogus = user("emp_1");
        bogus.setId(9999L);
        bogus.setUuid("caller-supplied-uuid");
        bogus.setCreatedBy(9999L);

        service.createUsersBulk(Collections.singletonList(bogus), requestInfo(42L, "logged-in-uuid"));

        // the service should have overwritten id/uuid/createdBy before insert
        assertThat(bogus.getCreatedBy()).isEqualTo(42L);
        assertThat(bogus.getUuid()).isNotEqualTo("caller-supplied-uuid");
        assertThat(bogus.getLastModifiedBy()).isEqualTo(42L);
    }

    @Test
    public void password_is_hashed_via_bcrypt_pool() {
        User u = user("emp_1");
        u.setPassword("plain-pass");

        service.createUsersBulk(Collections.singletonList(u), requestInfo());

        verify(passwordEncoder).encode("plain-pass");
        assertThat(u.getPassword()).isEqualTo("hashed-plain-pass");
    }

    @Test
    public void missing_password_gets_random_UUID_generated() {
        User u = user("emp_1");
        u.setPassword(null);

        service.createUsersBulk(Collections.singletonList(u), requestInfo());

        // password was generated + hashed
        assertThat(u.getPassword()).startsWith("hashed-");
        assertThat(u.getPassword().length()).isGreaterThan("hashed-".length());
    }

    @Test
    public void encryption_and_uniqueness_are_each_called_once_for_whole_batch() {
        List<User> incoming = Arrays.asList(user("emp_1"), user("emp_2"), user("emp_3"));

        service.createUsersBulk(incoming, requestInfo());

        // exactly one encrypt call for the whole list
        verify(encryptionDecryptionUtil, times(1)).encryptObject(anyString(), anyList(), eq("User"), eq(User.class));
        // exactly one uniqueness SQL for the whole list
        verify(bulkUserRepository, times(1)).findExistingUsernames(anyList(), anyString());
        // exactly one bulk INSERT
        verify(bulkUserRepository, times(1)).createBulk(anyList());
    }

    // --- helpers ---

    private User user(String username) {
        return User.builder()
                .username(username)
                .name("Name " + username)
                .type(UserType.EMPLOYEE)
                .tenantId("os.osun")
                .password("plain-pass")
                .active(true)
                .roles(new HashSet<>())
                .build();
    }

    private RequestInfo requestInfo() {
        return requestInfo(1L, "caller-uuid");
    }

    private RequestInfo requestInfo(Long userId, String userUuid) {
        return RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder()
                        .id(userId).uuid(userUuid).build())
                .build();
    }
}
