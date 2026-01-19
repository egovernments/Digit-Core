package org.egov.user.security.oauth2.custom.jwt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.egov.user.web.contract.auth.OidcValidatedJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Component
public class JwtExchangeAuthenticationProvider implements AuthenticationProvider {

    private final JwtValidationService jwtValidationService;
    private final UserService userService;
    private final MultiStateInstanceUtil centraInstanceUtil;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Autowired
    private HttpServletRequest request;

    @Value("${citizen.login.password.otp.enabled}")
    private boolean citizenLoginPasswordOtpEnabled;

    @Value("${employee.login.password.otp.enabled}")
    private boolean employeeLoginPasswordOtpEnabled;

    @Value("${citizen.login.password.otp.fixed.value}")
    private String fixedOTPPassword;

    @Value("${citizen.login.password.otp.fixed.enabled}")
    private boolean fixedOTPEnabled;


    public JwtExchangeAuthenticationProvider(
            JwtValidationService jwtValidationService,
            UserService userService, MultiStateInstanceUtil centraInstanceUtil, EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.jwtValidationService = jwtValidationService;
        this.userService = userService;
        this.centraInstanceUtil = centraInstanceUtil;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
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
            user = encryptionDecryptionUtil.decryptObject(user, "UserSelf", User.class, requestInfo);
        } catch (UserNotFoundException e) {
            // TODO: create user if not exists
            // Convert jwt to user request
            log.info("User not found for user id: {}, creating new user", jwt.getExternalUserId(), e);
            User newUser = convertJwtToUser(jwt);
            requestInfo = getRequestInfo(newUser);
            user = userService.createUser(newUser, requestInfo);
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

    private User convertJwtToUser(OidcValidatedJwt jwt) {
        Set<Role> roles = new HashSet<>();
        if (!CollectionUtils.isEmpty(jwt.getRoles())) {
            for (String role : jwt.getRoles()) {
                Role domainRole = new Role();
                domainRole.setCode(role);
                domainRole.setTenantId(jwt.getTenantId());
                roles.add(domainRole);
            }
        }
        return User.builder()
                .uuid(jwt.getExternalUserId())
                .idpSubject(jwt.getSubject())
                .idpIssuer(jwt.getIssuer())
                .idpTokenExp(jwt.getExpirationTime())
                .emailId(jwt.getEmail())
                .active(true)
                .accountLocked(false)
                .type(jwt.getUserType() == null ? UserType.EMPLOYEE : UserType.fromValue(jwt.getUserType()))
                .tenantId(jwt.getTenantId())
                .roles(roles)
                .username(jwt.getPreferredUsername())
                .name(jwt.getName())
                .mobileValidationMandatory(false)
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
}

