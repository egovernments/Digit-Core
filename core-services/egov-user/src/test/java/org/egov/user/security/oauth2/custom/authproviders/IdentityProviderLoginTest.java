package org.egov.user.security.oauth2.custom.authproviders;

import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentityProviderLoginTest {

    @Mock private UserService userService;
    @Mock private MultiStateInstanceUtil multiStateInstanceUtil;
    @Mock private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Test
    public void identityProviderEmployeeCannotUseAnyNativeLoginPath() {
        User user = User.builder().uuid("founder").username("idp-user").tenantId("pg")
                .type(UserType.EMPLOYEE).active(true).roles(new java.util.HashSet<org.egov.user.domain.model.Role>())
                .password(UserServiceConstants.DISABLED_LOCAL_CREDENTIAL).build();
        when(userService.getUniqueUser("idp-user", "pg", UserType.EMPLOYEE)).thenReturn(user);
        when(encryptionDecryptionUtil.decryptObject(eq(user), eq("UserSelf"), eq(User.class), any()))
                .thenReturn(user);
        CustomAuthenticationProvider provider = new CustomAuthenticationProvider(userService);
        ReflectionTestUtils.setField(provider, "centraInstanceUtil", multiStateInstanceUtil);
        ReflectionTestUtils.setField(provider, "encryptionDecryptionUtil", encryptionDecryptionUtil);
        LinkedHashMap<String, String> details = new LinkedHashMap<String, String>();
        details.put("tenantId", "pg");
        details.put("userType", "EMPLOYEE");
        details.put("isInternal", "true");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("idp-user", UserServiceConstants.DISABLED_LOCAL_CREDENTIAL);
        authentication.setDetails(details);

        try {
            provider.authenticate(authentication);
            fail("Expected OAuth2Exception");
        } catch (OAuth2Exception exception) {
            assertEquals("Invalid login credentials", exception.getMessage());
        }
        verify(userService, never()).handleFailedLogin(any(User.class), anyString(), any());
    }
}
