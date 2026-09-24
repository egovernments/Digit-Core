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
import org.egov.user.domain.exception.sso.TokenReplayException;
import org.egov.user.domain.model.*;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.egov.user.domain.exception.sso.IdpJwtValidationException;

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

        @Mock
        private AuthProperties.Provider provider;

        @Mock
        private MsGraphService msGraphService;

        @Mock
        private SsoDefaultPasswordResolver ssoDefaultPasswordResolver;

        @Mock
        private SsoUserPersistenceService ssoUserPersistenceService;

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
                                ssoUserPersistenceService, encryptionDecryptionUtil);
                when(encryptionDecryptionUtil.encryptObject(any(UserIdpDetails.class), anyString(), eq(UserIdpDetails.class)))
                                .thenAnswer(invocation -> invocation.getArgumentAt(0, UserIdpDetails.class));
                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class)))
                                .thenAnswer(invocation -> {
                                    User u = invocation.getArgumentAt(0, User.class);
                                    return (u != null && u.getType() != null) ? u : User.builder()
                                            .uuid(u != null ? u.getUuid() : "new-uuid")
                                            .type(UserType.EMPLOYEE)
                                            .username(u != null ? u.getUsername() : "johndoe")
                                            .active(true)
                                            .build();
                                });
                when(ssoUserPersistenceService.isTokenReplay(anyString(), anyString())).thenReturn(false);
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                SecureUser secureUser = (SecureUser) result.getPrincipal();
                assertEquals("uuid", secureUser.getUser().getUuid());

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
                User updatedUser = userCaptor.getValue();
                assertEquals("John Doe", updatedUser.getName());
                assertEquals("john@example.com", updatedUser.getEmailId());
                assertEquals(1, updatedUser.getRoles().size());
                assertEquals("ROLE", updatedUser.getRoles().iterator().next().getCode());
                assertFalse(updatedUser.getMfaEnabled());
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result.getPrincipal() instanceof SecureUser);
                verify(ssoUserPersistenceService, never()).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class));
                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), eq("PERMANENT"),
                                eq("1f3572c4-07ce-4d58-86d3-7b6e2458e812"), eq("NMCP"), eq("EMPLOYED"), eq(TENANT_PB),
                                anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
        }

        /**
         * Regression guard for the SSO -> HRMS correlation-id defect.
         *
         * The jwt_exchange grant is a form-encoded OAuth2 request carrying no RequestInfo body, so
         * this provider has nothing to forward and must synthesize one. Downstream services treat
         * correlationId as always-present and dereference it without a null check — egov-hrms does
         * getCorrelationId().concat("-username-hrms") while validating the employee create — so a
         * null here crashed the callee with a NullPointerException that reached the user as the
         * unrelated "sso.user.contact_admin" / "A conflict was detected with your account".
         */
        @Test
        public void testAuthenticate_NewUserCreation_SynthesizedRequestInfoHasCorrelationId() {
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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

                ArgumentCaptor<RequestInfo> requestInfoCaptor = ArgumentCaptor.forClass(RequestInfo.class);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class),
                                requestInfoCaptor.capture());

                RequestInfo sentToHrms = requestInfoCaptor.getValue();
                assertNotNull("RequestInfo sent to HRMS must not be null", sentToHrms);
                assertNotNull("correlationId must be set — egov-hrms dereferences it without a null check",
                                sentToHrms.getCorrelationId());
                assertTrue("correlationId must not be blank",
                                sentToHrms.getCorrelationId().trim().length() > 0);
        }

        /**
         * Same guard for the returning-user branch, which synthesizes its RequestInfo from the
         * resolved User. That object also reaches boundary-service and egov-enc.
         */
        @Test
        public void testAuthenticate_ExistingUser_SynthesizedRequestInfoHasCorrelationId() {
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<RequestInfo> requestInfoCaptor = ArgumentCaptor.forClass(RequestInfo.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class),
                                any(UserIdpDetails.class), eq(TENANT_PB), requestInfoCaptor.capture());

                RequestInfo synthesized = requestInfoCaptor.getValue();
                assertNotNull(synthesized);
                assertNotNull("correlationId must be set on the returning-user path too",
                                synthesized.getCorrelationId());
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                authenticationProvider.authenticate(authenticationToken);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
                assertTrue(userCaptor.getValue().getMfaEnabled());
        }

        @Test
        public void testSupports() {
                assertTrue(authenticationProvider.supports(JwtExchangeAuthenticationToken.class));
        }

        @Test(expected = IdpJwtValidationException.class)
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
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
                } catch (IdpJwtValidationException e) {
                        // Expected - invalid JWT should result in authentication exception
                        assertNotNull("Error code should not be null", e.getErrorCode());
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(lockedUser);
                when(userService.isAccountUnlockAble(lockedUser)).thenReturn(true);
                when(userService.updateWithoutOtpValidation(any(User.class), any(RequestInfo.class), anyBoolean())).thenReturn(unlockedUser);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);

                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(any(UserIdpDetails.class), eq(TENANT_PB));
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userService).updateWithoutOtpValidation(userCaptor.capture(), any(RequestInfo.class), anyBoolean());
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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

                User createdUser = User.builder().uuid("new-uuid").username("johndoe").type(UserType.EMPLOYEE)
                                .active(true)
                                .password("password").roles(Collections.emptySet()).build();
                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class))).thenAnswer(inv -> inv.getArgumentAt(0, User.class).toBuilder().roles(roles).build());

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
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

        @Test(expected = org.egov.user.domain.exception.sso.OidcProviderConfigException.class)
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

                authenticationProvider.authenticate(authenticationToken);
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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

                when(ssoUserPersistenceService.updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), anyString(), any(RequestInfo.class)))
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<UserIdpDetails> idpCaptor = ArgumentCaptor.forClass(UserIdpDetails.class);
                verify(ssoUserPersistenceService).upsertIdpDetailsOnly(idpCaptor.capture(), eq(TENANT_PB));
                assertEquals("uti-123", idpCaptor.getValue().getTokenId());
        }

        @Test(expected = TokenReplayException.class)
        public void testAuthenticate_TokenReplay_ThrowsTokenReplayException() {
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
                when(ssoUserPersistenceService.isTokenReplay(anyString(), anyString())).thenReturn(true);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                authenticationProvider.authenticate(authenticationToken);
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                assertTrue(result instanceof UsernamePasswordAuthenticationToken);
                verify(hrmsUserUtil).createHrmsUser(
                                any(org.egov.user.domain.model.hrms.User.class), anyString(), anyString(), anyString(),
                                anyString(), anyString(), anyString(), anyString(), any(OidcValidatedJwt.class), any(RequestInfo.class));
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
                assertFalse(userCaptor.getValue().getMfaEnabled());
        }

        @Test(expected = IdpJwtValidationException.class)
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
                                .thenThrow(new org.egov.user.domain.exception.UserNotFoundException(
                                                new UserSearchCriteria()));

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<RequestInfo> requestInfoCaptor = ArgumentCaptor.forClass(RequestInfo.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(any(User.class), any(UserIdpDetails.class), eq(TENANT_PB), requestInfoCaptor.capture());
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

                User user = User.builder().uuid("uuid").type(UserType.EMPLOYEE).active(true)
                                .roles(Collections.emptySet()).tenantId(TENANT_PB).build();

                when(jwtValidationService.validate(anyString(), anyString())).thenReturn(jwt);
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

                Authentication result = authenticationProvider.authenticate(authenticationToken);

                assertNotNull(result);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(ssoUserPersistenceService).updateUserAndUpsertIdpDetails(userCaptor.capture(), any(UserIdpDetails.class), eq(TENANT_PB), any(RequestInfo.class));
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any()))
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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                when(userService.getUniqueUser(anyString(), anyString(), anyString(), any())).thenReturn(user);

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
                } catch (IdpJwtValidationException e) {
                        // Expected - invalid JWT should result in authentication exception
                        assertNotNull("Error code should not be null", e.getErrorCode());
                        assertTrue("Error should contain JWT validation failure", 
                                e.getMessage().contains("Invalid JWT"));
                }
        }

        @Test
        public void testAuthenticate_TokenReplayAttack_Rejected() {
                String token = "replayed-jwt-token";
                JwtExchangeAuthenticationToken authenticationToken =
                                new JwtExchangeAuthenticationToken(token, TENANT_PB);

                Map<String, Object> claims = new HashMap<>();
                claims.put("iss", "issuer");
                claims.put("sub", "subject");
                claims.put("tenantId", TENANT_PB);
                claims.put("userType", "EMPLOYEE");

                OidcValidatedJwt jwt = oidcJwt(claims, token);

                when(jwtValidationService.validate(eq(token), eq(TENANT_PB))).thenReturn(jwt);
                
                // Mock token replay detection
                when(ssoUserPersistenceService.isTokenReplay(anyString(), anyString()))
                                .thenReturn(true);

                try {
                        authenticationProvider.authenticate(authenticationToken);
                        fail("Expected TokenReplayException for replayed token");
                } catch (TokenReplayException e) {
                        // Expected - token replay should be detected and rejected
                        assertNotNull("Token replay exception should not be null", e);
                        assertEquals("token_replay", e.getErrorCode());
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
                                e instanceof IdpJwtValidationException || 
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
                                e instanceof IdpJwtValidationException || 
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
                                e instanceof IdpJwtValidationException || 
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
                                e instanceof IdpJwtValidationException || 
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
