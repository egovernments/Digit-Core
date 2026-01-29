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
        private String rolePrefix = "ROLE_";
        private String defaultBoundaryCode;
        private String decryptionPurpose = "UserSelf";

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
