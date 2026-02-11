package org.egov.user.security.oauth2.custom.jwt;

import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.security.oauth2.custom.MsGraphService;
import org.egov.user.utils.ProjectEmployeeStaffUtil;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.user.domain.model.UserSearchCriteria;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        private AuthProperties authProperties;

        @Mock
        private OidcProviderSupplier oidcProviderSupplier;

        @Mock
        private AuthProperties.Provider provider;

        @Mock
        private MsGraphService msGraphService;

        private AccessTokenMfaExtractor accessTokenMfaExtractor = new AccessTokenMfaExtractor(new ObjectMapper());

        private JwtExchangeAuthenticationProvider authenticationProvider;

        @Before
        public void setup() {
                authenticationProvider = new JwtExchangeAuthenticationProvider(
                                jwtValidationService, userService, multiStateInstanceUtil, encryptionDecryptionUtil,
                                projectEmployeeStaffUtil, authProperties, oidcProviderSupplier, msGraphService, accessTokenMfaExtractor);
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
                when(provider.getId()).thenReturn("oidc-azure");
                when(provider.getIssuerUri()).thenReturn("issuer");
                when(provider.getDefaultPassword()).thenReturn("eGov@123");
                when(provider.getDefaultDob()).thenReturn(1157328000000L);
                when(provider.getDefaultDesignation()).thenReturn("1f3572c4-07ce-4d58-86d3-7b6e2458e812");
                when(provider.getDefaultDepartment()).thenReturn("NMCP");
                when(provider.getMobileNumberPrefix()).thenReturn("9");
                when(provider.getMobileNumberLength()).thenReturn(10);
                when(provider.getEmployeeType()).thenReturn("PERMANENT");
                when(provider.getDefaultEmployeeStatus()).thenReturn("EMPLOYED");
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
                claims.put("name", "John Doe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(),
                                "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .tenantId("pb")
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                SecureUser secureUser = (SecureUser) result.getPrincipal();
                assertEquals("uuid", secureUser.getUser().getUuid());

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                User updatedUser = userCaptor.getValue();
                assertEquals("John Doe", updatedUser.getName());
                assertEquals("john@example.com", updatedUser.getEmailId());
                assertEquals(1, updatedUser.getRoles().size());
                assertEquals("ROLE", updatedUser.getRoles().iterator().next().getCode());
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

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(),
                                "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId("pb")
                                .build();

                when(projectEmployeeStaffUtil.createEmployeeAndProjectStaff(anyString(), anyString(), any(),
                                anyString(),
                                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                                .thenReturn(hrmsUser);

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(createdUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(projectEmployeeStaffUtil).createEmployeeAndProjectStaff(eq("Project"), eq("Boundary"), any(),
                                eq("Hierarchy"), eq("PERMANENT"), anyString(), anyString(), eq("EMPLOYED"), eq("pb"),
                                anyString(), any());
                verify(userService).updateWithoutOtpValidation(any(), any());
        }

        @Test
        public void testAuthenticate_MfaEnable_AuthTokenJwt() {
                String token = "jwt-assertion";
                // Simple JWT for authToken with amr claim
                String authToken = "eyJhbGciOiJub25lIn0.eyJhbXIiOlsicHdkIiwibWZhIl19.";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token,
                                authToken);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "pb");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                assertTrue(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testAuthenticate_MfaDisable_AuthTokenJwt() {
                String token = "jwt-assertion";
                // Simple JWT for authToken without mfa in amr
                String authToken = "eyJhbGciOiJub25lIn0.eyJhbXIiOlsicHdkIl19.";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token,
                                authToken);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "pb");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                assertFalse(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testAuthenticate_MfaEnable_AuthTokenJson_Amr() {
                String token = "jwt-assertion";
                String authToken = "{\"amr\": [\"pwd\", \"mfa\"]}";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token,
                                authToken);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "pb");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                assertTrue(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testAuthenticate_MfaEnable_AuthTokenJson_MfaEnable() {
                String token = "jwt-assertion";
                String authToken = "{\"mfaenable\": true}";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token,
                                authToken);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "pb");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                assertTrue(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testAuthenticate_RawAuthToken_DefaultsToFalse() {
                String token = "jwt-token";
                String authToken = "not-a-jwt-or-json";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token,
                                authToken);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "pb");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), "Project", "Hierarchy", "Boundary", token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(token)).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(userService.updateWithoutOtpValidation(any(), any())).thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(any(), anyString(), eq(User.class), any()))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                // Should default to false because parsing fails
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any());
                assertFalse(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testSupports() {
                assertTrue(authenticationProvider.supports(JwtExchangeAuthenticationToken.class));
        }
}
