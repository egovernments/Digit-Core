package org.egov.user.identity;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.web.contract.auth.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.egov.user.config.UserServiceConstants.IDENTITY_CLIENT_ID;

@Service
public class IdentityBridgeService {

    private final IdentityRepository repository;
    private final IdentityJwtVerifier jwtVerifier;
    private final DefaultTokenServices tokenServices;
    private final UserService userService;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;
    private final String workloadToken;
    private final String allowedClientId;
    private final Set<String> allowedRoles;

    public IdentityBridgeService(
            IdentityRepository repository,
            IdentityJwtVerifier jwtVerifier,
            DefaultTokenServices tokenServices,
            UserService userService,
            EncryptionDecryptionUtil encryptionDecryptionUtil,
            @Value("${identity.service.token:}") String workloadToken,
            @Value("${identity.allowed.client.id:digit-ui}") String allowedClientId,
            @Value("${identity.allowed.role.codes:EMPLOYEE}") String allowedRoleCodes) {
        this.repository = repository;
        this.jwtVerifier = jwtVerifier;
        this.tokenServices = tokenServices;
        this.userService = userService;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.workloadToken = workloadToken;
        this.allowedClientId = allowedClientId;
        this.allowedRoles = new HashSet<String>();
        for (String role : allowedRoleCodes.split(",")) {
            if (!role.trim().isEmpty()) this.allowedRoles.add(role.trim());
        }
    }

    public void requireWorkload(String authorization) {
        if (workloadToken.isEmpty()) throw new IdentityException(503, "Identity bridge is not configured");
        String supplied = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : "";
        if (supplied.isEmpty() || !MessageDigest.isEqual(
                supplied.getBytes(StandardCharsets.UTF_8), workloadToken.getBytes(StandardCharsets.UTF_8))) {
            throw new IdentityException(401, "Invalid identity workload credential");
        }
    }

    public List<IdentityContext> resolveContexts(Map<String, Object> request) {
        requireAllowedClient(request);
        Map<String, Object> identity = object(request.get("identity"), "identity");
        String issuer = string(identity.get("issuer"), "identity.issuer");
        String subject = string(identity.get("subject"), "identity.subject");
        Object rawOrganizations = request.get("organizations");
        if (!(rawOrganizations instanceof List)) {
            throw new IdentityException(400, "organizations must be an array");
        }
        List<IdentityContext> contexts = new ArrayList<IdentityContext>();
        Set<String> seen = new HashSet<String>();
        for (Object value : (List<?>) rawOrganizations) {
            Map<String, Object> organization = object(value, "organizations[]");
            String organizationId = string(organization.get("organizationId"), "organizationId");
            if (!seen.add(organizationId)) continue;
            IdentityContext context = repository.resolveContext(issuer, subject, organizationId);
            if (context != null) contexts.add(context);
        }
        return contexts;
    }

    @Transactional
    public String ensureSubject(Map<String, Object> request) {
        return repository.ensureSubject(
                string(request.get("issuer"), "issuer"),
                string(request.get("subject"), "subject"),
                string(request.get("digitUserUuid"), "digitUserUuid"),
                System.currentTimeMillis());
    }

    /**
     * Returns the DIGIT employee for a verified external subject, creating one
     * only when the subject is unlinked and no existing employee is named. The
     * created employee has no usable local credential and only the base
     * EMPLOYEE role at the Organization's tenant; authorization roles are
     * granted afterwards through membership reconciliation. A subject keeps the
     * same DIGIT user UUID across later Organization memberships.
     */
    @Transactional
    public Map<String, Object> ensureEmployee(Map<String, Object> request) {
        String issuer = string(request.get("issuer"), "issuer");
        String subject = string(request.get("subject"), "subject");
        String tenantId = repository.requireActiveOrganizationTenant(
                string(request.get("organizationId"), "organizationId"));
        String requestedUuid = optionalString(request.get("digitUserUuid"));
        long now = System.currentTimeMillis();

        String linked = repository.findLinkedUserUuid(issuer, subject);
        if (linked != null || requestedUuid != null) {
            String uuid = linked != null ? linked : requestedUuid;
            if (requestedUuid != null && !requestedUuid.equalsIgnoreCase(uuid)) {
                throw new IdentityException(409, "Identity subject is already linked to another DIGIT user");
            }
            repository.ensureSubject(issuer, subject, uuid, now);
            return employeeResponse(uuid, false);
        }

        Map<String, Object> profile = object(request.get("profile"), "profile");
        String name = string(profile.get("name"), "profile.name");
        String mobileNumber = optionalString(profile.get("mobileNumber"));
        org.egov.user.domain.model.User user = org.egov.user.domain.model.User.builder()
                .username(identityUsername(issuer, subject))
                .name(name.length() > 50 ? name.substring(0, 50) : name)
                .emailId(optionalString(profile.get("emailId")))
                .mobileNumber(mobileNumber)
                .mobileValidationMandatory(mobileNumber != null)
                .active(true).type(UserType.EMPLOYEE).tenantId(tenantId).locale("en_IN")
                .roles(Collections.singleton(org.egov.user.domain.model.Role.builder()
                        .code("EMPLOYEE").name("Employee").tenantId(tenantId).build()))
                .build();
        org.egov.user.domain.model.User created;
        try {
            created = userService.createIdentityProviderEmployee(user, new RequestInfo());
        } catch (DuplicateUserNameException exception) {
            throw new IdentityException(409, "Identity employee provisioning is already in progress");
        }
        repository.ensureSubject(issuer, subject, created.getUuid(), now);
        return employeeResponse(created.getUuid(), true);
    }

    private Map<String, Object> employeeResponse(String uuid, boolean created) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("digitUserUuid", uuid);
        response.put("created", created);
        return response;
    }

    static String identityUsername(String issuer, String subject) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((issuer + "\n" + subject).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder("idp-");
            for (int i = 0; i < 16; i++) hex.append(String.format("%02x", digest[i]));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Transactional
    public String ensureOrganization(Map<String, Object> request) {
        return repository.ensureOrganization(
                string(request.get("organizationId"), "organizationId"),
                string(request.get("alias"), "alias"),
                string(request.get("tenantId"), "tenantId"),
                string(request.get("name"), "name"),
                System.currentTimeMillis());
    }

    @Transactional
    public String reconcileMembership(Map<String, Object> request) {
        String issuer = string(request.get("issuer"), "issuer");
        String subject = string(request.get("subject"), "subject");
        String organizationId = string(request.get("organizationId"), "organizationId");
        boolean active = !(request.get("active") instanceof Boolean) || (Boolean) request.get("active");
        Object requestedRoles = request.get("roles");
        if (!(requestedRoles instanceof List)) {
            throw new IdentityException(400, "roles must be an array");
        }
        Set<String> roles = new HashSet<String>();
        for (Object value : (List<?>) requestedRoles) {
            String role = string(value, "roles[]");
            if (!allowedRoles.contains(role)) {
                throw new IdentityException(400, "Role is not allowed for identity projection: " + role);
            }
            roles.add(role);
        }
        java.util.UUID membershipId = repository.reconcileMembership(
                repository.requireSubjectId(issuer, subject), organizationId, roles,
                active, System.currentTimeMillis());
        return membershipId == null ? null : membershipId.toString();
    }

    public List<Map<String, Object>> reconciliationSnapshot() {
        return repository.reconciliationSnapshot();
    }

    public Map<String, Object> exchange(String authorization, Map<String, Object> request) {
        requireAllowedClient(request);
        IdentityAssertion assertion = jwtVerifier.verify(authorization);
        Map<String, Object> requestedContext = object(request.get("context"), "context");
        String organizationId = string(requestedContext.get("organizationId"), "context.organizationId");
        String tenantId = string(requestedContext.get("tenantId"), "context.tenantId");
        if (!organizationId.equals(assertion.getOrganizationId())) {
            throw new IdentityException(403, "Requested organization is not present in the assertion");
        }

        IdentityUserContext context = repository.requireUserContext(assertion);
        if (!tenantId.equals(context.getTenantId())) {
            throw new IdentityException(403, "Requested tenant does not match the mapped organization");
        }
        User user = context.getUser();
        applyDecryptedIdentity(user);
        SecureUser secureUser = new SecureUser(user);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("client_id", IDENTITY_CLIENT_ID);
        parameters.put("tenantId", tenantId);
        parameters.put("authorizationVersion", Long.toString(context.getAuthorizationVersion()));
        Set<String> scopes = new HashSet<String>(Arrays.asList("read", "write"));
        OAuth2Request oauthRequest = new OAuth2Request(
                parameters, IDENTITY_CLIENT_ID, secureUser.getAuthorities(), true,
                scopes, Collections.<String>emptySet(), null,
                Collections.<String>emptySet(), Collections.<String, java.io.Serializable>emptyMap());
        PreAuthenticatedAuthenticationToken userAuthentication =
                new PreAuthenticatedAuthenticationToken(
                        secureUser, null, secureUser.getAuthorities());
        OAuth2AccessToken accessToken = tokenServices.createAccessToken(
                new OAuth2Authentication(oauthRequest, userAuthentication));

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("access_token", accessToken.getValue());
        response.put("token_type", "bearer");
        response.put("expires_in", accessToken.getExpiresIn());
        response.put("UserRequest", user);
        return response;
    }

    /**
     * eg_user stores identity fields encrypted. Decrypt them exactly as password
     * login does ("UserSelf" for the user's own record) so the exchanged token
     * carries the same principal a native DIGIT login would.
     */
    private void applyDecryptedIdentity(User user) {
        org.egov.user.domain.model.User stored = userService.getUserByUuid(user.getUuid());
        List<org.egov.common.contract.request.Role> roles =
                new ArrayList<org.egov.common.contract.request.Role>();
        if (stored.getRoles() != null) {
            for (org.egov.user.domain.model.Role role : stored.getRoles()) {
                roles.add(org.egov.common.contract.request.Role.builder()
                        .code(role.getCode()).name(role.getName()).build());
            }
        }
        RequestInfo requestInfo = RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder()
                        .uuid(stored.getUuid())
                        .type(stored.getType() == null ? null : stored.getType().name())
                        .roles(roles).build())
                .build();
        org.egov.user.domain.model.User decrypted = encryptionDecryptionUtil.decryptObject(
                stored, "UserSelf", org.egov.user.domain.model.User.class, requestInfo);
        user.setUserName(text(decrypted.getUsername()));
        user.setName(text(decrypted.getName()));
        user.setMobileNumber(text(decrypted.getMobileNumber()));
        user.setEmailId(text(decrypted.getEmailId()));
    }

    private String optionalString(Object value) {
        return value instanceof String && !((String) value).trim().isEmpty()
                ? ((String) value).trim() : null;
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private void requireAllowedClient(Map<String, Object> request) {
        String clientId = string(request.get("clientId"), "clientId");
        if (allowedClientId.isEmpty() || !allowedClientId.equals(clientId)) {
            throw new IdentityException(403, "Client is not allowed to request an identity context");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> object(Object value, String name) {
        if (!(value instanceof Map)) throw new IdentityException(400, name + " is required");
        return (Map<String, Object>) value;
    }

    private String string(Object value, String name) {
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IdentityException(400, name + " is required");
        }
        return ((String) value).trim();
    }
}
