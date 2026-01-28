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
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private HttpServletRequest request;

    public JwtExchangeAuthenticationProvider(
            JwtValidationService jwtValidationService,
            UserService userService, MultiStateInstanceUtil centraInstanceUtil, EncryptionDecryptionUtil encryptionDecryptionUtil, ProjectEmployeeStaffUtil projectEmployeeStaffUtil) {
        this.jwtValidationService = jwtValidationService;
        this.userService = userService;
        this.centraInstanceUtil = centraInstanceUtil;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.projectEmployeeStaffUtil = projectEmployeeStaffUtil;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {

        String token = (String) authentication.getCredentials();

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

        User user;
        RequestInfo requestInfo;
        try {
            user = userService.getUniqueUser(jwt.getIssuer(), jwt.getExternalUserId(), tenantId, UserType.fromValue(userType));
            requestInfo = getRequestInfo(user);
            User userForUpdate = createUserForSsoUpdate(user, jwt);
            requestInfo.getUserInfo().setId(userForUpdate.getId());
            userForUpdate = userService.updateWithoutOtpValidation(userForUpdate, requestInfo);
            user = encryptionDecryptionUtil.decryptObject(user, "UserSelf", User.class, requestInfo);
        } catch (UserNotFoundException e) {
            log.info("User not found for user id: {}, creating new user", jwt.getExternalUserId(), e);
            requestInfo = getRequestInfo(jwt.getRoles(), jwt.getUserType(), jwt.getExternalUserId());
            Random random = new Random();

            long firstDigit = 6 + random.nextInt(4); // 6,7,8,9
            long remainingDigits = (long)(random.nextDouble() * 1_000_000_000L);
            String mobileNumber = String.valueOf(firstDigit * 1_000_000_000L + remainingDigits);
            org.egov.user.domain.model.hrms.User userToCreate = convertJwtToUser(jwt, mobileNumber);
            userToCreate.setPassword("eGov@123");
            org.egov.user.domain.model.hrms.User hrmsUser = projectEmployeeStaffUtil.createEmployeeAndProjectStaff(
                    jwt.getProjectName(),
                    jwt.getBoundary(),
                    userToCreate,
                    jwt.getHierarchy(),
                    jwt.getUserType(),
                    "1f3572c4-07ce-4d58-86d3-7b6e2458e812",
                    "NMCP",
                    tenantId,
                    requestInfo
            );
            String userUuid = hrmsUser.getUserServiceUuid();
            log.info("Created HRMS user and staff mapping for user service uuid: {}", userUuid);
            User createdUser = convertHrmsUserToUser(hrmsUser);
            createdUser.setPassword("eGov@123");
            createdUser.setTenantId(tenantId);
            createdUser.setIdpIssuer(jwt.getIssuer());
            createdUser.setIdpSubject(jwt.getSubject());
            createdUser.setIdpTokenExp(jwt.getExpirationTime());
            createdUser.setLastSsoLoginAt(jwt.getIssuanceTime());
            createdUser.setActive(Boolean.TRUE);
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
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_" + user.getType()));
        final SecureUser secureUser = new SecureUser(getUser(user));
        userService.resetFailedLoginAttempts(user);
        return new UsernamePasswordAuthenticationToken(secureUser, user.getPassword(), grantedAuths);
    }

    private User convertHrmsUserToUser(org.egov.user.domain.model.hrms.@Valid @NotNull User user) {
        Set<Role> domainRoles = new HashSet<>();
        for(org.egov.user.domain.model.hrms.Role role: user.getRoles()) {
            Role domainRole = new Role();
            domainRole.setTenantId(user.getTenantId());
            domainRole.setCode(role.getCode());
            domainRoles.add(domainRole);
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
            contract_roles.add(org.egov.common.contract.request.Role.builder().code(role.getCode()).name(role.getName()).build());
        }

        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder().uuid(user.getUuid())
                .type(user.getType() != null ? user.getType().name() : null).roles(contract_roles).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }

    private RequestInfo getRequestInfo(Set<String> roles, String userType, String userUuid) {
        List<org.egov.common.contract.request.Role> contract_roles = new ArrayList<>();
        for (String role: roles) {
            contract_roles.add(org.egov.common.contract.request.Role.builder().code(role).name(role).build());
        }

        org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder().uuid(userUuid)
                .type(userType).roles(contract_roles).build();
        return RequestInfo.builder().userInfo(userInfo).build();
    }

    private org.egov.user.domain.model.hrms.User convertJwtToUser(OidcValidatedJwt jwt, String mobileNumber) {
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
        return org.egov.user.domain.model.hrms.User.builder()
                .uuid(jwt.getExternalUserId())
                .emailId(jwt.getEmail())
                .active(true)
                .accountLocked(false)
                .tenantId(jwt.getTenantId())
                .type(jwt.getUserType())
                .tenantId(jwt.getTenantId())
                .roles(roles)
                .userName(jwt.getPreferredUsername())
                .name(jwt.getName())
                .mobileNumber(mobileNumber)
                .dob(1157328000000L)
                .build();
    }



    private org.egov.user.web.contract.auth.User getUser(User user) {
        org.egov.user.web.contract.auth.User authUser =  org.egov.user.web.contract.auth.User.builder().id(user.getId()).userName(user.getUsername()).uuid(user.getUuid())
                .name(user.getName()).mobileNumber(user.getMobileNumber()).emailId(user.getEmailId())
                .locale(user.getLocale()).active(user.getActive()).type(user.getType().name())
                .roles(toAuthRole(user.getRoles())).tenantId(user.getTenantId())
                .build();

        if(user.getPermanentAddress()!=null)
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

        copy.setPassword(null);
        copy.setMobileNumber(null);
        copy.setUsername(null);
        copy.setName(null);

        return copy;
    }

}

