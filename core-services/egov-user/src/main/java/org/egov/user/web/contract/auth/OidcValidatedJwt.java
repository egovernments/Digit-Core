package org.egov.user.web.contract.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.user.security.oauth2.custom.jwt.JwtConstants;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Exclude;

@Getter
@ToString
public class OidcValidatedJwt {
    private final Set<String> roles;
    private final Map<String, Object> claims;
    private final Date expirationTime;
    private final Date issuanceTime;
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Exclude
    private final String rawToken;
    private final String providerId;

    public OidcValidatedJwt(Set<String> roles, Map<String, Object> claims, Date expirationTime, Date issuanceTime,
            String rawToken, String providerId) {
        this.claims = claims;
        this.roles = roles;
        this.expirationTime = expirationTime;
        this.issuanceTime = issuanceTime;
        this.rawToken = rawToken;
        this.providerId = providerId;
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

    /**
     * Azure/Entra object id (oid). Required for Microsoft Graph API user lookup.
     * Falls back to sub if oid is not present.
     */
    public String getOid() {
        Object oid = claims.get("oid");
        return oid != null ? oid.toString() : getSubject();
    }

    
    /**
     * Token unique identifier for session tracking. Uses jti if present, else uti.
     * Returns null if neither claim is present (caller must validate and throw).
     */
    public String getTokenId() {
        String jti = (String) claims.get(JwtConstants.CLAIM_JTI);
        return jti != null ? jti : (String) claims.get(JwtConstants.CLAIM_UTI);
    }

    public String getProviderId() {
        return providerId;
    }
}
