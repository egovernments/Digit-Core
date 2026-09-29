package com.example.gateway.constants;

public class GatewayConstants {

    public static final String EMPTY_STRING = "";
    public static final String AUTH_TOKEN = "auth-token";
    public static final String ID_TOKEN = "x-id-token";
    public static final String JSON_TYPE = "json";
    public static final String X_WWW_FORM_URLENCODED_TYPE = "application/x-www-form-urlencoded";
    public static final String FORM_DATA = "multipart/form-data";
    public static final String RECEIVED_REQUEST_MESSAGE = "Received request for: {}";
    public static final String AUTH_BOOLEAN_FLAG_NAME = "shouldDoAuth";
    public static final String AUTH_TOKEN_KEY = "authToken";
    public static final String ERROR_MESSAGE_KEY = "error.message";
    public static final String ERROR_CODE_KEY = "error.status_code";
    public static final String CURRENT_REQUEST_TENANTID = "request.tenant_id";
    public static final String CURRENT_REQUEST_SANITIZED_BODY = "request.body.sanitized";
    public static final String CURRENT_REQUEST_SANITIZED_BODY_STR = "request.body.sanitized.str";
    public static final String CURRENT_REQUEST_START_TIME = "request.time.start";
    public static final String CURRENT_REQUEST_END_TIME = "request.time.end";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";

    public static final String FILESTORE_REGEX = "^/filestore/.*";
    public static final String REQUEST_INFO_FIELD_NAME_PASCAL_CASE = "RequestInfo";
    public static final String REQUEST_INFO_FIELD_NAME_CAMEL_CASE = "requestInfo";
    public static final String USER_INFO_FIELD_NAME = "userInfo";
    public static final String USER_INFO_KEY = "USER_INFO";
    public static final String CORRELATION_ID_FIELD_NAME = "correlationId";
    public static final String CORRELATION_ID_HEADER_NAME = "x-correlation-id";
    public static final String CORRELATION_ID_KEY = "CORRELATION_ID";
    public static final String TENANTID_MDC = "TENANTID";
    public static final String RBAC_BOOLEAN_FLAG_NAME = "shouldDoRbac";
    public static final String SKIP_RBAC = "RBAC check skipped";
    public static final String REQUEST_TENANT_ID_KEY = "tenantId";
    public static final String TENANT_ID_KEY = "TENANT_ID";
    public static final String OPEN_ENDPOINT_MESSAGE = "Routing to an open endpoint: {}";

    // Client-facing error texts. Never put exception messages or request input in a gateway error response
    // (reflected XSS). The UI redirects on "internal server error", "some error occured" and "ZuulRuntimeException",
    // so keep those out of these texts - except USER_SERVICE_ERROR_MESSAGE, which keeps the maintenance-page
    // redirect an egov-user 500 triggered before these texts were fixed.
    public static final String INVALID_REQUEST_INFO_MESSAGE = "Invalid RequestInfo in request";
    public static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed";
    public static final String USER_FETCH_FAILURE_CODE = "Exception occurred while fetching user: ";
    public static final String USER_FETCH_FAILURE_MESSAGE = "Error while authenticating the auth token";
    public static final String USER_SERVICE_ERROR_MESSAGE = "Internal Server Error while authenticating the auth token";
    // The UI logs the user out when an error message contains this marker, so expired/invalid tokens must keep it.
    public static final String INVALID_ACCESS_TOKEN_MARKER = "InvalidAccessTokenException";
    public static final String INVALID_ACCESS_TOKEN_MESSAGE = INVALID_ACCESS_TOKEN_MARKER + ": Invalid or expired access token";
    public static final String PRE_HOOK_FAILURE_MESSAGE = "Pre-hook url threw an error";
    public static final String REQUEST_ENRICHMENT_FAILURE_MESSAGE = "Failed to enrich request body";
    public static final String GATEWAY_UNEXPECTED_ERROR_MESSAGE = "Unexpected error at the API gateway";

    public static final String X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    public static final String NOSNIFF = "nosniff";
    public static final String ERROR_RESPONSE_CSP = "default-src 'none'; frame-ancestors 'none'";

}
