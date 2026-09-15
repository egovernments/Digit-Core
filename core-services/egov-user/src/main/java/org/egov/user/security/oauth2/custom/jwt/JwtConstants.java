package org.egov.user.security.oauth2.custom.jwt;

/**
 * Constants for JWT: grant type, request param keys, claim keys, MDMS keys, OIDC error codes,
 * OAuth2Error code, username format placeholders.
 */
public final class JwtConstants {

    private JwtConstants() {}

    public static final String GRANT_TYPE_JWT_EXCHANGE = "jwt_exchange";

    /** Message when AccessTokenMfaExtractor is missing (required for JWT exchange). */
    public static final String MFA_EXTRACTOR_REQUIRED_MESSAGE =
            "AccessTokenMfaExtractor is required for JWT exchange authentication";

    /** Request parameter keys. */
    public static final String PARAM_ASSERTION = "assertion";
    public static final String PARAM_TENANT_ID = "tenantId";

    /** JWT claim keys. */
    public static final String CLAIM_USER_TYPE = "userType";
    public static final String CLAIM_USER_TYPE_ALT = "user_type";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_JTI = "jti";
    public static final String CLAIM_UTI = "uti";

    /** MDMS / role and designation mapping keys. */
    public static final String KEY_SSO_ROLE = "ssoRole";
    public static final String KEY_DIGIT_ROLES = "digitRoles";
    public static final String KEY_DIGIT_ROLE = "digitRole";
    public static final String KEY_IDP_DESIGNATION = "idpDesignation";
    public static final String KEY_DIGIT_DESIGNATION_CODE = "digitDesignationCode";

    /** OAuth2Error error code. */
    public static final String OAUTH2_ERROR_INVALID_TOKEN = "invalid_token";

    /** Error message when IDP token has neither jti nor uti claim. */
    public static final String ERROR_MISSING_TOKEN_ID = "IDP token must contain jti or uti claim";

}
