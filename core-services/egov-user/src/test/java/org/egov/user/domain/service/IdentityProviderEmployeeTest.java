package org.egov.user.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.exception.InvalidUpdatePasswordRequestException;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.utils.UserUtils;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.persistence.repository.FileStoreRepository;
import org.egov.user.persistence.repository.OtpRepository;
import org.egov.user.persistence.repository.UserRepository;
import org.egov.user.domain.model.NonLoggedInUserUpdatePasswordRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentityProviderEmployeeTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpRepository otpRepository;
    @Mock private FileStoreRepository fileRepository;
    @Mock private UserUtils userUtils;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EncryptionDecryptionUtil encryptionDecryptionUtil;
    @Mock private TokenStore tokenStore;
    @Mock private MobileNumberValidator mobileNumberValidator;
    private UserService userService;

    @Before
    public void setUp() {
        userService = new UserService(userRepository, otpRepository, fileRepository, userUtils,
                passwordEncoder, encryptionDecryptionUtil, tokenStore, 90, false, false,
                ".*", 15, 8);
        ReflectionTestUtils.setField(userService, "mobileNumberValidator", mobileNumberValidator);
        ReflectionTestUtils.setField(userService, "createUserValidateName", true);
    }

    @Test
    public void identityProviderEmployeeIsPersistedWithANonVerifiableCredential() {
        User user = User.builder().username("idp-user").name("Founder").active(true)
                .type(UserType.EMPLOYEE).tenantId("pg").mobileValidationMandatory(false)
                .roles(Collections.singleton(Role.builder().code("EMPLOYEE").tenantId("pg").build()))
                .build();
        when(encryptionDecryptionUtil.encryptObject(any(User.class), eq("User"), eq(User.class), eq("pg")))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        when(userUtils.getStateLevelTenantForCitizen("pg", UserType.EMPLOYEE)).thenReturn("pg");
        when(userRepository.create(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        userService.createIdentityProviderEmployee(user, new RequestInfo());

        ArgumentCaptor<User> persisted = ArgumentCaptor.forClass(User.class);
        verify(userRepository).create(persisted.capture());
        String stored = persisted.getValue().getPassword();
        assertEquals(UserServiceConstants.DISABLED_LOCAL_CREDENTIAL, stored);
        assertFalse(new BCryptPasswordEncoder().matches(stored, stored));
        assertFalse(new BCryptPasswordEncoder().matches("", stored));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test(expected = InvalidUpdatePasswordRequestException.class)
    public void forgotPasswordCannotCreateALocalCredentialForAnIdentityProviderEmployee() {
        User stored = User.builder().username("idp-user").type(UserType.EMPLOYEE).tenantId("pg")
                .password(UserServiceConstants.DISABLED_LOCAL_CREDENTIAL).build();
        when(userUtils.getStateLevelTenantForCitizen("pg", UserType.EMPLOYEE)).thenReturn("pg");
        when(encryptionDecryptionUtil.encryptObject(any(), eq("User"), any(), eq("pg")))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        when(userRepository.findAll(any())).thenReturn(Collections.singletonList(stored));
        NonLoggedInUserUpdatePasswordRequest request = NonLoggedInUserUpdatePasswordRequest.builder()
                .userName("idp-user").tenantId("pg").type(UserType.EMPLOYEE)
                .otpReference("123456").newPassword("Passw0rd@123").build();

        userService.updatePasswordForNonLoggedInUser(request, new RequestInfo());
    }
}
