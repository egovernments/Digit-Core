package org.egov.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.*;

/**
 * Configuration properties for authentication and SSO settings.
 * 
 * <p>This class holds all configuration related to OIDC/SSO authentication including
 * provider configurations, forwarding settings, and various authentication parameters.
 * The configuration is typically loaded from application properties with the "auth" prefix.</p>
 * 
 * <p>Key features supported:</p>
 * <ul>
 *   <li>OIDC provider configuration (static or MDMS-based)</li>
 *   <li>Multiple identity provider support with issuer aliases</li>
 *   <li>Role and designation mapping from JWT claims</li>
 *   <li>Microsoft Graph integration for MFA enrichment</li>
 *   <li>Audience validation and security settings</li>
 * </ul>
 * 
 * @see org.egov.user.config.AuthProperties.Oidc
 * @see org.egov.user.config.AuthProperties.Provider
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private Oidc oidc = new Oidc();

    private List<Provider> providers = new ArrayList<>();

    /**
     * OIDC configuration settings.
     * 
     * <p>Controls the overall OIDC authentication behavior including provider source
     * selection and enablement flag. When disabled, OIDC authentication is bypassed.</p>
     */
    @Getter
    @Setter
    public static class Oidc {
        private boolean enabled = false;
        /**
         * Source of OIDC provider list: "static" (from auth.providers) or "mdms" (from MDMS master).
         * When "mdms", provider config is fetched from MDMS and works for any OIDC IdP (Azure, Google, etc.).
         */
        private String providersSource = OidcConfigConstants.PROVIDERS_SOURCE_STATIC;

        /**
         * Global TTL in milliseconds for all JWKS and JWT decoder caches.
         *
         * <p>If set to a positive value, all JWKS/JWT decoder cache implementations
         * must use this value as their cache TTL unless they have a more specific
         * override. When this property is {@code null} or less than or equal to zero,
         * implementations must fall back to
         * {@link org.egov.user.config.OidcConfigConstants#DEFAULT_JWKS_CACHE_TTL_MS}.</p>
         *
         * <p>Property: {@code auth.oidc.jwks-cache-ttl-ms}</p>
         */
        private Long jwksCacheTtlMs;
    }

    /**
     * OIDC provider configuration.
     * 
     * <p>Represents a complete configuration for an OIDC identity provider including
     * endpoints, validation rules, role mappings, and integration settings. Each provider
     * supports comprehensive JWT token validation and user attribute mapping.</p>
     * 
     * <p>Key capabilities:</p>
     * <ul>
     *   <li>JWT signature validation via JWKS</li>
     *   <li>Audience and issuer validation</li>
     *   <li>Role and designation mapping from claims</li>
     *   <li>Microsoft Graph integration for enrichment</li>
     *   <li>Multiple issuer URI support via aliases</li>
     *   <li>IdP user validation for username/password login</li>
     * </ul>
     */
    @Getter
    public static class Provider {
        private final String id;
        private final String issuerUri;
        /**
         * Optional alternate issuer values for the same provider.
         * Useful when an IdP can emit tokens with multiple valid issuer formats
         * (e.g. Azure AD v1 vs v2 issuer strings).
         *
         * Property: auth.providers[i].issuer-aliases[0..n]
         */
        private final List<String> issuerAliases;
        private final String jwkSetUri;
        private final List<String> audiences;
        private final String tenantId;
        private final String userType;
        private final String defaultRoleCodes;
        private final String roleClaimKey;
        private final Map<String, String> roleMapping;
        private final Map<String, String> designationMapping;
        private final String defaultDesignationCode;
        private final String defaultDepartmentCode;
        private final String designationClaimKey;
        private final String defaultBoundaryHierarchyType;
        private final Long defaultDob;
        private final String defaultEmployeeStatus;
        private final String rolePrefix;
        private final String decryptionPurpose;
        private final String graphClientId;
        private final String graphTenantId;
        private final String graphMethodsUrl;
        private final String graphUsersUrl;
        private final String graphTokenUrl;
        private final String graphScope;
        private final String graphAppRoleAssignmentUrl;
        /**
         * IdP-specific graph/MFA enrichment service type: "azure" (Microsoft Graph), "none", or custom.
         * Property: auth.providers[i].graph-service-type
         */
        private final String graphServiceType;
        /**
         * Azure AD resource ID (UUID) of the app whose app role assignments are checked for IdP user validation.
         * Property: auth.providers[i].graph-app-resource-id
         */
        private final String graphAppResourceId;
        /**
         * IdP-specific validator for username/password login: when user has non-LOCAL authProvider,
         * this validator checks if the user still has access at the IdP. "none" = skip validation.
         * Property: auth.providers[i].idp-user-validator-type
         */
        private final String idpUserValidatorType;
        /**
         * Provider type for access token validation: determines which validator to use.
         * Property: auth.providers[i].providerType
         */
        private final String providerType;

        static final ObjectMapper ROLE_MAPPING_MAPPER = new ObjectMapper();

        // Default constructor for Spring Boot configuration binding
        public Provider() {
            this.id = null;
            this.issuerUri = null;
            this.issuerAliases = Collections.emptyList();
            this.jwkSetUri = null;
            this.audiences = Collections.emptyList();
            this.tenantId = null;
            this.userType = "EMPLOYEE";
            this.defaultRoleCodes = null;
            this.roleClaimKey = OidcConfigConstants.DEFAULT_ROLE_CLAIM_KEY;
            this.roleMapping = Collections.emptyMap();
            this.designationMapping = Collections.emptyMap();
            this.defaultDesignationCode = null;
            this.defaultDepartmentCode = null;
            this.designationClaimKey = null;
            this.defaultBoundaryHierarchyType = null;
            this.defaultDob = null;
            this.defaultEmployeeStatus = OidcConfigConstants.DEFAULT_EMPLOYED_STATUS;
            this.rolePrefix = OidcConfigConstants.DEFAULT_ROLE_PREFIX;
            this.decryptionPurpose = OidcConfigConstants.DEFAULT_DECRYPTION_PURPOSE;
            this.graphClientId = null;
            this.graphTenantId = null;
            this.graphMethodsUrl = OidcConfigConstants.DEFAULT_GRAPH_METHODS_URL;
            this.graphUsersUrl = OidcConfigConstants.DEFAULT_GRAPH_USERS_URL;
            this.graphTokenUrl = OidcConfigConstants.DEFAULT_GRAPH_TOKEN_URL;
            this.graphScope = OidcConfigConstants.DEFAULT_GRAPH_SCOPE;
            this.graphAppRoleAssignmentUrl = OidcConfigConstants.DEFAULT_GRAPH_APP_ROLE_ASSIGNMENTS_URL;
            this.graphServiceType = OidcConfigConstants.GRAPH_SERVICE_TYPE_AZURE;
            this.graphAppResourceId = null;
            this.idpUserValidatorType = OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_NONE;
            this.providerType = OidcConfigConstants.PROVIDER_TYPE_MICROSOFT;
        }

        // Full constructor for creating immutable instances
        public Provider(String id, String issuerUri, List<String> issuerAliases, String jwkSetUri,
                       List<String> audiences, String tenantId, String userType, String defaultRoleCodes,
                       String roleClaimKey, Map<String, String> roleMapping, Map<String, String> designationMapping,
                       String defaultDesignationCode, String defaultDepartmentCode, String designationClaimKey, String defaultBoundaryHierarchyType,
                       Long defaultDob, String defaultEmployeeStatus, String rolePrefix, String decryptionPurpose,
                       String graphClientId, String graphTenantId, String graphMethodsUrl, String graphUsersUrl,
                       String graphTokenUrl, String graphScope, String graphAppRoleAssignmentUrl,
                       String graphServiceType, String graphAppResourceId, String idpUserValidatorType, String providerType) {
            this.id = id;
            this.issuerUri = issuerUri;
            this.issuerAliases = issuerAliases != null ? Collections.unmodifiableList(new ArrayList<>(issuerAliases)) : Collections.emptyList();
            this.jwkSetUri = jwkSetUri;
            this.audiences = audiences != null ? Collections.unmodifiableList(new ArrayList<>(audiences)) : Collections.emptyList();
            this.tenantId = tenantId;
            this.userType = userType != null ? userType : "EMPLOYEE";
            this.defaultRoleCodes = defaultRoleCodes;
            this.roleClaimKey = roleClaimKey != null ? roleClaimKey : OidcConfigConstants.DEFAULT_ROLE_CLAIM_KEY;
            this.roleMapping = roleMapping != null ? Collections.unmodifiableMap(new HashMap<>(roleMapping)) : Collections.emptyMap();
            this.designationMapping = designationMapping != null ? Collections.unmodifiableMap(new HashMap<>(designationMapping)) : Collections.emptyMap();
            this.defaultDesignationCode = defaultDesignationCode;
            this.defaultDepartmentCode = defaultDepartmentCode;
            this.designationClaimKey = designationClaimKey;
            this.defaultBoundaryHierarchyType = defaultBoundaryHierarchyType;
            this.defaultDob = defaultDob;
            this.defaultEmployeeStatus = defaultEmployeeStatus != null ? defaultEmployeeStatus : OidcConfigConstants.DEFAULT_EMPLOYED_STATUS;
            this.rolePrefix = rolePrefix != null ? rolePrefix : OidcConfigConstants.DEFAULT_ROLE_PREFIX;
            this.decryptionPurpose = decryptionPurpose != null ? decryptionPurpose : OidcConfigConstants.DEFAULT_DECRYPTION_PURPOSE;
            this.graphClientId = graphClientId;
            this.graphTenantId = graphTenantId;
            this.graphMethodsUrl = graphMethodsUrl != null ? graphMethodsUrl : OidcConfigConstants.DEFAULT_GRAPH_METHODS_URL;
            this.graphUsersUrl = graphUsersUrl != null ? graphUsersUrl : OidcConfigConstants.DEFAULT_GRAPH_USERS_URL;
            this.graphTokenUrl = graphTokenUrl != null ? graphTokenUrl : OidcConfigConstants.DEFAULT_GRAPH_TOKEN_URL;
            this.graphScope = graphScope != null ? graphScope : OidcConfigConstants.DEFAULT_GRAPH_SCOPE;
            this.graphAppRoleAssignmentUrl = graphAppRoleAssignmentUrl != null ? graphAppRoleAssignmentUrl : OidcConfigConstants.DEFAULT_GRAPH_APP_ROLE_ASSIGNMENTS_URL;
            this.graphServiceType = graphServiceType != null ? graphServiceType : OidcConfigConstants.GRAPH_SERVICE_TYPE_AZURE;
            this.graphAppResourceId = graphAppResourceId;
            this.idpUserValidatorType = idpUserValidatorType != null ? idpUserValidatorType : OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_NONE;
            this.providerType = providerType != null ? providerType : OidcConfigConstants.PROVIDER_TYPE_MICROSOFT;
        }

        // Builder pattern for easier construction
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String issuerUri;
            private List<String> issuerAliases = new ArrayList<>();
            private String jwkSetUri;
            private List<String> audiences = new ArrayList<>();
            private String tenantId;
            private String userType = "EMPLOYEE";
            private String defaultRoleCodes;
            private String roleClaimKey = OidcConfigConstants.DEFAULT_ROLE_CLAIM_KEY;
            private Map<String, String> roleMapping = new HashMap<>();
            private Map<String, String> designationMapping = new HashMap<>();
            private String defaultDesignationCode;
            private String defaultDepartmentCode;
            private String designationClaimKey;
            private String defaultBoundaryHierarchyType;
            private Long defaultDob;
            private String defaultEmployeeStatus = OidcConfigConstants.DEFAULT_EMPLOYED_STATUS;
            private String rolePrefix = OidcConfigConstants.DEFAULT_ROLE_PREFIX;
            private String decryptionPurpose = OidcConfigConstants.DEFAULT_DECRYPTION_PURPOSE;
            private String graphClientId;
            private String graphTenantId;
            private String graphMethodsUrl = OidcConfigConstants.DEFAULT_GRAPH_METHODS_URL;
            private String graphUsersUrl = OidcConfigConstants.DEFAULT_GRAPH_USERS_URL;
            private String graphTokenUrl = OidcConfigConstants.DEFAULT_GRAPH_TOKEN_URL;
            private String graphScope = OidcConfigConstants.DEFAULT_GRAPH_SCOPE;
            private String graphAppRoleAssignmentUrl = OidcConfigConstants.DEFAULT_GRAPH_APP_ROLE_ASSIGNMENTS_URL;
            private String graphServiceType = OidcConfigConstants.GRAPH_SERVICE_TYPE_AZURE;
            private String graphAppResourceId;
            private String idpUserValidatorType = OidcConfigConstants.IDP_USER_VALIDATOR_TYPE_NONE;
            private String providerType = OidcConfigConstants.PROVIDER_TYPE_MICROSOFT;

            public Builder id(String id) { this.id = id; return this; }
            public Builder issuerUri(String issuerUri) { this.issuerUri = issuerUri; return this; }
            public Builder issuerAliases(List<String> issuerAliases) { this.issuerAliases = issuerAliases; return this; }
            public Builder jwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; return this; }
            public Builder audiences(List<String> audiences) { this.audiences = audiences; return this; }
            public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
            public Builder userType(String userType) { this.userType = userType; return this; }
            public Builder defaultRoleCodes(String defaultRoleCodes) { this.defaultRoleCodes = defaultRoleCodes; return this; }
            public Builder roleClaimKey(String roleClaimKey) { this.roleClaimKey = roleClaimKey; return this; }
            public Builder roleMapping(Map<String, String> roleMapping) { this.roleMapping = roleMapping; return this; }
            public Builder designationMapping(Map<String, String> designationMapping) { this.designationMapping = designationMapping; return this; }
            public Builder defaultDesignationCode(String defaultDesignationCode) { this.defaultDesignationCode = defaultDesignationCode; return this; }
            public Builder defaultDepartmentCode(String defaultDepartmentCode) { this.defaultDepartmentCode = defaultDepartmentCode; return this; }
            public Builder designationClaimKey(String designationClaimKey) { this.designationClaimKey = designationClaimKey; return this; }
            public Builder defaultBoundaryHierarchyType(String defaultBoundaryHierarchyType) { this.defaultBoundaryHierarchyType = defaultBoundaryHierarchyType; return this; }
            public Builder defaultDob(Long defaultDob) { this.defaultDob = defaultDob; return this; }
            public Builder defaultEmployeeStatus(String defaultEmployeeStatus) { this.defaultEmployeeStatus = defaultEmployeeStatus; return this; }
            public Builder rolePrefix(String rolePrefix) { this.rolePrefix = rolePrefix; return this; }
            public Builder decryptionPurpose(String decryptionPurpose) { this.decryptionPurpose = decryptionPurpose; return this; }
            public Builder graphClientId(String graphClientId) { this.graphClientId = graphClientId; return this; }
            public Builder graphTenantId(String graphTenantId) { this.graphTenantId = graphTenantId; return this; }
            public Builder graphMethodsUrl(String graphMethodsUrl) { this.graphMethodsUrl = graphMethodsUrl; return this; }
            public Builder graphUsersUrl(String graphUsersUrl) { this.graphUsersUrl = graphUsersUrl; return this; }
            public Builder graphTokenUrl(String graphTokenUrl) { this.graphTokenUrl = graphTokenUrl; return this; }
            public Builder graphScope(String graphScope) { this.graphScope = graphScope; return this; }
            public Builder graphAppRoleAssignmentUrl(String graphAppRoleAssignmentUrl) { this.graphAppRoleAssignmentUrl = graphAppRoleAssignmentUrl; return this; }
            public Builder graphServiceType(String graphServiceType) { this.graphServiceType = graphServiceType; return this; }
            public Builder graphAppResourceId(String graphAppResourceId) { this.graphAppResourceId = graphAppResourceId; return this; }
            public Builder idpUserValidatorType(String idpUserValidatorType) { this.idpUserValidatorType = idpUserValidatorType; return this; }
            public Builder providerType(String providerType) { this.providerType = providerType; return this; }

            public Provider build() {
                return new Provider(id, issuerUri, issuerAliases, jwkSetUri, audiences, tenantId,
                        userType, defaultRoleCodes, roleClaimKey, roleMapping, designationMapping,
                        defaultDesignationCode, defaultDepartmentCode, designationClaimKey, defaultBoundaryHierarchyType,
                        defaultDob, defaultEmployeeStatus, rolePrefix, decryptionPurpose,
                        graphClientId, graphTenantId, graphMethodsUrl, graphUsersUrl,
                        graphTokenUrl, graphScope, graphAppRoleAssignmentUrl,
                        graphServiceType, graphAppResourceId, idpUserValidatorType, providerType);
            }
        }
    }
}
