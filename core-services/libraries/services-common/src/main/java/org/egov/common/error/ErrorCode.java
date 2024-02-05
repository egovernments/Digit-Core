package org.egov.common.error;

public class ErrorCode {
    public static final String IS_CREATE_REQUEST_FLAG_NULL_ERROR_MESSAGE = "IsCreateRequest flag being sent to enrichAuditDetails method must not be null";

    public static final String AUDIT_DETAILS_NULL_ERROR_MESSAGE = "AuditDetails being sent to enrichAuditDetails method must not be null";

    public static final String REQUEST_INFO_NULL_ERROR_MESSAGE = "RequestInfo being sent to enrichAuditDetails method must not be null";

    public static final String USER_INFO_NULL_ERROR_MESSAGE = "UserInfo present inside RequestInfo being sent to enrichAuditDetails method must not be null";

    public static final String USER_UUID_NULL_ERROR_MESSAGE = "User uuid present inside UserInfo being sent to enrichAuditDetails method must not be null";

    public static final String CREATED_TIME_NULL_ERROR_MESSAGE = "CreatedTime present inside AuditDetails being sent to enrichAuditDetails method must not be null in case of update request";

    public static final String CREATED_BY_NULL_ERROR_MESSAGE = "CreatedBy present inside AuditDetails being sent to enrichAuditDetails method must not be null in case of update request";
}
