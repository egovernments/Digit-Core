package org.egov.user.security.oauth2.custom.authproviders;

import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.exception.sso.SsoException;
import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes;
import org.egov.user.utils.DatabaseSchemaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomAuthenticationProviderDigitLoginTest {

    private static final String TENANT = "pb.amritsar";

    @Mock
    private UserService userService;

    @Mock
    private DatabaseSchemaUtils centralInstanceUtil;

    private CustomAuthenticationProvider provider;

    @Before
    public void setup() {
        provider = new CustomAuthenticationProvider(userService);
        ReflectionTestUtils.setField(provider, "centraInstanceUtil", centralInstanceUtil);
        ReflectionTestUtils.setField(provider, "employeeDigitLoginDisabled", true);
        provider.setEmployeeDigitLoginAllowedUsernames(" Admin , ops ");
        when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                .thenThrow(new UserNotFoundException(UserSearchCriteria.builder().build()));
    }

    @Test
    public void employeeNotInAllowList_IsRejectedBeforeLookup() {
        try {
            provider.authenticate(token("EMP-1", "EMPLOYEE"));
            fail("expected SsoException");
        } catch (SsoException e) {
            assertEquals(SsoErrorCodes.DIGIT_LOGIN_DISABLED, e.getErrorCode());
            assertEquals(HttpStatus.FORBIDDEN, e.getHttpStatus());
        }
        verifyZeroInteractions(userService);
    }

    @Test
    public void employeeInAllowList_PassesGuard() {
        assertPassesGuard(token("  ADMIN ", "EMPLOYEE"));
    }

    @Test
    public void citizen_PassesGuard() {
        assertPassesGuard(token("9999999999", "CITIZEN"));
    }

    @Test
    public void flagOff_EmployeePassesGuard() {
        ReflectionTestUtils.setField(provider, "employeeDigitLoginDisabled", false);
        assertPassesGuard(token("EMP-1", "EMPLOYEE"));
    }

    @Test
    public void emptyAllowList_RejectsAllEmployees() {
        provider.setEmployeeDigitLoginAllowedUsernames("");
        try {
            provider.authenticate(token("admin", "EMPLOYEE"));
            fail("expected SsoException");
        } catch (SsoException e) {
            assertEquals(SsoErrorCodes.DIGIT_LOGIN_DISABLED, e.getErrorCode());
        }
        verify(userService, never()).getUniqueUser(anyString(), anyString(), any(UserType.class));
    }

    private void assertPassesGuard(UsernamePasswordAuthenticationToken token) {
        try {
            provider.authenticate(token);
            fail("expected OAuth2Exception from user lookup");
        } catch (OAuth2Exception e) {
            assertEquals("Invalid login credentials", e.getMessage());
        }
        verify(userService).getUniqueUser(anyString(), anyString(), any(UserType.class));
    }

    private UsernamePasswordAuthenticationToken token(String username, String userType) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, "secret");
        LinkedHashMap<String, String> details = new LinkedHashMap<>();
        details.put("tenantId", TENANT);
        details.put("userType", userType);
        token.setDetails(details);
        return token;
    }
}
