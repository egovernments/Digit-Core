package org.egov.user.security.oauth2.custom.jwt;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.user.config.*;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.exception.sso.OidcProviderConfigException;
import org.egov.user.domain.exception.sso.SsoMissingParamException;
import org.egov.user.domain.exception.sso.SsoUserMappingException;
import org.egov.user.domain.exception.sso.TokenReplayException;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.SsoUserPersistenceService;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.security.oauth2.custom.service.EmployeeCreationProfile;
import org.egov.user.security.oauth2.custom.service.IdpGraphService;
import org.egov.user.security.oauth2.custom.service.impl.NoOpGraphService;
import org.egov.user.utils.HrmsUserUtil;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.egov.user.config.UserServiceConstants.SYSTEM_USER_ID;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Authentication provider for JWT exchange flow with SSO integration.
 * 
 * <p>This provider handles the complete authentication lifecycle for SSO users using
 * JWT tokens from identity providers. It supports both existing user updates and new
 * user creation scenarios with comprehensive security features including token replay
 * protection, MFA enrichment, and HRMS integration.</p>
 * 
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>JWT token validation and claim extraction</li>
 *   <li>User lookup by issuer, subject, and tenant</li>
 *   <li>Automatic user creation with HRMS integration</li>
 *   <li>Role and designation mapping from JWT claims</li>
 *   <li>MFA device registration and enforcement</li>
 *   <li>Token replay protection for security</li>
 *   <li>Account status validation and unlock functionality</li>
 *   <li>Multi-tenant support with proper schema routing</li>
 * </ul>
 * 
 * <p>The provider integrates with multiple external services including HRMS for employee
 * management, Graph services for MFA enrichment, and various identity providers for
 * SSO authentication.</p>
 */
@Slf4j
@Component
public class JwtExchangeAuthenticationProvider implements AuthenticationProvider {

    private static final Pattern NAME_DISALLOWED_CHARS =
            Pattern.compile(UserServiceConstants.PATTERN_NAME_DISALLOWED_CHARS);
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

    private final JwtValidationService jwtValidationService;
    private final UserService userService;
    private final MultiStateInstanceUtil centraInstanceUtil;
    private final HrmsUserUtil hrmsUserUtil;
    private final OidcProviderSupplier oidcProviderSupplier;
    private final List<IdpGraphService> graphServices;
    private final TokenMfaExtractor tokenMfaExtractor;
    private final SsoDefaultPasswordResolver ssoDefaultPasswordResolver;
    private final NoOpGraphService noOpGraphService;
    private final SsoUserPersistenceService ssoUserPersistenceService;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    public JwtExchangeAuthenticationProvider(
            JwtValidationService jwtValidationService,
            UserService userService, MultiStateInstanceUtil centraInstanceUtil,
            HrmsUserUtil hrmsUserUtil,
            OidcProviderSupplier oidcProviderSupplier,
            List<IdpGraphService> graphServices,
            TokenMfaExtractor tokenMfaExtractor,
            SsoDefaultPasswordResolver ssoDefaultPasswordResolver,
            NoOpGraphService noOpGraphService,
            SsoUserPersistenceService ssoUserPersistenceService,
            EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.jwtValidationService = jwtValidationService;
        this.userService = userService;
        this.centraInstanceUtil = centraInstanceUtil;
        this.hrmsUserUtil = hrmsUserUtil;
        this.oidcProviderSupplier = oidcProviderSupplier;
        this.graphServices = graphServices != null ? graphServices : new ArrayList<>();
        this.tokenMfaExtractor = requireNonNull(tokenMfaExtractor,
                JwtConstants.MFA_EXTRACTOR_REQUIRED_MESSAGE);
        this.ssoDefaultPasswordResolver = ssoDefaultPasswordResolver;
        this.noOpGraphService = noOpGraphService;
        this.ssoUserPersistenceService = ssoUserPersistenceService;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    /** Holds token and tenant from the incoming authentication. */
    private static final class JwtExchangeInput {
        final String token;
        final String tenantId;

        JwtExchangeInput(String token, String tenantId) {
            this.token = token;
            this.tenantId = tenantId;
        }
    }

    /** Holds resolved provider and MFA details from JWT claims. */
    private static final class ProviderAndMfa {
        final AuthProperties.Provider provider;
        final TokenMfaDetails mfaDetails;

        ProviderAndMfa(AuthProperties.Provider provider, TokenMfaDetails mfaDetails) {
            this.provider = provider;
            this.mfaDetails = mfaDetails;
        }
    }

    /** Holds user and request info after find-or-create. */
    private static final class UserAndRequestInfo {
        final User user;
        final RequestInfo requestInfo;

        UserAndRequestInfo(User user, RequestInfo requestInfo) {
            this.user = user;
            this.requestInfo = requestInfo;
        }
    }

    /**
     * Authenticates a user using JWT exchange flow with SSO integration.
     *
     * <p>This method handles the complete authentication lifecycle for SSO users:
     * <ol>
     *   <li>Validates JWT token and extracts tenant ID, user type, and provider information</li>
     *   <li>Validates required parameters (tenant ID, user type)</li>
     *   <li>Looks up existing user by issuer, external user ID, and tenant</li>
     *   <li>If user exists: updates user information with latest JWT claims and MFA details</li>
     *   <li>If user not found: creates new HRMS user with employee/project staff mapping</li>
     *   <li>Applies MFA details from JWT claims and provider-specific graph service</li>
     *   <li>Sets creator ID from OID digits if available</li>
     *   <li>Validates account status (active, not locked)</li>
     *   <li>Unlocks account if eligible based on failed login attempts</li>
     *   <li>Resets failed login attempts and returns authenticated user</li>
     * </ol>
     *
     * <p>Handles both existing user updates and new user creation scenarios with proper
     * exception handling for missing users, duplicate users, inactive accounts, and locked accounts.
     *
     * @param authentication the JwtExchangeAuthenticationToken containing JWT token and tenant ID
     * @return UsernamePasswordAuthenticationToken with authenticated SecureUser and role-based authorities
     * @throws SsoMissingParamException if tenant ID or user type is missing/invalid
     * @throws SsoUserMappingException if user is inactive, account is permanently locked, or duplicate user conflict
     * @throws OidcProviderConfigException if provider configuration is not found
     * @throws UserNotFoundException if user lookup fails (handled internally for new user creation)
     * @throws DuplicateUserNameException if user creation results in duplicate username (fatal error)
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        JwtExchangeInput input = extractJwtExchangeInput(authentication);
        
        try {
            OidcValidatedJwt jwt = jwtValidationService.validate(input.token, input.tenantId);

            validateRequiredParams(jwt, input.tenantId);
            
            // TOKEN REPLAY PROTECTION
            validateTokenReplay(jwt, input.tenantId);

            ProviderAndMfa providerAndMfa = resolveProviderAndMfa(jwt, input.tenantId);

            UserAndRequestInfo userAndRequestInfo = findOrCreateUser(jwt, providerAndMfa.provider,
                    providerAndMfa.mfaDetails, input.tenantId);
            User user = ensureAccountEligible(userAndRequestInfo.user, userAndRequestInfo.requestInfo);

            return buildSuccessAuthentication(user, providerAndMfa.provider);
        } catch (org.egov.user.domain.exception.sso.IdpJwtValidationException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", e.getMessage(), null));
        }
    }

    /**
     * Resolves the graph/MFA enrichment service for the given provider.
     * Returns the first implementation that supports the provider, or a no-op if none match.
     */
    private IdpGraphService resolveGraphService(AuthProperties.Provider provider) {
        return graphServices.stream()
                .filter(s -> s.supports(provider))
                .findFirst()
                .orElse(noOpGraphService);
    }

    /**
     * Extracts JWT and tenant ID from the authentication object.
     */
    private JwtExchangeInput extractJwtExchangeInput(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        String tenantId = null;
        if (authentication instanceof JwtExchangeAuthenticationToken) {
            JwtExchangeAuthenticationToken jwtToken = (JwtExchangeAuthenticationToken) authentication;
            tenantId = jwtToken.getTenantId();
        }
        
        // Validate token is not null or empty
        if (token == null || token.trim().isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Token cannot be null or empty", null));
        }
        
        // Validate token size to prevent DoS attacks (max 10KB)
        if (token.length() > 10240) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Token size exceeds maximum allowed limit", null));
        }
        
        return new JwtExchangeInput(token, tenantId);
    }

    /**
     * Validates tenant ID and user type from JWT; sets tenant MDC when in central instance.
     *
     * @throws SsoMissingParamException if tenantId or userType is missing/invalid
     */
    private void validateRequiredParams(OidcValidatedJwt jwt, String tenantId) {
        if (centraInstanceUtil.getIsEnvironmentCentralInstance()) {
            MDC.put(UserServiceConstants.TENANTID_MDC_STRING, tenantId);
        }
        if (isEmpty(tenantId)) {
            throw SsoMissingParamException.tenantIdMissing();
        }
        String userType = jwt.getUserType();
        if (isEmpty(userType) || isNull(UserType.fromValue(userType))) {
            throw SsoMissingParamException.userTypeMissing();
        }
    }

    /**
     * Validates that the JWT token has not been used before (replay protection).
     * 
     * @param jwt the validated JWT containing tokenId
     * @param tenantId the tenant ID
     * @throws TokenReplayException if the tokenId has been used before
     */
    private void validateTokenReplay(OidcValidatedJwt jwt, String tenantId) {
        String tokenId = jwt.getTokenId();
        if (tokenId == null || tokenId.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(JwtConstants.OAUTH2_ERROR_INVALID_TOKEN, 
                        JwtConstants.ERROR_MISSING_TOKEN_ID, null));
        }
        
        // Check if token has been used before via service layer
        if (ssoUserPersistenceService.isTokenReplay(tokenId, tenantId)) {
            throw new TokenReplayException(tokenId);
        }
    }

    /**
     * Resolves provider by JWT provider id and tenant, asserts issuer match, extracts MFA from validated claims.
     */
    private ProviderAndMfa resolveProviderAndMfa(OidcValidatedJwt jwt, String tenantId) {
        AuthProperties.Provider provider = getProviderById(jwt.getProviderId(), tenantId);
        assertIssuerMatchesProvider(jwt, provider);

        // MFA is derived from validated id_token claims. If missing, default is MFA disabled.
        TokenMfaDetails mfaDetails = tokenMfaExtractor.extractFromClaims(jwt.getClaims());

        return new ProviderAndMfa(provider, mfaDetails);
    }

    /**
     * Looks up user by issuer + subject + tenant; if not found, creates HRMS user and staff mapping,
     * then persists user and IDP details. Returns the user and request info for downstream use.
     */
    private UserAndRequestInfo findOrCreateUser(OidcValidatedJwt jwt, AuthProperties.Provider provider,
            TokenMfaDetails mfaDetails, String tenantId) {
        try {
            return findExistingUserAndUpdate(jwt, provider, mfaDetails, tenantId);
        } catch (UserNotFoundException e) {
            log.info("User not found for oid: {}, creating new user", jwt.getOid(), e);
            return createNewUser(jwt, provider, mfaDetails, tenantId);
        }
    }

    private UserAndRequestInfo findExistingUserAndUpdate(OidcValidatedJwt jwt, AuthProperties.Provider provider,
            TokenMfaDetails mfaDetails, String tenantId) {
        User user = userService.getUniqueUser(jwt.getIssuer(), jwt.getExternalUserId(), tenantId,
                UserType.fromValue(jwt.getUserType()));
        RequestInfo requestInfo = getRequestInfo(user);

        User userForUpdate = createUserForSsoUpdate(user, jwt);
        applyMfaDetailsToUser(userForUpdate, mfaDetails);
        resolveGraphService(provider).enrichUserWithMfaDetails(userForUpdate, provider, jwt.getOid());
        requestInfo.getUserInfo().setId(userForUpdate.getId());

        UserIdpDetails idpDetails = buildIdpDetails(userForUpdate, jwt);

        User originalUser = user;
        if (rolesChanged(user.getRoles(), userForUpdate.getRoles())) {
            user = ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                    userForUpdate, idpDetails, tenantId, requestInfo);
        } else {
            ssoUserPersistenceService.upsertIdpDetailsOnly(idpDetails, tenantId);
            User decrypted = encryptionDecryptionUtil.decryptObject(user, "UserSelf", User.class, requestInfo);
            user = decrypted != null ? decrypted : originalUser;
        }

        return new UserAndRequestInfo(user, requestInfo);
    }

    private UserAndRequestInfo createNewUser(OidcValidatedJwt jwt, AuthProperties.Provider provider,
            TokenMfaDetails mfaDetails, String tenantId) {
        RequestInfo requestInfo = getRequestInfo(jwt.getRoles(), jwt.getUserType(), jwt.getOid());

        String password = ssoDefaultPasswordResolver.generatePassword();
        org.egov.user.domain.model.hrms.User userToCreate = convertJwtToUser(jwt, provider);
        userToCreate.setPassword(password);

        Optional<EmployeeCreationProfile> profileOpt = resolveGraphService(provider)
                .getEmployeeCreationProfile(provider, jwt.getOid());
        String employeeType = profileOpt.map(EmployeeCreationProfile::getEmployeeType).filter(StringUtils::hasText)
                .orElse(OidcConfigConstants.DEFAULT_EMPLOYEE_TYPE);
        String designation = resolveDesignation(provider, jwt, profileOpt);
        String department = profileOpt.map(EmployeeCreationProfile::getDepartment).filter(StringUtils::hasText)
                .orElse(provider != null && StringUtils.hasText(provider.getDefaultDepartmentCode())
                ? provider.getDefaultDepartmentCode()
                : OidcConfigConstants.DEFAULT_DEPARTMENT_CODE);

        org.egov.user.domain.model.hrms.User hrmsUser;
        try {
            hrmsUser = hrmsUserUtil.createHrmsUser(
                    userToCreate, employeeType, designation, department,
                    provider.getDefaultEmployeeStatus(), tenantId, jwt.getOid(),
                    provider.getDefaultBoundaryHierarchyType(), jwt, requestInfo);
        } catch (CustomException e) {
            if ("EMPLOYEE_CREATION_FAILED".equals(e.getCode())) {
                throw SsoUserMappingException.contactAdminForHrmsConflict();
            }
            throw e;
        }

        log.info("Created HRMS user for user service uuid: {}", hrmsUser.getUserServiceUuid());

        User createdUser = convertHrmsUserToUser(hrmsUser);
        createdUser.setPassword(password);
        createdUser.setTenantId(tenantId);
        createdUser.setIdpIssuer(jwt.getIssuer());
        createdUser.setIdpSubject(jwt.getSubject());
        createdUser.setAuthProvider(jwt.getProviderId());
        createdUser.setTokenId(jwt.getTokenId());
        createdUser.setIdpTokenExp(jwt.getExpirationTime());
        createdUser.setLastSsoLoginAt(jwt.getIssuanceTime());
        createdUser.setActive(Boolean.TRUE);
        createdUser.setCreatedBy(requestInfo.getUserInfo().getId());
        createdUser.setLastModifiedBy(createdUser.getId());
        applyMfaDetailsToUser(createdUser, mfaDetails);
        resolveGraphService(provider).enrichUserWithMfaDetails(createdUser, provider, jwt.getOid());

        requestInfo.getUserInfo().setId(createdUser.getId());
        requestInfo.getUserInfo().setUuid(createdUser.getUuid());

        try {
            UserIdpDetails idpDetails = buildIdpDetails(createdUser, jwt);
            User user = ssoUserPersistenceService.updateUserAndUpsertIdpDetails(
                    createdUser, idpDetails, tenantId, requestInfo);
            return new UserAndRequestInfo(user, requestInfo);
        } catch (DuplicateUserNameException dupE) {
            log.error("Fatal: duplicate user conflict for oid: {}", jwt.getOid(), dupE);
            throw SsoUserMappingException.duplicateUser(dupE);
        }
    }

    /**
     * Ensures user is active and not permanently locked; unlocks if within cooldown.
     *
     * @return the user (possibly updated after unlock)
     * @throws SsoUserMappingException if inactive or permanently locked
     */
    private User ensureAccountEligible(User user, RequestInfo requestInfo) {
        if (user.getActive() == null || !user.getActive()) {
            throw SsoUserMappingException.userInactive();
        }
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            if (userService.isAccountUnlockAble(user)) {
                user = unlockAccount(user, requestInfo);
            } else {
                throw SsoUserMappingException.accountLocked();
            }
        }
        return user;
    }

    /**
     * Builds the successful authentication result: authorities, SecureUser, reset failed attempts.
     */
    private Authentication buildSuccessAuthentication(User user, AuthProperties.Provider provider) {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority(provider.getRolePrefix() + user.getType()));
        SecureUser secureUser = new SecureUser(getUser(user));
        userService.resetFailedLoginAttempts(user);
        return new UsernamePasswordAuthenticationToken(secureUser, user.getPassword(), grantedAuths);
    }

    /**
     * Gets the provider configuration by provider ID and tenant ID.
     *
     * <p>This method searches through all available OIDC providers to find a matching
     * configuration based on both provider ID and tenant ID. The match requires exact
     * equality for both fields with proper null checks and string trimming.
     *
     * <p>The search is performed as a stream operation that filters providers by:
     * <ul>
     *   <li>Non-null provider ID that matches the given providerId (trimmed)</li>
     *   <li>Non-null tenant ID that exactly matches the given tenantId</li>
     * </ul>
     *
     * @param providerId the provider ID to look up (must not be null)
     * @param tenantId the tenant ID to look up (must not be null)
     * @return the matching provider configuration
     * @throws OidcProviderConfigException if no provider is found with the given provider ID and tenant ID combination
     */
    private AuthProperties.Provider getProviderById(String providerId, String tenantId) {
        return oidcProviderSupplier.getProviders().stream()
                .filter(p -> p.getId() != null && p.getId().trim().equals(providerId) && p.getTenantId() !=null && p.getTenantId().equals(tenantId))
                .findFirst()
                .orElseThrow(() -> OidcProviderConfigException.providerNotFound(providerId));
    }

    /**
     * Ensures the JWT issuer matches the provider's issuer URI or one of its aliases.
     * Rejects the token if there is no match (null-safe).
     *
     * @param jwt      the validated JWT
     * @param provider the resolved provider configuration
     * @throws OidcProviderConfigException if the token issuer does not match the provider
     */
    private void assertIssuerMatchesProvider(OidcValidatedJwt jwt, AuthProperties.Provider provider) {
        String tokenIssuer = jwt.getIssuer();
        if (!StringUtils.hasText(tokenIssuer)) {
            throw OidcProviderConfigException.issuerMismatch(String.valueOf(tokenIssuer), provider.getId());
        }
        String normalizedTokenIssuer = normalizeIssuer(tokenIssuer);
        String issuerUri = provider.getIssuerUri();
        if (StringUtils.hasText(issuerUri) && normalizedTokenIssuer.equals(normalizeIssuer(issuerUri))) {
            return;
        }
        List<String> aliases = provider.getIssuerAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                if (StringUtils.hasText(alias) && normalizedTokenIssuer.equals(normalizeIssuer(alias))) {
                    return;
                }
            }
        }
        throw OidcProviderConfigException.issuerMismatch(tokenIssuer, provider.getId());
    }

    /**
     * Resolves the DIGIT designation code for a new SSO user using (in order of precedence):
     * <ol>
     *   <li>JWT claim configured via designationClaimKey on provider</li>
     *   <li>Designation from EmployeeCreationProfile (Graph jobTitle/custom designation)</li>
     *   <li>Provider's designationMapping (IDP designation → DIGIT designation code)</li>
     *   <li>Provider's defaultDesignationCode</li>
     *   <li>Global DEFAULT_DESIGNATION_ID constant</li>
     * </ol>
     */
    private String resolveDesignation(AuthProperties.Provider provider, OidcValidatedJwt jwt,
                                      Optional<EmployeeCreationProfile> profileOpt) {
        String idpDesignationFromClaim = null;
        if (provider != null && StringUtils.hasText(provider.getDesignationClaimKey())
                && jwt != null && jwt.getClaims() != null) {
            Object claimValue = jwt.getClaims().get(provider.getDesignationClaimKey());
            if (claimValue != null) {
                String value = claimValue.toString().trim();
                if (!value.isEmpty()) {
                    idpDesignationFromClaim = value;
                }
            }
        }

        String idpDesignation = StringUtils.hasText(idpDesignationFromClaim)
                ? idpDesignationFromClaim
                : profileOpt.map(EmployeeCreationProfile::getDesignation)
                .filter(StringUtils::hasText)
                .orElse(null);

        String mappedDesignation = null;
        if (provider != null && provider.getDesignationMapping() != null && idpDesignation != null) {
            mappedDesignation = provider.getDesignationMapping().get(idpDesignation);
        }

        String resolved = StringUtils.hasText(mappedDesignation)
                ? mappedDesignation
                : StringUtils.hasText(idpDesignation)
                ? idpDesignation
                : (provider != null && StringUtils.hasText(provider.getDefaultDesignationCode())
                ? provider.getDefaultDesignationCode()
                : null);

        if (!StringUtils.hasText(resolved)) {
            resolved = OidcConfigConstants.DEFAULT_DESIGNATION_ID;
        }
        return resolved;
    }

    /**
     * Converts an HRMS user object to a domain User object.
     * Maps roles from HRMS format to domain format.
     *
     * @param user the HRMS user object to convert
     * @return converted domain User object
     */
    private User convertHrmsUserToUser(org.egov.user.domain.model.hrms.@Valid @NotNull User user) {
        Set<Role> domainRoles = new HashSet<>();
        if (user.getRoles() != null) {
            for (org.egov.user.domain.model.hrms.Role role : user.getRoles()) {
                Role domainRole = new Role();
                domainRole.setTenantId(user.getTenantId());
                domainRole.setCode(role.getCode());
                domainRoles.add(domainRole);
            }
        }
        return User.builder()
                .uuid(user.getUserServiceUuid())
                .id(user.getId())
                .name(user.getName())
                .username(user.getUserName())
                .emailId(user.getEmailId())
                .mobileNumber(user.getMobileNumber())
                .mobileValidationMandatory(false)
                .type(UserType.fromValue(user.getType()))
                .roles(domainRoles)
                .loggedInUserId(user.getId())
                .createdBy(user.getId())
                .lastModifiedBy(user.getId())
                .build();
    }

    /**
     * Checks if this authentication provider supports the given authentication type.
     *
     * @param auth the authentication class to check
     * @return true if the authentication is a JwtExchangeAuthenticationToken, false otherwise
     */
    @Override
    public boolean supports(Class<?> auth) {
        return JwtExchangeAuthenticationToken.class.isAssignableFrom(auth);
    }

    /**
     * Creates a RequestInfo object from a User domain object.
     * Converts domain roles to contract roles.
     *
     * @param user the user domain object
     * @return RequestInfo with user information
     */
    private RequestInfo getRequestInfo(User user) {
        Set<Role> domain_roles = user.getRoles();
        List<org.egov.common.contract.request.Role> contract_roles = new ArrayList<>();
        for (org.egov.user.domain.model.Role role : domain_roles) {
            contract_roles.add(
                    org.egov.common.contract.request.Role.builder().code(role.getCode()).name(role.getName()).build());
        }

        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder()
                .uuid(user.getUuid())
                .type(user.getType() != null ? user.getType().name() : null).roles(contract_roles).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }

    /**
     * Creates a RequestInfo object from role codes, user type, and user UUID.
     * Used when creating a new user before the user exists in the system.
     *
     * @param roles set of role codes
     * @param userType the user type string
     * @param userUuid the user UUID
     * @return RequestInfo with user information
     */
    private RequestInfo getRequestInfo(Set<String> roles, String userType, String userUuid) {
        List<org.egov.common.contract.request.Role> contract_roles = new ArrayList<>();
        for (String role : roles) {
            contract_roles.add(org.egov.common.contract.request.Role.builder().code(role).name(role).build());
        }

        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder().uuid(userUuid)
                .type(userType).roles(contract_roles).id(SYSTEM_USER_ID).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }

    /**
     * Converts a validated JWT to an HRMS User object.
     * Extracts user information from JWT claims and generates username if configured.
     *
     * @param jwt the validated JWT containing user claims
     * @param provider the OIDC provider configuration
     * @return HRMS User object ready for creation
     */
    private org.egov.user.domain.model.hrms.User convertJwtToUser(OidcValidatedJwt jwt, AuthProperties.Provider provider) {
        List<org.egov.user.domain.model.hrms.Role> roles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(jwt.getRoles())) {
            for (String role : jwt.getRoles()) {
                org.egov.user.domain.model.hrms.Role domainRole = new org.egov.user.domain.model.hrms.Role();
                domainRole.setCode(role);
                domainRole.setName(role);
                domainRole.setTenantId(jwt.getTenantId());
                roles.add(domainRole);
            }
        }

        String username = jwt.getClaims().get("unique_name") != null ? 
            jwt.getClaims().get("unique_name").toString() : 
            jwt.getEmail(); // Use unique_name which has the email, fallback to preferred_username
        String name = sanitizeName(jwt.getName());
        return org.egov.user.domain.model.hrms.User.builder()
                .uuid(jwt.getExternalUserId())
                .emailId(username)
                .active(true)
                .accountLocked(false)
                .tenantId(jwt.getTenantId())
                .type(jwt.getUserType())
                .roles(roles)
                .userName(username)
                .name(name)
                .dob(provider.getDefaultDob())
                .createdBy(jwt.getOid())
                .build();
    }

    private String sanitizeName(String name) {
        if (name == null)
            return null;
        String sanitized = WHITESPACE_RUN
                .matcher(NAME_DISALLOWED_CHARS.matcher(name).replaceAll(""))
                .replaceAll(" ")
                .trim();
        return sanitized.isEmpty() ? null : sanitized;
    }


    /**
     * Converts a domain User object to a contract User object for API responses.
     *
     * @param user the domain User object
     * @return contract User object with user information
     */
    private org.egov.user.web.contract.auth.User getUser(User user) {
        org.egov.user.web.contract.auth.User authUser = org.egov.user.web.contract.auth.User.builder().id(user.getId())
                .userName(user.getUsername()).uuid(user.getUuid())
                .name(user.getName()).mobileNumber(user.getMobileNumber()).emailId(user.getEmailId())
                .locale(user.getLocale()).active(user.getActive()).type(user.getType().name())
                .roles(toAuthRole(user.getRoles())).tenantId(user.getTenantId())
                .build();

        if (user.getPermanentAddress() != null)
            authUser.setPermanentCity(user.getPermanentAddress().getCity());

        return authUser;
    }

    /**
     * Converts domain Role objects to contract Role objects.
     *
     * @param domainRoles set of domain Role objects
     * @return set of contract Role objects
     */
    private Set<org.egov.user.web.contract.auth.Role> toAuthRole(Set<org.egov.user.domain.model.Role> domainRoles) {
        if (domainRoles == null)
            return new HashSet<>();
        return domainRoles.stream().map(org.egov.user.web.contract.auth.Role::new).collect(Collectors.toSet());
    }

    /**
     * Unlock account and disable existing failed login attempts for the user
     *
     * @param user to be unlocked
     * @return Updated user
     */
    private User unlockAccount(User user, RequestInfo requestInfo) {
        User userToBeUpdated = user.toBuilder()
                .accountLocked(false)
                .password(null)
                .build();
        return userService.updateWithoutOtpValidation(userToBeUpdated, requestInfo);
    }

    /**
     * Creates a User object for SSO update from existing user and JWT claims.
     * Sets only identity and IdP linkage fields; session/MFA are persisted via eg_user_idp_details.
     *
     * @param user the existing user object
     * @param jwt the validated JWT with updated claims
     * @return User object ready for update (eg_user only)
     */
    private User createUserForSsoUpdate(User user, OidcValidatedJwt jwt) {
        String name = sanitizeName(jwt.getName());
        return user.toBuilder()
                .authProvider(jwt.getProviderId())
                .idpSubject(jwt.getSubject())
                .idpIssuer(jwt.getIssuer())
                .name(name)
                .emailId(jwt.getClaims().get("unique_name") != null ? 
                    jwt.getClaims().get("unique_name").toString() : 
                    jwt.getEmail()) // Use unique_name which has the email, fallback to preferred_username
                .roles(toDomainRoles(jwt.getRoles(), user.getTenantId()))
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getId())
                .password(null)
                .mobileNumber(null)
                .username(null)
                .build();
    }

    /**
     * Builds IDP details for upsert into eg_user_idp_details (session and MFA only).
     * Requires JWT to contain jti or uti claim; throws otherwise.
     */
    private UserIdpDetails buildIdpDetails(User user, OidcValidatedJwt jwt) {
        String tokenId = jwt.getTokenId();
        if (tokenId == null || tokenId.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(JwtConstants.OAUTH2_ERROR_INVALID_TOKEN, JwtConstants.ERROR_MISSING_TOKEN_ID, null));
        }
        return UserIdpDetails.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .uuid(user.getUuid())
                .idpTokenExp(jwt.getExpirationTime())
                .lastSsoLoginAt(jwt.getIssuanceTime())
                .tokenId(tokenId)
                .mfaEnabled(user.getMfaEnabled())
                .mfaDeviceName(user.getMfaDeviceName())
                .mfaPhoneLast4(user.getMfaPhoneLast4())
                .mfaRegisteredOn(user.getMfaRegisteredOn())
                .mfaDetails(user.getMfaDetails())
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getLastModifiedBy())
                .build();
    }

    /**
     * Returns true if the two role sets differ by role identity (code + tenantId).
     * Both null or both empty is treated as unchanged.
     */
    private boolean rolesChanged(Set<Role> previous, Set<Role> current) {
        if (CollectionUtils.isEmpty(previous) && CollectionUtils.isEmpty(current)) {
            return false;
        }
        if (CollectionUtils.isEmpty(previous) || CollectionUtils.isEmpty(current)) {
            return true;
        }
        Set<String> previousKeys = previous.stream()
                .map(r -> (r.getCode() != null ? r.getCode() : "") + "|" + (r.getTenantId() != null ? r.getTenantId() : ""))
                .collect(Collectors.toSet());
        Set<String> currentKeys = current.stream()
                .map(r -> (r.getCode() != null ? r.getCode() : "") + "|" + (r.getTenantId() != null ? r.getTenantId() : ""))
                .collect(Collectors.toSet());
        return !previousKeys.equals(currentKeys);
    }

    /**
     * Converts a set of role code strings to domain Role objects.
     *
     * @param roles set of role code strings
     * @param tenantId the tenant ID for the roles
     * @return set of domain Role objects
     */
    private Set<Role> toDomainRoles(Set<String> roles, String tenantId) {
        if (CollectionUtils.isEmpty(roles)) {
            return new HashSet<>();
        }
        return roles.stream()
                .map(roleCode -> Role.builder()
                        .code(roleCode)
                        .tenantId(tenantId)
                        .build())
                .collect(Collectors.toSet());
    }

    /**
     * Applies MFA details from JWT claims to user object (only non-null fields).
     * Used before the single update so MFA is persisted in one call.
     *
     * @param user the user object to update
     * @param mfa the MFA details extracted from JWT claims
     */
    private void applyMfaDetailsToUser(User user, TokenMfaDetails mfa) {
        if (user == null || mfa == null)
            return;
        if (mfa.getMfaEnabled() != null)
            user.setMfaEnabled(mfa.getMfaEnabled());
        if (mfa.getMfaPhoneLast4() != null)
            user.setMfaPhoneLast4(mfa.getMfaPhoneLast4());
        if (mfa.getMfaDeviceName() != null)
            user.setMfaDeviceName(mfa.getMfaDeviceName());
        if (mfa.getMfaDetails() != null)
            user.setMfaDetails(mfa.getMfaDetails());
        if (mfa.getMfaRegisteredOn() != null)
            user.setMfaRegisteredOn(mfa.getMfaRegisteredOn());
    }

    /**
     * Normalizes issuer URI by trimming whitespace and removing trailing slashes.
     * This ensures consistent issuer matching across the authentication flow.
     *
     * @param issuer the issuer URI to normalize
     * @return normalized issuer URI, or null if input is null
     */
    private String normalizeIssuer(String issuer) {
        if (issuer == null) {
            return null;
        }
        return issuer.trim().replaceAll("/+$", "");
    }

}
