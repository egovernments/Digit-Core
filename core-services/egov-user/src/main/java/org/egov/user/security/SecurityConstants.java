package org.egov.user.security;

import java.util.regex.Pattern;

/**
 * Constants for security: request param keys, detail map keys, map keys used in put/get, headers.
 * Does not include log or exception message strings.
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    /** Microsoft Graph: form/JSON keys and headers. */
    public static final String KEY_GRANT_TYPE = "grant_type";
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CLIENT_SECRET = "client_secret";
    public static final String KEY_SCOPE = "scope";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    /** OAuth2 token response: lifetime in seconds. */
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_VALUE = "value";
    /** Microsoft Graph appRoleAssignments response: resourceId of the app. */
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_ODATA_TYPE = "@odata.type";
    public static final String KEY_DISPLAY_NAME = "displayName";
    public static final String KEY_CREATION_DATE_TIME = "creationDateTime";
    public static final String KEY_CREATED_DATE_TIME = "createdDateTime";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    /** Microsoft Graph user resource: employee creation fields. */
    public static final String KEY_DEPARTMENT = "department";
    public static final String KEY_JOB_TITLE = "jobTitle";
    public static final String KEY_EMPLOYEE_TYPE = "employeeType";
    public static final String KEY_DESIGNATION = "designation";
    /** Query suffix for GET user to fetch employee creation fields. */
    public static final String GRAPH_USERS_SELECT_QUERY = "?$select=department,jobTitle,employeeType";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    public static final String DEFAULT_GRAPH_SCOPE = "https://graph.microsoft.com/.default";

    /** Redis key prefix for Graph access token cache. Key format: graph:token:providerId:tenantId */
    public static final String GRAPH_TOKEN_REDIS_KEY_PREFIX = "graph:token:";

    /** Encryption key and purpose for Graph token in Redis (egov-enc-service + MDMS DataSecurity). */
    public static final String GRAPH_TOKEN_ENCRYPTION_KEY = "GraphToken";
    public static final String GRAPH_TOKEN_DECRYPT_PURPOSE = "SystemDecrypt";

    /** Thrown when Graph token encryption returns plaintext (e.g. missing MDMS GraphToken SecurityPolicy). */
    public static final String GRAPH_TOKEN_ENCRYPTION_NOOP_CODE = "GRAPH_TOKEN_ENCRYPTION_NOOP";
    public static final String GRAPH_TOKEN_ENCRYPTION_NOOP_MESSAGE =
            "Graph token encryption did not change value. Ensure MDMS DataSecurity SecurityPolicy for model 'GraphToken' is configured (see MDMS_GRAPH_TOKEN_ENCRYPTION_POLICY.md).";

    /** Validation patterns for Graph URL path parameters derived from JWT claims. */
    public static final Pattern GRAPH_OID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static final Pattern GRAPH_EMAIL_PATTERN =
            Pattern.compile("^[^@\\s/\\\\?#]+@[^@\\s/\\\\?#.]+\\.[^@\\s/\\\\?#]{2,}$");
}
