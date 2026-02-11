package org.egov.user.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private Oidc oidc = new Oidc();

    private Forward forward = new Forward();
    private List<Provider> providers = new ArrayList<>();
    private String defaultBoundaryCode;

    @Getter
    @Setter
    public static class Oidc {
        private boolean enabled = false;
        /**
         * Source of OIDC provider list: "static" (from auth.providers) or "mdms" (from MDMS master).
         * When "mdms", provider config is fetched from MDMS and works for any OIDC IdP (Azure, Google, etc.).
         */
        private String providersSource = "static";
    }

    @Getter
    @Setter
    public static class Forward {
        private boolean authorizationHeader = true;
        private boolean requestinfoAuthtoken = false;
    }

    @Getter
    @Setter
    public static class Provider {
        private String id;
        private String issuerUri;
        /**
         * Optional alternate issuer values for the same provider.
         * Useful when an IdP can emit tokens with multiple valid issuer formats
         * (e.g. Azure AD v1 vs v2 issuer strings).
         *
         * Property: auth.providers[i].issuer-aliases[0..n]
         */
        private List<String> issuerAliases = new ArrayList<>();
        private String jwkSetUri;
        private List<String> audiences = new ArrayList<>();
        private String tenantId;
        private String userType = "EMPLOYEE";
        private String employeeType = "PERMANENT";
        private String defaultRoleCodes;
        private String roleClaimKey = "roles";
        private Map<String, String> roleMapping;
        private Map<String, String> roleBoundaryMapping;
        private String hierarchyType;
        private String projectName;
        private String defaultPassword;
        private Long defaultDob;
        private String defaultDesignation;
        private String defaultDepartment;
        private String mobileNumberPrefix;
        private Integer mobileNumberLength;
        private String defaultEmployeeStatus = "EMPLOYED";
        private String rolePrefix = "ROLE_";
        /** Optional. When set, SSO-created employee username uses this pattern: {provider}, {tenantId}, {role}, {idpRole}, {number}. */
        private String employeeUsernameFormat;
        /** Short keyword for {provider} in username (e.g. ms, google, okta) so you can see which IdP created the user. */
        private String employeeUsernameProviderKey;
        /** Zero-padded length for {number} in employeeUsernameFormat (default 6). */
        private Integer employeeUsernameNumberLength = 6;
        private String defaultBoundaryCode;
        private String decryptionPurpose = "UserSelf";
        private String graphClientId;
        private String graphClientSecret;
        private String graphTenantId;
        private String graphMethodsUrl = "https://graph.microsoft.com/v1.0/users/%s/authentication/methods";
        private String graphTokenUrl = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
        private String graphScope = "https://graph.microsoft.com/.default";

        @JsonSetter("roleMapping")
        public void setRoleMapping(String roleMappingString) throws IOException {
            roleMapping = (Map<String, String>) new ObjectMapper().readValue(roleMappingString, Map.class);
        }

        @JsonSetter("roleBoundaryMapping")
        public void setRoleBoundaryMapping(String roleBoundaryMappingString) throws IOException {
            roleBoundaryMapping = (Map<String, String>) new ObjectMapper().readValue(roleBoundaryMappingString,
                    Map.class);
        }
    }
}
