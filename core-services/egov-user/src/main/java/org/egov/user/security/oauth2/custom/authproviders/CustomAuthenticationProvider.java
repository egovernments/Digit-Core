package org.egov.user.security.oauth2.custom.authproviders;

import static java.util.Objects.isNull;
import static org.egov.user.config.UserServiceConstants.IP_HEADER_NAME;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.MDC;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.ServiceCallException;
import org.egov.user.config.AuthProperties;
import org.egov.user.config.OidcConfigConstants;
import org.egov.user.config.OidcProviderSupplier;
import org.egov.user.config.UserServiceConstants;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.exception.sso.IdpUserAccessRevokedException;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.security.oauth2.custom.service.IdpUserValidator;
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

    private final UserService userService;

    @Autowired(required = false)
    private OidcProviderSupplier oidcProviderSupplier;

    @Autowired
    private List<IdpUserValidator> idpUserValidators = new ArrayList<>();
    
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

    /**
     * Authenticates a user using username/password or OTP.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Extracts username, password, tenant ID, and user type from authentication</li>
     *   <li>Looks up the user in the system</li>
     *   <li>Decrypts user data</li>
     *   <li>Validates account status (active, locked)</li>
     *   <li>Unlocks account if eligible</li>
     *   <li>Validates password or OTP based on configuration</li>
     *   <li>Handles failed login attempts</li>
     *   <li>Returns authenticated user with authorities</li>
     * </ol>
     *
     * @param authentication the UsernamePasswordAuthenticationToken containing credentials
     * @return UsernamePasswordAuthenticationToken with authenticated user and authorities
     * @throws OAuth2Exception if authentication fails (invalid credentials, account locked, etc.)
     */
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

        boolean isPasswordMatched;
        if (isCitizen) {
            if (fixedOTPEnabled && !fixedOTPPassword.equals("") && fixedOTPPassword.equals(password)) {
                //for automation allow fixing otp validation to a fixed otp
                isPasswordMatched = true;
            } else {
                isPasswordMatched = isPasswordMatch(citizenLoginPasswordOtpEnabled, password, user, authentication);
            }
        } else {
            isPasswordMatched = isPasswordMatch(employeeLoginPasswordOtpEnabled, password, user, authentication);
        }

        if (isPasswordMatched) {
            validateIdpAccess(user);
			/*
			  We assume that there will be only one type. If it is multiple
			  then we have change below code Separate by comma or other and
			  iterate
			 */
            List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_" + user.getType()));
            final SecureUser secureUser = new SecureUser(getUser(user));
            userService.resetFailedLoginAttempts(user);
            return new UsernamePasswordAuthenticationToken(secureUser,
                    password, grantedAuths);
        } else {
            // Handle failed login attempt
            // Fetch Real IP after being forwarded by reverse proxy
            userService.handleFailedLogin(user, request.getHeader(IP_HEADER_NAME), requestInfo);

            throw new OAuth2Exception("Invalid login credentials");
        }

    }

    /**
     * Validates that a user with non-LOCAL authProvider still has access at the IdP.
     * Skips when authProvider is LOCAL/null/blank, when OIDC supplier is absent, or when no provider/validator matches (fail-open).
     */
    private void validateIdpAccess(User user) {
        String authProvider = user.getAuthProvider();
        if (authProvider == null || authProvider.trim().isEmpty()
                || OidcConfigConstants.AUTH_PROVIDER_LOCAL.equalsIgnoreCase(authProvider.trim())) {
            return;
        }
        if (oidcProviderSupplier == null || idpUserValidators == null || idpUserValidators.isEmpty()) {
            return;
        }
        String tenantId = user.getTenantId();
        Optional<AuthProperties.Provider> providerOpt = oidcProviderSupplier.getProviders().stream()
                .filter(p -> p.getId() != null && p.getId().trim().equals(authProvider.trim())
                        && (tenantId == null || (p.getTenantId() != null && p.getTenantId().equals(tenantId))))
                .findFirst();
        if (!providerOpt.isPresent()) {
            log.warn("IdP user validation skipped: no provider config for authProvider={}, tenantId={}", authProvider, tenantId);
            return;
        }
        AuthProperties.Provider provider = providerOpt.get();
        IdpUserValidator validator = idpUserValidators.stream()
                .filter(v -> v.supports(provider))
                .findFirst()
                .orElse(null);
        if (validator == null) {
            log.warn("IdP user validation skipped: no IdpUserValidator supports provider id={}", provider.getId());
            return;
        }
        try {
            validator.validate(user, provider);
        } catch (IdpUserAccessRevokedException e) {
            // A genuine "this IdP user no longer has access" decision: deny the login.
            throw e;
        } catch (RuntimeException e) {
            // FAIL-SAFE. This call reaches an external IdP (e.g. MS Graph) on the password-grant
            // success path, after the password has already been verified. An IdP or network outage
            // must not take down password login for users carrying a non-LOCAL authProvider, so any
            // failure other than an explicit revocation is logged and the login is allowed.
            log.warn("IdP user validation could not be completed for authProvider={}, tenantId={}; "
                            + "allowing login on the already-verified password. Cause: {}",
                    authProvider, tenantId, e.toString());
        }
    }

    /**
     * Validates password or OTP based on configuration.
     * Supports both password-based and OTP-based authentication.
     * Skips validation for internal calls if configured.
     *
     * @param isOtpBased true if OTP-based authentication is enabled
     * @param password the password or OTP to validate
     * @param user the user object containing stored password/OTP reference
     * @param authentication the authentication object containing request details
     * @return true if password/OTP matches, false otherwise
     */
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

    /**
     * Extracts tenant ID from authentication details.
     *
     * @param authentication the authentication object
     * @return the tenant ID string
     * @throws OAuth2Exception if tenant ID is missing
     */
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

    /**
     * Converts a domain User object to a contract User object for API responses.
     *
     * @param user the domain User object
     * @return contract User object with user information
     */
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

    /**
     * Converts domain Role objects to contract Role objects.
     *
     * @param domainRoles set of domain Role objects
     * @return set of contract Role objects
     */
    private Set<Role> toAuthRole(Set<org.egov.user.domain.model.Role> domainRoles) {
        if (domainRoles == null)
            return new HashSet<>();
        return domainRoles.stream().map(org.egov.user.web.contract.auth.Role::new).collect(Collectors.toSet());
    }

    /**
     * Checks if this authentication provider supports the given authentication type.
     *
     * @param authentication the authentication class to check
     * @return true if the authentication is a UsernamePasswordAuthenticationToken, false otherwise
     */
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
