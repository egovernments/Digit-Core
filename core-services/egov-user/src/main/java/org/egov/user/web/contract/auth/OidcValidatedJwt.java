package org.egov.user.web.contract.auth;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class OidcValidatedJwt {
    private final Set<String> roles;
    private final Map<String, Object> claims;
    private final Date expirationTime;
    private final Date issuanceTime;
    private final String projectName;
    private final String hierarchy;
    private final String boundary;

    public OidcValidatedJwt(Set<String> roles, Map<String, Object> claims, Date expirationTime, Date issuanceTime, String projectName, String hierarchy, String boundary) {
        this.claims = claims;
        this.roles = roles;
        this.expirationTime = expirationTime;
        this.issuanceTime = issuanceTime;
        this.projectName = projectName;
        this.hierarchy = hierarchy;
        this.boundary = boundary;
    }

    public String getIssuer() {
        return (String) claims.get("iss");
    }

    public String getSubject() {
        return (String) claims.get("sub");
    }

    public String getEmail() {
        return (String) claims.get("email");
    }

    public String getName() {
        return (String) claims.getOrDefault("name", getSubject());
    }

    public String getPreferredUsername() {
        return (String) claims.getOrDefault("preferred_username", getSubject());
    }

    public String getTenantId() {
        return (String) claims.getOrDefault("tenantId", (String) claims.get("tenant_id"));
    }

    public String getUserType() {
        return (String) claims.getOrDefault("userType", (String) claims.get("user_type"));
    }

    public String getExternalUserId() {
        // Use sub as stable ID
        return getSubject();
    }
}

