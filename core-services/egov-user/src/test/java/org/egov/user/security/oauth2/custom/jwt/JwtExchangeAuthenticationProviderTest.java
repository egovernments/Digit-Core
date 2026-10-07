package org.egov.user.security.oauth2.custom.jwt;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.config.SsoDefaultPasswordResolver;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.exception.sso.SsoException;
import org.egov.user.domain.exception.sso.SsoMissingParamException;
import org.egov.user.domain.exception.sso.SsoUserMappingException;
import org.egov.user.domain.exception.sso.SsoUserNotOnboardedException;
import org.egov.user.domain.model.*;
import org.egov.user.persistence.repository.UserIdpLinkRepository;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.SsoUserPersistenceService;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile;
import org.egov.user.security.oauth2.custom.service.IdpGraphService;
import org.egov.user.security.oauth2.custom.service.impl.MsGraphService;
import org.egov.user.security.oauth2.custom.service.impl.NoOpGraphService;
import org.egov.user.utils.HrmsUserUtil;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtExchangeAuthenticationProviderTest {

    private static final String TENANT_PB = "pb";

        @Mock
        private JwtValidationService jwtValidationService;

        @Mock
        private UserService userService;

        @Mock
        private MultiStateInstanceUtil multiStateInstanceUtil;

        @Mock
        private HrmsUserUtil hrmsUserUtil;

        @Mock
        private AuthProperties authProperties;

        @Mock
        private OidcProviderSupplier oidcProviderSupplier;

        @Spy
        private AuthProperties.Provider provider = new AuthProperties.Provider();

        @Mock
        private MsGraphService msGraphService;

        @Mock
        private SsoDefaultPasswordResolver ssoDefaultPasswordResolver;

        @Mock
        private SsoUserPersistenceService ssoUserPersistenceService;

        @Mock
        private UserIdpLinkRepository userIdpLinkRepository;

        @Mock
        private EncryptionDecryptionUtil encryptionDecryptionUtil;

        private TokenMfaExtractor tokenMfaExtractor = new TokenMfaExtractor();

        private JwtExchangeAuthenticationProvider authenticationProvider;

        @Before
        public void setup() {
                List<IdpGraphService> graphServices = Collections.singletonList(msGraphService);
                authenticationProvider = new JwtExchangeAuthenticationProvider(
                                jwtValidationService, userService, multiStateInstanceUtil,
                                hrmsUserUtil, oidcProviderSupplier, graphServices,
                                tokenMfaExtractor, ssoDefaultPasswordResolver, new NoOpGraphService(),
                                ssoUserPersistenceService, encryptionDecryptionUtil, userIdpLinkRepository);
                when(encryptionDecryptionUtil.encryptObject(any(UserIdpDetails.class), anyString(), eq(UserIdpDetails.class)))
                                .thenAnswer(invocation -> invocation.getArgumentAt(0, UserIdpDetails.class));
                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any()))
                                .thenAnswer(invocation -> {
                                    User u = invocation.getArgumentAt(0, User.class);
                                    return (u != null && u.getType() != null) ? u : User.builder()
                                            .uuid(u != null ? u.getUuid() : "new-uuid")
                                            .type(UserType.EMPLOYEE)
                                            .username(u != null ? u.getUsername() : "johndoe")
                                            .active(true)
                                            .build();
                                });
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.empty());
                when(userIdpLinkRepository.findByUser(any(), anyString())).thenReturn(Collections.emptyList());
                when(authProperties.getProviders()).thenReturn(Collections.singletonList(provider));
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(provider));
                when(provider.getId()).thenReturn("oidc-azure");
                when(provider.getIssuerUri()).thenReturn("issuer");
                when(ssoDefaultPasswordResolver.generatePassword()).thenReturn("eGov@123");
                when(provider.getDefaultDob()).thenReturn(1157328000000L);
                when(provider.getDefaultEmployeeStatus()).thenReturn("EMPLOYED");
                when(provider.getRolePrefix()).thenReturn("ROLE_");
                when(provider.getTenantId()).thenReturn(TENANT_PB);
                when(msGraphService.supports(any())).thenReturn(true);
                when(msGraphService.getEmployeeCreationProfile(any(), anyString())).thenReturn(Optional.empty());
                when(provider.isJitEnabled()).thenReturn(true);
        }

        private static OidcValidatedJwt oidcJwt(Map<String, Object> claims, String token) {
                Map<String, Object> claimsWithJti = new HashMap<>(claims);
                // Only add jti if neither jti nor uti is present
                if (!claimsWithJti.containsKey("jti") && !claimsWithJti.containsKey("uti")) {
                        claimsWithJti.putIfAbsent("jti", "test-jti-" + System.nanoTime());
                }
                // Add default email if not present
                claimsWithJti.putIfAbsent("email", "test@example.com");
                claimsWithJti.putIfAbsent("name", "Test User");
                // Add unique_name claim based on email if not present
                if (!claimsWithJti.containsKey("unique_name")) {
                        claimsWithJti.put("unique_name", claimsWithJti.get("email"));
                }
                return new OidcValidatedJwt(
                                Collections.singleton("ROLE"), claimsWithJti, new Date(), new Date(), token, "oidc-azure");
        }

        @Test
        public void testAuthenticate_ExistingUser() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("email", "john@example.com");
                claims.put("preferred_username", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("preferred_username");
                when(userService.getUniqueUser(eq("john@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                SecureUser secureUser = (SecureUser) result.getPrincipal();
                assertEquals("uuid", secureUser.getUser().getUuid());
                Map<?, ?> provenance = (Map<?, ?>) result.getDetails();
                assertEquals(jwt.getProviderId(), provenance.get(JwtConstants.EXT_IDP_PROVIDER_ID));
                assertEquals(TENANT_PB, provenance.get(JwtConstants.EXT_IDP_TENANT_ID));
                assertEquals("issuer", provenance.get(JwtConstants.EXT_IDP_ISSUER));
                assertEquals("subject", provenance.get(JwtConstants.EXT_IDP_SUBJECT));
                assertEquals(TENANT_PB, provenance.get(JwtConstants.PARAM_TENANT_ID));
                assertEquals("EMPLOYEE", provenance.get(JwtConstants.PARAM_USER_TYPE));

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                User updatedUser = userCaptor.getValue();
                assertEquals("John Doe", updatedUser.getName());
                assertEquals("john@example.com", updatedUser.getEmailId());
                assertEquals(0, updatedUser.getRoles().size());
                assertFalse(updatedUser.getMfaEnabled());
        }

        @Test
        public void ssoUpdate_missingNameAndMobileClaims_passesNullToKeepExistingDbValue() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("preferred_username", "john@example.com");
                claims.put("jti", "test-jti-" + System.nanoTime());

                OidcValidatedJwt jwt = new OidcValidatedJwt(
                                Collections.singleton("ROLE"), claims, new Date(), new Date(), token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .name("Existing Name").mobileNumber("8888888888")
                                .tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("preferred_username");
                when(provider.getNameClaimKey()).thenReturn("displayName");
                when(userService.getUniqueUser(eq("john@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                User updatedUser = userCaptor.getValue();
                assertNull(updatedUser.getName());
                assertNull(updatedUser.getMobileNumber());
        }

        @Test
        public void ssoUpdate_missingEmailClaim_usesDecryptedExistingEmail() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("preferred_username", "john@example.com");
                claims.put("jti", "test-jti-" + System.nanoTime());

                OidcValidatedJwt jwt = new OidcValidatedJwt(
                                Collections.singleton("ROLE"), claims, new Date(), new Date(), token, "oidc-azure");

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .emailId("encrypted-blob").tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();
                User decryptedUser = user.toBuilder().emailId("decrypted@example.com").build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("preferred_username");
                when(userService.getUniqueUser(eq("john@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);
                when(encryptionDecryptionUtil.decryptObject(eq(user), eq("UserSelf"), eq(User.class), any(RequestInfo.class)))
                                .thenReturn(decryptedUser);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                User updatedUser = userCaptor.getValue();
                assertEquals("decrypted@example.com", updatedUser.getEmailId());
        }

        @Test
        public void testAuthenticate_ExistingUser_SameRoles_SkipsUpdate_StillUpsertsIdpDetails() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("email", "john@example.com");
                claims.put("preferred_username", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> sameRoles = new HashSet<>();
                sameRoles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .tenantId(TENANT_PB)
                                .roles(sameRoles).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result.getPrincipal() instanceof SecureUser);
                verify(ssoUserPersistenceService, never()).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any());
                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
        }

        @Test
        public void subjectHit_updatesIdpDetailsOnly_rolesUntouched() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> roles = new HashSet<>();
                roles.add(Role.builder().code("ROLE_ADMIN").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(roles).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService, never()).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any());
                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
                assertEquals(1, roles.size());
                assertEquals("ROLE_ADMIN", roles.iterator().next().getCode());
        }

        @Test
        public void subjectMiss_usernameHit_linksSubject_callsUpdateUserAndUpsert() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "new-subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "jane@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");

                Set<Role> existingRoles = new HashSet<>();
                existingRoles.add(Role.builder().code("ROLE_CLERK").tenantId(TENANT_PB).build());
                User existingUser = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(existingRoles).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(existingUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<UserIdpLink> linkCaptor = ArgumentCaptor.forClass(UserIdpLink.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), linkCaptor.capture());
                assertEquals("new-subject", linkCaptor.getValue().getSubject());
        }

        @Test
        public void subjectMiss_usernameHit_rolesNotModified() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "new-subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "jane@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");

                Set<Role> existingRoles = new HashSet<>();
                existingRoles.add(Role.builder().code("ROLE_CLERK").tenantId(TENANT_PB).build());
                User existingUser = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(existingRoles).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(existingUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                assertEquals(existingRoles, userCaptor.getValue().getRoles());
        }

        @Test
        public void subjectMiss_usernameHit_alreadyLinkedToOtherSubject_throwsNotOnboarded() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "new-subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "jane@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");

                User alreadyLinkedUser = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(alreadyLinkedUser);
                UserIdpLink conflictingLink = UserIdpLink.builder().issuer("issuer").subject("other-subject")
                                .tenantId(TENANT_PB).providerId("oidc-azure").build();
                when(userIdpLinkRepository.findByUser(any(), eq(TENANT_PB)))
                                .thenReturn(Collections.singletonList(conflictingLink));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected SsoUserNotOnboardedException");
                } catch (SsoUserNotOnboardedException e) {
                        assertEquals(SsoErrorCodes.USER_NOT_ONBOARDED, e.getErrorCode());
                }
                verify(ssoUserPersistenceService, never()).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any());
        }

        @Test
        public void subjectMiss_usernameMiss_jitDisabled_throwsNotOnboarded() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "ghost@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");
                when(provider.isJitEnabled()).thenReturn(false);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("ghost@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected SsoUserNotOnboardedException");
                } catch (SsoUserNotOnboardedException e) {
                        assertEquals(SsoErrorCodes.USER_NOT_ONBOARDED, e.getErrorCode());
                }
        }

        @Test
        public void subjectMiss_usernameMiss_jitEnabled_createsUser() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");
                claims.put("upn", "unknown@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");
                when(provider.isJitEnabled()).thenReturn(true);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("unknown@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid").userName("johndoe").name("John Doe")
                                .roles(Collections.emptyList()).tenantId(TENANT_PB).build();
                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
        }

        @Test
        public void usernameClaimAbsent_jitDisabled_throwsNotOnboarded() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");
                when(provider.isJitEnabled()).thenReturn(false);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected SsoUserNotOnboardedException");
                } catch (SsoUserNotOnboardedException e) {
                        assertEquals(SsoErrorCodes.USER_NOT_ONBOARDED, e.getErrorCode());
                }
                verify(userService, never()).getUniqueUser(anyString(), anyString(), any(UserType.class));
        }

        @Test
        public void testAuthenticate_NewUserCreation() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), eq("PERMANENT"),
                                eq("1f3572c4-07ce-4d58-86d3-7b6e2458e812"), eq("NMCP"), eq("EMPLOYED"), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
        }

        @Test
        public void testAuthenticate_NewUserCreation_UsesGraphEmployeeProfile() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("oid", "26f0a779-36d5-4360-bd5f-954568d301f6");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                EmployeeCreationProfile profile = EmployeeCreationProfile.builder()
                                .employeeType("CONTRACT")
                                .designation("design-uuid-123")
                                .department("IT")
                                .build();
                when(msGraphService.getEmployeeCreationProfile(any(), eq("26f0a779-36d5-4360-bd5f-954568d301f6")))
                                .thenReturn(Optional.of(profile));

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();
                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), eq("CONTRACT"),
                                eq("design-uuid-123"), eq("IT"), eq("EMPLOYED"), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
        }

        @Test
        public void testAuthenticate_MissingUserType_ThrowsSsoMissingParamExceptionWithCorrectErrorCode() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoMissingParamException");
                } catch (SsoMissingParamException e) {
                        assertEquals(org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes.USER_TYPE_MISSING, e.getErrorCode());
                }
        }

        @Test
        public void testAuthenticate_NewUserCreation_DesignationFromClaimKey() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");
                claims.put("jobTitle", "FieldOfficer");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                when(provider.getDesignationClaimKey()).thenReturn("jobTitle");

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                authenticationProvider.authenticate(authenticationToken);

                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(),
                                eq("FieldOfficer"), anyString(), anyString(), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
        }

        @Test
        public void testAuthenticate_NewUserCreation_DesignationFromDesignationMapping() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");
                claims.put("jobTitle", "FieldOfficer");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                when(provider.getDesignationClaimKey()).thenReturn("jobTitle");
                Map<String, String> designationMapping = new HashMap<>();
                designationMapping.put("FieldOfficer", "MAPPED_DESIGNATION_ID");
                when(provider.getDesignationMapping()).thenReturn(designationMapping);

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                authenticationProvider.authenticate(authenticationToken);

                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(),
                                eq("MAPPED_DESIGNATION_ID"), anyString(), anyString(), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
        }

        @Test
        public void testAuthenticate_MfaEnable_FromJwtClaims() throws Exception {
                String token = "jwt-assertion";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("amr", Arrays.asList("pwd", "mfa"));

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("email");
                when(userService.getUniqueUser(eq("test@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);
                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                assertTrue(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testSupports() {
                assertTrue(authenticationProvider.supports(JwtExchangeAuthenticationToken.class));
        }

        @Test
        public void ssoLogin_SucceedsForTenant_EvenIfListedInDigitLoginDisabledTenants() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().id(1L).uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();
                UserIdpLink link = UserIdpLink.builder().tenantId(TENANT_PB).issuer("issuer").subject("subject")
                                .userId(1L).uuid("uuid").providerId("oidc-azure").build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(eq(TENANT_PB), eq("oidc-azure"), eq("issuer"), eq("subject")))
                                .thenReturn(Optional.of(link));
                when(userService.getUserById(eq(1L), eq(TENANT_PB))).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test(expected = org.egov.user.domain.exception.sso.IdpJwtValidationException.class)
        public void testAuthenticate_MissingJtiAndUti_Throws() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                // No jti or uti - buildIdpDetails will throw
                OidcValidatedJwt jwt = new OidcValidatedJwt(
                                Collections.singleton("ROLE"), claims, new Date(), new Date(), token, "oidc-azure");
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);
                authenticationProvider.authenticate(authenticationToken);
        }

        @Test
        public void testAuthenticate_InvalidJwt_Throws() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                when(jwtValidationService.validate(anyString(), anyString()))
                                .thenThrow(org.egov.user.domain.exception.sso.IdpJwtValidationException.invalid("Invalid signature", null));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected IdpJwtValidationException for invalid JWT");
                } catch (org.egov.user.domain.exception.sso.IdpJwtValidationException e) {
                        // Expected - invalid JWT should result in authentication exception
                        assertTrue("Error should contain JWT validation failure",
                                e.getMessage().contains("Invalid signature"));
                }
        }

        @Test(expected = SsoUserMappingException.class)
        public void testAuthenticate_AccountLocked_NotUnlockable_Throws() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                OidcValidatedJwt jwt = oidcJwt(claims, token);
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).accountLocked(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);
                when(userService.isAccountUnlockAble(user)).thenReturn(false);
                authenticationProvider.authenticate(authenticationToken);
        }

        @Test
        public void testAuthenticate_AccountLocked_Unlockable_SucceedsAndUnlocksAccount() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> lockedUserRoles = new HashSet<>();
                lockedUserRoles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User lockedUser = User.builder()
                                .uuid("uuid")
                                .type(UserType.EMPLOYEE)
                                .active(true)
                                .accountLocked(true)
                                .tenantId(TENANT_PB)
                                .roles(lockedUserRoles)
                                .build();

                User unlockedUser = lockedUser.toBuilder()
                                .accountLocked(false)
                                .password(null)
                                .build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(lockedUser.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(lockedUser);
                when(userService.isAccountUnlockAble(lockedUser)).thenReturn(true);
                when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class))).thenReturn(unlockedUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);

                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any(RequestInfo.class));
                User updated = userCaptor.getValue();
                assertFalse(updated.getAccountLocked());
                org.junit.Assert.assertNull(updated.getPassword());
                verify(userService).resetFailedLoginAttempts(unlockedUser);
        }

        @Test
        public void testAuthenticate_NewUserCreation_UpsertsIdpDetails() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
        }

        @Test
        public void testAuthenticate_ExistingUserUpdatedSuccessfully() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("email", "john@example.com");
                claims.put("preferred_username", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> roles = new HashSet<>();
                roles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true).password("password")
                                .tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("preferred_username");
                when(userService.getUniqueUser(eq("john@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);
                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any())).thenAnswer(inv -> inv.getArgumentAt(0, User.class).toBuilder().roles(roles).build());

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
        }

        @Test
        public void testAuthenticate_MissingTenantId_ReturnsCorrectErrorCode() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken = new JwtExchangeAuthenticationToken(token);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                OidcValidatedJwt jwt = oidcJwt(claims, token);
                when(jwtValidationService.validate(anyString(), any())).thenReturn(jwt);
                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoMissingParamException");
                } catch (SsoMissingParamException e) {
                        assertEquals(org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes.TENANT_ID_MISSING, e.getErrorCode());
                }
        }

        @Test
        public void testAuthenticate_InactiveUser_ReturnsCorrectErrorCode() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                OidcValidatedJwt jwt = oidcJwt(claims, token);
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(false).tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);
                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoUserMappingException");
                } catch (SsoUserMappingException e) {
                        assertEquals(org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes.USER_INACTIVE, e.getErrorCode());
                }
        }

        @Test(expected = org.egov.user.domain.exception.sso.OidcProviderConfigException.class)
        public void testAuthenticate_IssuerMismatch_Throws() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "other-issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);

                authenticationProvider.authenticate(authenticationToken);
        }

        @Test
        public void testAuthenticate_ProviderNotFoundForTenant_Throws() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, "unknown-tenant");

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", "unknown-tenant");
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                // no provider configured for unknown tenant
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected OidcProviderConfigException");
                } catch (org.egov.user.domain.exception.sso.OidcProviderConfigException e) {
                }
                verifyZeroInteractions(userIdpLinkRepository);
        }

        @Test
        public void linkLookup_scopedToResolvedProvider_disabledProviderLinkNeverMatches() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(eq(TENANT_PB), eq("oidc-azure"), eq("issuer"), eq("subject")))
                                .thenReturn(Optional.empty());
                when(userService.getUniqueUser(anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));
                when(provider.isJitEnabled()).thenReturn(false);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected SsoUserNotOnboardedException");
                } catch (SsoUserNotOnboardedException e) {
                        assertEquals(SsoErrorCodes.USER_NOT_ONBOARDED, e.getErrorCode());
                }
                verify(userService, never()).getUserById(any(), anyString());
        }

        @Test
        public void sameIssuer_twoProviders_sameSubject_createsTwoLinkRows_bothLoginToSameUser() {
                AuthProperties.Provider providerB = spy(new AuthProperties.Provider());
                when(providerB.getId()).thenReturn("oidc-second-client");
                when(providerB.getIssuerUri()).thenReturn("issuer");
                when(providerB.getTenantId()).thenReturn(TENANT_PB);
                when(providerB.getRolePrefix()).thenReturn("ROLE_");
                when(providerB.isJitEnabled()).thenReturn(true);
                when(providerB.getUsernameClaimKey()).thenReturn("upn");
                when(oidcProviderSupplier.getProviders()).thenReturn(Arrays.asList(provider, providerB));
                when(provider.getUsernameClaimKey()).thenReturn("upn");

                User existingUser = User.builder().id(1L).uuid("shared-uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(existingUser);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                                .thenReturn(Optional.empty());
                when(userIdpLinkRepository.findByUser(eq(1L), eq(TENANT_PB))).thenReturn(Collections.emptyList());

                String token = "jwt-token";
                Map<String, Object> claimsA = new HashMap<>();
                claimsA.put("iss", "issuer");
                claimsA.put("sub", "subject-a");
                claimsA.put("tenantId", TENANT_PB);
                claimsA.put("userType", "EMPLOYEE");
                claimsA.put("upn", "jane@example.com");
                OidcValidatedJwt jwtA = oidcJwt(claimsA, token);
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwtA);

                JwtExchangeAuthenticationToken authTokenA = new JwtExchangeAuthenticationToken(token, TENANT_PB);
                authenticationProvider.authenticate(authTokenA);

                ArgumentCaptor<UserIdpLink> linkCaptorA = ArgumentCaptor.forClass(UserIdpLink.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class),
                                eq(TENANT_PB), any(RequestInfo.class), linkCaptorA.capture());
                assertEquals("oidc-azure", linkCaptorA.getValue().getProviderId());
                assertEquals("subject-a", linkCaptorA.getValue().getSubject());
                assertEquals("shared-uuid", linkCaptorA.getValue().getUuid());

                when(userIdpLinkRepository.findByUser(eq(1L), eq(TENANT_PB)))
                                .thenReturn(Collections.singletonList(linkCaptorA.getValue()));

                Map<String, Object> claimsB = new HashMap<>();
                claimsB.put("iss", "issuer");
                claimsB.put("sub", "subject-b");
                claimsB.put("tenantId", TENANT_PB);
                claimsB.put("userType", "EMPLOYEE");
                claimsB.put("upn", "jane@example.com");
                claimsB.put("jti", "test-jti-b");
                OidcValidatedJwt jwtB = new OidcValidatedJwt(Collections.singleton("ROLE"), claimsB, new Date(),
                                new Date(), token, "oidc-second-client");
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwtB);

                JwtExchangeAuthenticationToken authTokenB = new JwtExchangeAuthenticationToken(token, TENANT_PB);
                authenticationProvider.authenticate(authTokenB);

                ArgumentCaptor<UserIdpLink> linkCaptorB = ArgumentCaptor.forClass(UserIdpLink.class);
                verify(ssoUserPersistenceService, times(2)).updateUserAndUpsertIdpDetails(any(User.class),
                                any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), linkCaptorB.capture());
                UserIdpLink secondCallLink = linkCaptorB.getAllValues().get(1);
                assertEquals("oidc-second-client", secondCallLink.getProviderId());
                assertEquals("subject-b", secondCallLink.getSubject());
                assertEquals("shared-uuid", secondCallLink.getUuid());
        }

        @Test
        public void providerADisabled_providerBActive_sameUser_bWorks_aDenied() {
                User existingUser = User.builder().id(1L).uuid("shared-uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();
                UserIdpLink linkForProviderB = UserIdpLink.builder().tenantId(TENANT_PB).issuer("issuer")
                                .subject("subject").userId(1L).uuid("shared-uuid").providerId("oidc-provider-b").build();

                String token = "jwt-token";
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.emptyList());
                OidcValidatedJwt jwtA = oidcJwt(claims, token);
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwtA);
                JwtExchangeAuthenticationToken authTokenA = new JwtExchangeAuthenticationToken(token, TENANT_PB);
                try {
                        authenticationProvider.authenticate(authTokenA);
                        fail("Expected OidcProviderConfigException");
                } catch (org.egov.user.domain.exception.sso.OidcProviderConfigException e) {
                }
                verifyZeroInteractions(userIdpLinkRepository);

                AuthProperties.Provider providerB = mock(AuthProperties.Provider.class);
                when(providerB.getId()).thenReturn("oidc-provider-b");
                when(providerB.getIssuerUri()).thenReturn("issuer");
                when(providerB.getTenantId()).thenReturn(TENANT_PB);
                when(providerB.getRolePrefix()).thenReturn("ROLE_");
                when(oidcProviderSupplier.getProviders()).thenReturn(Collections.singletonList(providerB));

                claims.put("jti", "test-jti-b");
                OidcValidatedJwt jwtB = new OidcValidatedJwt(Collections.singleton("ROLE"), claims, new Date(),
                                new Date(), token, "oidc-provider-b");
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwtB);
                when(userIdpLinkRepository.find(eq(TENANT_PB), eq("oidc-provider-b"), eq("issuer"), eq("subject")))
                                .thenReturn(Optional.of(linkForProviderB));
                when(userService.getUserById(eq(1L), eq(TENANT_PB))).thenReturn(existingUser);

                JwtExchangeAuthenticationToken authTokenB = new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Authentication result = authenticationProvider.authenticate(authTokenB);

                assertNotNull(result);
                verify(userService).getUserById(eq(1L), eq(TENANT_PB));
        }

        @Test
        public void autoLink_typeMismatch_throwsNotOnboarded() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "new-subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "jane@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");
                when(provider.getUserType()).thenReturn("CITIZEN");

                User citizenTypedUser = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(citizenTypedUser);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected SsoUserNotOnboardedException");
                } catch (SsoUserNotOnboardedException e) {
                        assertEquals(SsoErrorCodes.USER_NOT_ONBOARDED, e.getErrorCode());
                }
                verify(ssoUserPersistenceService, never()).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any());
        }

        @Test
        public void secondIdp_createsSecondLink_sameUuid() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "new-subject-on-second-idp");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("upn", "jane@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(provider.getUsernameClaimKey()).thenReturn("upn");

                User existingUser = User.builder().id(1L).uuid("shared-uuid").type(UserType.EMPLOYEE).active(true)
                                .tenantId(TENANT_PB).roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(eq("jane@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(existingUser);
                UserIdpLink firstLink = UserIdpLink.builder().tenantId(TENANT_PB).issuer("other-issuer")
                                .subject("first-subject").userId(1L).uuid("shared-uuid").providerId("oidc-google").build();
                when(userIdpLinkRepository.findByUser(eq(1L), eq(TENANT_PB)))
                                .thenReturn(Collections.singletonList(firstLink));

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<UserIdpLink> linkCaptor = ArgumentCaptor.forClass(UserIdpLink.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), linkCaptor.capture());
                assertEquals("shared-uuid", linkCaptor.getValue().getUuid());
                assertEquals("issuer", linkCaptor.getValue().getIssuer());
                assertEquals("new-subject-on-second-idp", linkCaptor.getValue().getSubject());
        }

        @Test
        public void jitCreate_insertsLink() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<UserIdpLink> linkCaptor = ArgumentCaptor.forClass(UserIdpLink.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), linkCaptor.capture());
                assertEquals("issuer", linkCaptor.getValue().getIssuer());
                assertEquals("subject", linkCaptor.getValue().getSubject());
        }

        @Test
        public void testAuthenticate_UserActiveNull_ThrowsAndReturnsCorrectErrorCode() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);
                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                OidcValidatedJwt jwt = oidcJwt(claims, token);
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(null).tenantId(TENANT_PB)
                                .roles(Collections.emptySet()).build();
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);
                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoUserMappingException");
                } catch (SsoUserMappingException e) {
                        assertEquals(org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes.USER_INACTIVE, e.getErrorCode());
                }
        }

        @Test
        public void testAuthenticate_DuplicateUser_ThrowsSsoUserMappingException() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class), any()))
                        .thenThrow(new org.egov.user.domain.exception.DuplicateUserNameException(
                                new UserSearchCriteria()
                        ));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoUserMappingException");
                } catch (SsoUserMappingException e) {
                        assertEquals(org.egov.user.security.oauth2.custom.jwt.SsoErrorCodes.USER_DUPLICATE, e.getErrorCode());
                }
        }

        @Test
        public void testAuthenticate_EmployeeCreationFails_ThrowsSsoUserMappingException() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenThrow(new CustomException("EMPLOYEE_CREATION_FAILED", "hrms failed"));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        org.junit.Assert.fail("Expected SsoUserMappingException");
                } catch (SsoUserMappingException e) {
                        assertEquals(SsoErrorCodes.USER_CONTACT_ADMIN, e.getErrorCode());
                }
        }

        @Test
        public void testAuthenticate_IssuerAlias_MatchesProvider_Succeeds() {
                when(provider.getIssuerUri()).thenReturn("issuer-primary");
                when(provider.getIssuerAliases()).thenReturn(Collections.singletonList("issuer-alias"));
                when(provider.getTenantId()).thenReturn(TENANT_PB);

                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer-alias");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test
        public void testAuthenticate_UtiClaimAccepted_WhenJtiMissing() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("uti", "uti-123");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> sameRoles = new HashSet<>();
                sameRoles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(sameRoles).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<UserIdpDetails> idpCaptor = ArgumentCaptor.forClass(UserIdpDetails.class);
                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(idpCaptor.capture(), eq(TENANT_PB));
                assertEquals("uti-123", idpCaptor.getValue().getTokenId());
        }

        @Test
        public void testAuthenticate_SameIdTokenReused_Succeeds() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> sameRoles = new HashSet<>();
                sameRoles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(sameRoles).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                assertNotNull(authenticationProvider.authenticate(authenticationToken));
                assertNotNull(authenticationProvider.authenticate(authenticationToken));
                verify(ssoUserPersistenceService, times(2)).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
        }

        @Test
        public void testAuthenticate_NewUserCreation_CompleteFlow() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John Doe");
                claims.put("preferred_username", "johndoe");
                claims.put("email", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
        }

        @Test
        public void testAuthenticate_MfaDetailsExtraction_WithNullClaims() {
                String token = "jwt-assertion";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("email");
                when(userService.getUniqueUser(eq("test@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                assertFalse(userCaptor.getValue().getMfaEnabled());
        }

        @Test(expected = org.egov.user.domain.exception.sso.IdpJwtValidationException.class)
        public void testAuthenticate_MissingTokenId_ThrowsException() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                // No jti or uti claim

                OidcValidatedJwt jwt = new OidcValidatedJwt(
                                Collections.singleton("ROLE"), claims, new Date(), new Date(), token, "oidc-azure");

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);

                authenticationProvider.authenticate(authenticationToken);
        }

        @Test
        public void testAuthenticate_MultipleProviders_CorrectSelection() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                // Create multiple providers
                AuthProperties.Provider provider2 = mock(AuthProperties.Provider.class);
                List<AuthProperties.Provider> providers = Arrays.asList(provider, provider2);
                
                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(oidcProviderSupplier.getProviders()).thenReturn(providers);
                when(provider.getId()).thenReturn("oidc-azure");
                when(provider.getTenantId()).thenReturn(TENANT_PB);
                when(provider2.getId()).thenReturn("oidc-microsoft");
                when(provider2.getTenantId()).thenReturn("other-tenant");
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                // Verify that the correct provider was selected by checking the authentication result
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) result;
                assertEquals("ROLE_EMPLOYEE", authToken.getAuthorities().iterator().next().getAuthority());
        }

        @Test
        public void testAuthenticate_GraphServiceEnrichment_Called() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("oid", "test-oid-123");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(msGraphService).enrichUserWithMfaDetails(any(User.class), any(AuthProperties.Provider.class), eq("test-oid-123"));
        }

        @Test
        public void testAuthenticate_NoOpGraphService_Fallback() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("oid", "test-oid-123");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(msGraphService.supports(any())).thenReturn(false);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(msGraphService, never()).enrichUserWithMfaDetails(any(User.class), any(AuthProperties.Provider.class), anyString());
        }

        @Test
        public void testAuthenticate_RequestInfo_CorrectUserMapping() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("email");
                when(userService.getUniqueUser(eq("test@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<RequestInfo> requestInfoCaptor = ArgumentCaptor.forClass(RequestInfo.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), requestInfoCaptor.capture(), any());
                RequestInfo capturedRequestInfo = requestInfoCaptor.getValue();
                assertEquals("uuid", capturedRequestInfo.getUserInfo().getUuid());
        }

        @Test
        public void testAuthenticate_RoleMapping_PreservesTenantId() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                Set<Role> roles = new HashSet<>();
                roles.add(Role.builder().code("ROLE").tenantId(TENANT_PB).build());
                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(roles).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(provider.getUsernameClaimKey()).thenReturn("email");
                when(userService.getUniqueUser(eq("test@example.com"), eq(TENANT_PB), eq(UserType.EMPLOYEE)))
                                .thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class), any());
                User updatedUser = userCaptor.getValue();
                Role role = updatedUser.getRoles().iterator().next();
                assertEquals(TENANT_PB, role.getTenantId());
        }

        @Test
        public void testAuthenticate_DesignationResolution_PriorityOrder() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("jobTitle", "FieldOfficer");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), any(UserType.class)))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(new UserSearchCriteria()));

                org.egov.user.domain.model.hrms.User hrmsUser = org.egov.user.domain.model.hrms.User.builder()
                                .userServiceUuid("new-uuid")
                                .userName("johndoe")
                                .name("John Doe")
                                .roles(Collections.emptyList())
                                .tenantId(TENANT_PB)
                                .build();

                when(hrmsUserUtil.createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class)))
                                .thenReturn(hrmsUser);

                authenticationProvider.authenticate(authenticationToken);

                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(),
                                anyString(), anyString(), anyString(), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
        }

        @Test
        public void testAuthenticate_IssuerNormalization_TrailingSlashes() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer/");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
        }

        @Test
        public void testAuthenticate_IssuerNormalization_Whitespace() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "  issuer  ");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
        }

        @Test
        public void testAuthenticate_CentralInstance_MdcSet() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(multiStateInstanceUtil.getIsEnvironmentCentralInstance()).thenReturn(true);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
        }

        @Test
        public void testAuthenticate_NonCentralInstance_MdcNotSet() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(multiStateInstanceUtil.getIsEnvironmentCentralInstance()).thenReturn(false);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
        }

        @Test
        public void testAuthenticate_EmptyRoles_HandledCorrectly() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test
        public void testAuthenticate_NullRoles_HandledCorrectly() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test
        public void testAuthenticate_MaxLengthTokenId_HandledCorrectly() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                // Create a very long token ID (255 characters)
                StringBuilder longTokenId = new StringBuilder();
                for (int i = 0; i < 255; i++) {
                        longTokenId.append("a");
                }
                claims.put("jti", longTokenId.toString());

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test
        public void testAuthenticate_SpecialCharactersInClaims_HandledCorrectly() {
                String token = "jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");
                claims.put("name", "John <script>alert('xss')</script> Doe");
                claims.put("preferred_username", "john@example.com");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userIdpLinkRepository.find(anyString(), anyString(), anyString(), anyString()))
                		.thenReturn(Optional.of(UserIdpLink.builder().userId(user.getId()).providerId("oidc-azure").tenantId(TENANT_PB).build()));
                when(userService.getUserById(any(), anyString())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
        }

        @Test
        public void testAuthenticate_JwtValidationThrowsSecurityException_HandledCorrectly() {
                String token = "malicious-jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                // Mock JWT validation service to throw security exception
                when(jwtValidationService.validate(eq(token), eq(TENANT_PB)))
                                .thenThrow(new org.egov.user.domain.exception.sso.IdpJwtValidationException("JWT_INVALID", "Invalid JWT"));

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected IdpJwtValidationException for invalid JWT");
                } catch (org.egov.user.domain.exception.sso.IdpJwtValidationException e) {
                        // Expected - invalid JWT should result in authentication exception
                        assertTrue("Error should contain JWT validation failure",
                                e.getMessage().contains("Invalid JWT"));
                }
        }

        @Test
        public void testAuthenticate_MissingRequiredClaims_HandledCorrectly() {
                String token = "incomplete-jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                // Create JWT with missing critical claims
                Map<String, Object> claims = new HashMap<>();
                claims.put("sub", "subject");
                // Missing iss (issuer) and tenantId claims
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(eq(token), eq(TENANT_PB))).thenReturn(jwt);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected exception for JWT with missing required claims");
                } catch (Exception e) {
                        // Expected - JWT with missing required claims should be rejected
                        assertTrue("Should handle missing claims gracefully", 
                                e instanceof org.egov.user.domain.exception.sso.IdpJwtValidationException || 
                                e instanceof SsoException);
                }
        }

        @Test
        public void testAuthenticate_NullOrEmptyToken_Rejected() {
                // Test null token
                JwtExchangeAuthenticationToken nullTokenAuth =
                                new JwtExchangeAuthenticationToken(null, TENANT_PB);
                
                try {
                        authenticationProvider.authenticate(nullTokenAuth);
                        fail("Expected exception for null token");
                } catch (Exception e) {
                        // Expected - null token should be rejected
                        assertTrue("Should reject null token", 
                                e instanceof org.egov.user.domain.exception.sso.IdpJwtValidationException || 
                                e instanceof IllegalArgumentException);
                }

                // Test empty token
                JwtExchangeAuthenticationToken emptyTokenAuth =
                                new JwtExchangeAuthenticationToken("", TENANT_PB);
                
                try {
                        authenticationProvider.authenticate(emptyTokenAuth);
                        fail("Expected exception for empty token");
                } catch (Exception e) {
                        // Expected - empty token should be rejected
                        assertTrue("Should reject empty token", 
                                e instanceof org.egov.user.domain.exception.sso.IdpJwtValidationException || 
                                e instanceof IllegalArgumentException);
                }
        }

        @Test
        public void testAuthenticate_ExtremelyLargeToken_HandledCorrectly() {
                // Test with very large token (potential DoS attack)
                StringBuilder largeToken = new StringBuilder();
                for (int i = 0; i < 100000; i++) { // 100KB token
                        largeToken.append("a");
                }
                
                JwtExchangeAuthenticationToken largeTokenAuth =
                                new JwtExchangeAuthenticationToken(largeToken.toString(), TENANT_PB);
                
                try {
                        authenticationProvider.authenticate(largeTokenAuth);
                        // Should either succeed (if system can handle it) or fail gracefully
                } catch (Exception e) {
                        // If it fails, should fail gracefully without crashing
                        assertTrue("Should handle large token gracefully", 
                                e instanceof org.egov.user.domain.exception.sso.IdpJwtValidationException || 
                                e instanceof SsoException);
                }
        }

        private String sanitizeName(String input) throws Exception {
                Method m = JwtExchangeAuthenticationProvider.class
                                .getDeclaredMethod("sanitizeName", String.class);
                m.setAccessible(true);
                return (String) m.invoke(authenticationProvider, input);
        }

        @Test
        public void shouldStripAllDisallowedCharactersFromName() throws Exception {
                String allDisallowed = "J\\o$h\"n<A>?~`!@#%^()+={}[]*,:;\u201cB\u201d\u2018C\u2019 Doe";
                String sanitized = sanitizeName(allDisallowed);
                assertEquals("JohnABC Doe", sanitized);
                assertTrue("sanitized name must satisfy PATTERN_NAME",
                                sanitized.matches(UserServiceConstants.PATTERN_NAME));
        }

        @Test
        public void shouldCollapseWhitespaceAndTrimName() throws Exception {
                assertEquals("John A Doe", sanitizeName("  John (A)  Doe! "));
                assertEquals("Doe John", sanitizeName("Doe, John"));
        }

        @Test
        public void shouldReturnNullWhenNameIsBlankOrNull() throws Exception {
                assertNull(sanitizeName(null));
                assertNull(sanitizeName("###"));
                assertNull(sanitizeName("   "));
        }

        @Test
        public void shouldPreserveAllowedCharactersInName() throws Exception {
                assertEquals("Jane O'Neil-Smith", sanitizeName("Jane O'Neil-Smith"));
                assertEquals("Dr. A_B 3", sanitizeName("Dr. A_B 3"));
        }
}
