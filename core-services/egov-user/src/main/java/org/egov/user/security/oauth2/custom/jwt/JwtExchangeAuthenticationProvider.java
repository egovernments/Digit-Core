package org.egov.user.security.oauth2.custom.jwt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.utils.ProjectEmployeeStaffUtil;
import org.egov.user.security.oauth2.custom.MsGraphService;
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Component
public class JwtExchangeAuthenticationProvider implements AuthenticationProvider {

    private final JwtValidationService jwtValidationService;
    private final UserService userService;
    private final MultiStateInstanceUtil centraInstanceUtil;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;
    private final ProjectEmployeeStaffUtil projectEmployeeStaffUtil;
    private final AuthProperties authProperties;
    private final OidcProviderSupplier oidcProviderSupplier;
    private final MsGraphService msGraphService;
    private final AccessTokenMfaExtractor accessTokenMfaExtractor;

    public JwtExchangeAuthenticationProvider(
            JwtValidationService jwtValidationService,
            UserService userService, MultiStateInstanceUtil centraInstanceUtil,
            EncryptionDecryptionUtil encryptionDecryptionUtil, ProjectEmployeeStaffUtil projectEmployeeStaffUtil,
            AuthProperties authProperties, OidcProviderSupplier oidcProviderSupplier,
            MsGraphService msGraphService,
            AccessTokenMfaExtractor accessTokenMfaExtractor) {
        this.jwtValidationService = jwtValidationService;
        this.userService = userService;
        this.centraInstanceUtil = centraInstanceUtil;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.projectEmployeeStaffUtil = projectEmployeeStaffUtil;
        this.authProperties = authProperties;
        this.oidcProviderSupplier = oidcProviderSupplier;
        this.msGraphService = msGraphService;
        this.accessTokenMfaExtractor = accessTokenMfaExtractor;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {

        String token = (String) authentication.getCredentials();
        String authToken = null;

        // Extract auth token from JwtExchangeAuthenticationToken if available
        if (authentication instanceof JwtExchangeAuthenticationToken) {
            authToken = ((JwtExchangeAuthenticationToken) authentication).getAuthToken();
        }

        OidcValidatedJwt jwt = jwtValidationService.validate(token);

        String tenantId = jwt.getTenantId();
        String userType = jwt.getUserType();
        if (centraInstanceUtil.getIsEnvironmentCentralInstance()) {
            MDC.put(UserServiceConstants.TENANTID_MDC_STRING, tenantId);
        }

        if (isEmpty(tenantId)) {
            throw new OAuth2Exception("TenantId is mandatory");
        }
        if (isEmpty(userType) || isNull(UserType.fromValue(userType))) {
            throw new OAuth2Exception("User Type is mandatory and has to be a valid type");
        }

        AuthProperties.Provider provider = getProviderById(jwt.getProviderId());
        AccessTokenMfaDetails mfaDetails = accessTokenMfaExtractor.extract(authToken);

        User user;
        RequestInfo requestInfo;
        try {
            user = userService.getUniqueUser(jwt.getIssuer(), jwt.getExternalUserId(), tenantId,
                    UserType.fromValue(userType));
            requestInfo = getRequestInfo(user);
            User userForUpdate = createUserForSsoUpdate(user, jwt);
            applyMfaDetailsToUser(userForUpdate, mfaDetails);
            msGraphService.enrichUserWithMfaDetails(userForUpdate, provider, jwt.getOid());
            if (jwt.getOid() != null) {
                String oidDigits = jwt.getOid().replaceAll("\\D", "");
                if (!oidDigits.isEmpty()) {
                    long creatorId = Long.parseLong(oidDigits.substring(0, Math.min(oidDigits.length(), 10)));
                    userForUpdate.setCreatedBy(creatorId);
                    userForUpdate.setLastModifiedBy(creatorId);
                    userForUpdate.setLoggedInUserId(creatorId);
                }
            }
            requestInfo.getUserInfo().setId(userForUpdate.getId());
            user = userService.updateWithoutOtpValidation(userForUpdate, requestInfo);
        } catch (UserNotFoundException e) {
            log.info("User not found for user id: {}, creating new user", jwt.getOid(), e);
            requestInfo = getRequestInfo(jwt.getRoles(), jwt.getUserType(), jwt.getOid());

            String mobileNumber = generateMobileNumber(provider);
            org.egov.user.domain.model.hrms.User userToCreate = convertJwtToUser(jwt, mobileNumber, provider);
            userToCreate.setPassword(provider.getDefaultPassword());
            org.egov.user.domain.model.hrms.User hrmsUser = projectEmployeeStaffUtil.createEmployeeAndProjectStaff(
                    jwt.getProjectName(),
                    jwt.getBoundary(),
                    userToCreate,
                    jwt.getHierarchy(),
                    provider.getEmployeeType(),
                    provider.getDefaultDesignation(),
                    provider.getDefaultDepartment(),
                    provider.getDefaultEmployeeStatus(),
                    tenantId,
                    jwt.getOid(),
                    requestInfo);
            String userUuid = hrmsUser.getUserServiceUuid();
            log.info("Created HRMS user and staff mapping for user service uuid: {}", userUuid);
            User createdUser = convertHrmsUserToUser(hrmsUser);
            createdUser.setPassword(provider.getDefaultPassword());
            createdUser.setTenantId(tenantId);
            createdUser.setIdpIssuer(jwt.getIssuer());
            createdUser.setIdpSubject(jwt.getSubject());
            createdUser.setAuthProvider(jwt.getProviderId());
            createdUser.setJwtToken(jwt.getRawToken());
            createdUser.setIdpTokenExp(jwt.getExpirationTime());
            createdUser.setLastSsoLoginAt(jwt.getIssuanceTime());
            createdUser.setActive(Boolean.TRUE);
            createdUser.setActive(Boolean.TRUE);
            createdUser.setEmailId(jwt.getPreferredUsername());
            if (jwt.getOid() != null) {
                String oidDigits = jwt.getOid().replaceAll("\\D", "");
                if (!oidDigits.isEmpty()) {
                    long creatorId = Long.parseLong(oidDigits.substring(0, Math.min(oidDigits.length(), 10)));
                    createdUser.setCreatedBy(creatorId);
                    createdUser.setLastModifiedBy(creatorId);
                }
            }
            applyMfaDetailsToUser(createdUser, mfaDetails);
            msGraphService.enrichUserWithMfaDetails(createdUser, provider, jwt.getOid());

            requestInfo.getUserInfo().setId(createdUser.getId());
            requestInfo.getUserInfo().setUuid(createdUser.getUuid());
            user = userService.updateWithoutOtpValidation(createdUser, requestInfo);
        } catch (DuplicateUserNameException e) {
            log.error("Fatal error, user conflict, more than one user found", e);
            throw new OAuth2Exception("Invalid login credentials: duplicate users found");

        }

        if (user.getActive() == null || !user.getActive()) {
            throw new OAuth2Exception("Please activate your account");
        }

        // If account is locked, perform lazy unlock if eligible
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            if (userService.isAccountUnlockAble(user)) {
                user = unlockAccount(user, requestInfo);
            } else
                throw new OAuth2Exception("Account locked");
        }

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority(provider.getRolePrefix() + user.getType()));
        final SecureUser secureUser = new SecureUser(getUser(user));
        userService.resetFailedLoginAttempts(user);
        return new UsernamePasswordAuthenticationToken(secureUser, user.getPassword(), grantedAuths);
    }

    private AuthProperties.Provider getProviderById(String providerId) {
        return oidcProviderSupplier.getProviders().stream()
                .filter(p -> p.getId() != null && p.getId().trim().equals(providerId))
                .findFirst()
                .orElseThrow(() -> new OAuth2Exception("No OIDC provider configured for providerId=" + providerId));
    }

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
                .roles(domainRoles)
                .loggedInUserId(user.getId())
                .build();
    }

    @Override
    public boolean supports(Class<?> auth) {
        return JwtExchangeAuthenticationToken.class.isAssignableFrom(auth);
    }

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

    private RequestInfo getRequestInfo(Set<String> roles, String userType, String userUuid) {
        List<org.egov.common.contract.request.Role> contract_roles = new ArrayList<>();
        for (String role : roles) {
            contract_roles.add(org.egov.common.contract.request.Role.builder().code(role).name(role).build());
        }

        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder().uuid(userUuid)
                .type(userType).roles(contract_roles).id(97L).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }

    private org.egov.user.domain.model.hrms.User convertJwtToUser(OidcValidatedJwt jwt, String mobileNumber,
            AuthProperties.Provider provider) {
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
        String idpRole = getFirstIdpRole(jwt, provider);
        String userName = generateEmployeeUsername(jwt.getTenantId(), jwt.getRoles(), idpRole, provider);
        if (userName == null) {
            userName = jwt.getPreferredUsername();
        }
        return org.egov.user.domain.model.hrms.User.builder()
                .uuid(jwt.getExternalUserId())
                .emailId(jwt.getEmail())
                .active(true)
                .accountLocked(false)
                .tenantId(jwt.getTenantId())
                .type(jwt.getUserType())
                .roles(roles)
                .userName(userName)
                .name(jwt.getName())
                .mobileNumber(mobileNumber)
                .dob(provider.getDefaultDob())
                .createdBy(jwt.getOid())
                .build();
    }

    /** First raw role from the JWT (e.g. Azure "roles" claim like "Digit.Admin"), before Digit mapping. */
    private String getFirstIdpRole(OidcValidatedJwt jwt, AuthProperties.Provider provider) {
        String claimKey = provider.getRoleClaimKey();
        if (claimKey == null || jwt.getClaims() == null) return null;
        Object val = jwt.getClaims().get(claimKey);
        if (val instanceof List) {
            List<?> list = (List<?>) val;
            return list.isEmpty() ? null : (list.get(0) != null ? list.get(0).toString() : null);
        }
        return val != null ? val.toString() : null;
    }

    /**
     * Generates employee username from pattern when auth.providers[i].employee-username-format is set.
     * Placeholders: {provider} (e.g. ms, google), {tenantId}, {role} (Digit-mapped), {idpRole} (raw IdP role), {number}.
     */
    private String generateEmployeeUsername(String tenantId, Set<String> roles, String idpRole,
            AuthProperties.Provider provider) {
        String format = provider.getEmployeeUsernameFormat();
        if (format == null || format.trim().isEmpty()) {
            return null;
        }
        String role = null;
        if (!CollectionUtils.isEmpty(roles)) {
            role = roles.iterator().next();
        }
        if (role == null && provider.getDefaultRoleCodes() != null) {
            String[] parts = provider.getDefaultRoleCodes().split(",");
            if (parts.length > 0) {
                role = parts[0].trim();
            }
        }
        if (role == null) {
            role = "EMP";
        }
        if (idpRole == null) {
            idpRole = "";
        }
        String providerKey = provider.getEmployeeUsernameProviderKey();
        if (providerKey == null) {
            providerKey = provider.getId() != null ? provider.getId() : "";
        }
        int numLen = provider.getEmployeeUsernameNumberLength() != null && provider.getEmployeeUsernameNumberLength() > 0
                ? provider.getEmployeeUsernameNumberLength() : 6;
        long seq;
        try {
            seq = userService.getNextEmployeeUsernameNumber(tenantId != null ? tenantId : "", numLen);
        } catch (Exception e) {
            log.warn("Failed to get next employee username sequence for tenant {}, using random: {}", tenantId, e.getMessage());
            int max = (int) Math.pow(10, numLen);
            seq = new Random().nextInt(max);
        }
        String number = String.format("%0" + numLen + "d", seq);
        return format
                .replace("{provider}", providerKey)
                .replace("{tenantId}", tenantId != null ? tenantId : "")
                .replace("{role}", role)
                .replace("{idpRole}", idpRole)
                .replace("{number}", number);
    }

    private String generateMobileNumber(AuthProperties.Provider provider) {
        Random random = new Random();
        String prefix = provider.getMobileNumberPrefix();
        int length = provider.getMobileNumberLength();
        int remainingLength = length - prefix.length();

        if (remainingLength <= 0) {
            return prefix;
        }

        StringBuilder mobileNumber = new StringBuilder(prefix);
        for (int i = 0; i < remainingLength; i++) {
            mobileNumber.append(random.nextInt(10));
        }
        return mobileNumber.toString();
    }

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

        User updatedUser = userService.updateWithoutOtpValidation(userToBeUpdated, requestInfo);
        userService.resetFailedLoginAttempts(userToBeUpdated);

        return updatedUser;
    }

    private User createUserForSsoUpdate(User user, OidcValidatedJwt jwt) {
        User copy = new User();
        BeanUtils.copyProperties(user, copy);

        copy.setIdpTokenExp(jwt.getExpirationTime());
        copy.setLastSsoLoginAt(jwt.getIssuanceTime());
        copy.setAuthProvider(jwt.getProviderId());
        copy.setJwtToken(jwt.getRawToken());
        copy.setIdpSubject(jwt.getSubject());
        copy.setIdpIssuer(jwt.getIssuer());
        copy.setEmailId(jwt.getPreferredUsername());

        copy.setName(jwt.getName());
        copy.setEmailId(jwt.getEmail());
        copy.setRoles(toDomainRoles(jwt.getRoles(), user.getTenantId()));
        copy.setPassword(null);
        copy.setMobileNumber(null);
        copy.setUsername(null);

        return copy;
    }

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
     * Apply MFA details from access_token to user (only non-null fields).
     * Used before the single update so MFA is persisted in one call.
     */
    private void applyMfaDetailsToUser(User user, AccessTokenMfaDetails mfa) {
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

}
