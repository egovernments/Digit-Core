package org.egov.user.security.oauth2.custom.authproviders;

import static java.util.Objects.isNull;
import static org.egov.user.config.UserServiceConstants.IP_HEADER_NAME;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.MDC;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.ServiceCallException;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.LoginStatus;
import org.egov.user.domain.model.enums.LoginType;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.web.contract.auth.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("customAuthProvider")
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    /**
     * TO-Do:Need to remove this and provide authentication for web, based on
     * authentication_code.
     */

    // TODO Remove default error handling provided by TokenEndpoint.class

    private UserService userService;
    
    @Autowired
    private MultiStateInstanceUtil centraInstanceUtil;
    
    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Value("${citizen.login.password.otp.enabled}")
    private boolean citizenLoginPasswordOtpEnabled;

    @Value("${employee.login.password.otp.enabled}")
    private boolean employeeLoginPasswordOtpEnabled;

    @Value("${citizen.login.password.otp.fixed.value}")
    private String fixedOTPPassword;

    @Value("${citizen.login.password.otp.fixed.enabled}")
    private boolean fixedOTPEnabled;

    @Autowired
    private HttpServletRequest request;


    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();

        final LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getDetails();

        String tenantId = details.get("tenantId");
        String userType = details.get("userType");
		/*
		 * Central instance tenant MDC enanchement
		 */
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
            user = userService.getUniqueUser(userName, tenantId, UserType.fromValue(userType));
            /* decrypt here otp service and final response need decrypted data*/
            Set<org.egov.user.domain.model.Role> domain_roles = user.getRoles();
            List<org.egov.common.contract.request.Role> contract_roles = new ArrayList<>();
            for (org.egov.user.domain.model.Role role : domain_roles) {
                contract_roles.add(org.egov.common.contract.request.Role.builder().code(role.getCode()).name(role.getName()).build());
            }

            org.egov.common.contract.request.User userInfo = org.egov.common.contract.request.User.builder().uuid(user.getUuid())
                    .type(user.getType() != null ? user.getType().name() : null).roles(contract_roles).build();
            requestInfo = RequestInfo.builder().userInfo(userInfo).build();
            user = encryptionDecryptionUtil.decryptObject(user, "UserSelf", User.class, requestInfo);

        } catch (UserNotFoundException e) {
            log.error("User not found", e);
            throw new OAuth2Exception("Invalid login credentials");
        } catch (DuplicateUserNameException e) {
            log.error("Fatal error, user conflict, more than one user found", e);
            throw new OAuth2Exception("Invalid login credentials");

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


        boolean isCitizen = false;
        if (user.getType() != null && user.getType().equals(UserType.CITIZEN))
            isCitizen = true;

        LoginType loginType;
        String ipAddress = request.getHeader(IP_HEADER_NAME);

        boolean isPasswordMatched;
        if (isCitizen) {
            if (fixedOTPEnabled && !fixedOTPPassword.equals("") && fixedOTPPassword.equals(password)) {
                //for automation allow fixing otp validation to a fixed otp
                isPasswordMatched = true;
                loginType = LoginType.OTP;
            } else {
                isPasswordMatched = isPasswordMatch(citizenLoginPasswordOtpEnabled, password, user, authentication);
                loginType = citizenLoginPasswordOtpEnabled ? LoginType.OTP : LoginType.PASSWORD;
            }
        } else {
            isPasswordMatched = isPasswordMatch(employeeLoginPasswordOtpEnabled, password, user, authentication);
            loginType = employeeLoginPasswordOtpEnabled ? LoginType.OTP : LoginType.PASSWORD;
        }

        if (isPasswordMatched) {

			/*
			  We assume that there will be only one type. If it is multiple
			  then we have change below code Separate by comma or other and
			  iterate
			 */
            List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_" + user.getType()));
            final SecureUser secureUser = new SecureUser(getUser(user));
            userService.resetFailedLoginAttempts(user);
            userService.auditLogin(user,ipAddress,loginType,LoginStatus.SUCCESS);
            return new UsernamePasswordAuthenticationToken(secureUser,
                    password, grantedAuths);
        } else {
            // Handle failed login attempt
            // Fetch Real IP after being forwarded by reverse proxy
            userService.handleFailedLogin(user, request.getHeader(IP_HEADER_NAME), requestInfo);
            userService.auditLogin(user,ipAddress,loginType,LoginStatus.FAILURE);
            throw new OAuth2Exception("Invalid login credentials");
        }

    }

    private boolean isPasswordMatch(Boolean isOtpBased, String password, User user, Authentication authentication) {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        final LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getDetails();
        String isCallInternal = details.get("isInternal");
        if (isOtpBased) {
            if (null != isCallInternal && isCallInternal.equals("true")) {
                log.debug("Skipping otp validation during login.........");
                return true;
            }
            user.setOtpReference(password);
            try {
                return userService.validateOtp(user);
            } catch (ServiceCallException e) {
                log.error("OTP validation failed ");
                return false;
            }
        } else {
            if (null != isCallInternal && isCallInternal.equals("true")) {
                log.debug("Skipping password validation during login.........");
                return true;
            }
            return bcrypt.matches(password, user.getPassword());
        }
    }

    @SuppressWarnings("unchecked")
    private String getTenantId(Authentication authentication) {
        final LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getDetails();

        System.out.println("details------->" + details);
        System.out.println("tenantId in CustomAuthenticationProvider------->" + details.get("tenantId"));

        final String tenantId = details.get("tenantId");
        if (isEmpty(tenantId)) {
            throw new OAuth2Exception("TenantId is mandatory");
        }
        return tenantId;
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

    private Set<Role> toAuthRole(Set<org.egov.user.domain.model.Role> domainRoles) {
        if (domainRoles == null)
            return new HashSet<>();
        return domainRoles.stream().map(org.egov.user.web.contract.auth.Role::new).collect(Collectors.toSet());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);

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
