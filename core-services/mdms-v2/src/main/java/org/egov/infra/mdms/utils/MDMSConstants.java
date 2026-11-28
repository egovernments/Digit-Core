package org.egov.infra.mdms.utils;

import org.springframework.stereotype.Component;


@Component
public class MDMSConstants {

    public static final String X_UNIQUE_KEY = "x-unique";
    public static final String X_REFERENCE_SCHEMA_KEY = "x-ref-schema";
    public static final String REQUIRED_KEY = "required";
    public static final String DOT_SEPARATOR = ".";
    public static final String DOT_REGEX = "\\.";
    public static final String FORWARD_SLASH = "/";
    public static final String DOLLAR_DOT = "$.";
    public static final String FIELD_PATH_KEY = "fieldPath";
    public static final String SCHEMA_CODE_KEY = "schemaCode";
    public static final String CURRENT_SNAPSHOT = "CURRENT_SNAPSHOT";
    public static final int MAX_HISTORY_TENANT_ID_LENGTH = 100;
    public static final String AUDIT_DATA_KEY = "data";
    public static final String AUDIT_IS_ACTIVE_KEY = "isActive";
    public static final String AUDIT_LAST_MODIFIED_BY_KEY = "lastModifiedBy";
    public static final String AUDIT_LAST_MODIFIED_TIME_KEY = "lastModifiedTime";
    public static final String AUDIT_CREATED_BY_KEY = "createdBy";
    public static final String AUDIT_CREATED_TIME_KEY = "createdTime";

}
