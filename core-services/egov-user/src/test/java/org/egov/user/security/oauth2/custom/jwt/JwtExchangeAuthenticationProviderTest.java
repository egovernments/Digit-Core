package org.egov.user.security.oauth2.custom.jwt;

import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.utils.ProjectEmployeeStaffUtil;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.UserSearchCriteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtExchangeAuthenticationProviderTest {

    @Mock
    private JwtValidationService jwtValidationService;

    @Mock
    private UserService userService;

    @Mock
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Mock
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Mock
    private ProjectEmployeeStaffUtil projectEmployeeStaffUtil;

    @Mock
    private HttpServletRequest request;

    private JwtExchangeAuthenticationProvider authenticationProvider;

    @Before
    public void setup() {
        authenticationProvider = new JwtExchangeAuthenticationProvider(
                jwtValidationService, userService, multiStateInstanceUtil, encryptionDecryptionUtil,
                projectEmployeeStaffUtil);
    }

    @Test
    public void testAuthenticate_ExistingUser() {
        String token = "jwt-token";
        JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "issuer");
        claims.put("sub", "subject");
        claims.put("tenantId", "pb");
        claims.put("userType", "EMPLOYEE");

        OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(), new Date(),
                "Project", "Hierarchy", "Boundary");

        User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                .roles(Collections.emptySet()).build();

        when(jwtValidationService.validate(token)).thenReturn(jwt);
        when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
        when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any())).thenReturn(user);

        Authentication result = authenticationProvider.authenticate(authenticationToken);

        assertNotNull(result);
        assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        SecureUser secureUser = (SecureUser) result.getPrincipal();
        assertEquals("uuid", secureUser.getUser().getUuid());
    }

    @Test
    public void testAuthenticate_NewUserCreation() {
        String token = "jwt-token";
        JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "issuer");
        claims.put("sub", "subject");
        claims.put("tenantId", "pb");
        claims.put("userType", "EMPLOYEE");
        claims.put("name", "John Doe");
        claims.put("preferred_username", "johndoe");
        claims.put("email", "john@example.com");

        OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(), new Date(),
                "Project", "Hierarchy", "Boundary");

        when(jwtValidationService.validate(token)).thenReturn(jwt);
        when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

        org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                .userServiceUuid("new-uuid")
                .userName("johndoe")
                .name("John Doe")
                .roles(Collections.emptyList())
                .tenantId("pb")
                .build();

        when(projectEmployeeStaffUtil.createEmployeeAndProjectStaff(anyString(), anyString(), any(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(hrmsUser);

        User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE).active(true)
                .password("password").roles(Collections.emptySet()).build();
        when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(createdUser);

        Authentication result = authenticationProvider.authenticate(authenticationToken);

        assertNotNull(result);
        verify(projectEmployeeStaffUtil).createEmployeeAndProjectStaff(eq("Project"), eq("Boundary"), any(),
                eq("Hierarchy"), eq("EMPLOYEE"), anyString(), anyString(), eq("pb"), any());
        verify(userService).updateWithoutOtpValidation(any(), any());
    }

    @Test
    public void testSupports() {
        assertTrue(authenticationProvider.supports(JwtExchangeAuthenticationToken.class));
    }
}
