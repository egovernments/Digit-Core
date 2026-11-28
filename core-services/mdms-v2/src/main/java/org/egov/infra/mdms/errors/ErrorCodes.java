package org.egov.infra.mdms.errors;

import org.springframework.stereotype.Component;

@Component
public class ErrorCodes {

    public static final String DUPLICATE_SCHEMA_CODE = "DUPLICATE_SCHEMA_CODE";

    public static final String DUPLICATE_SCHEMA_CODE_MSG = "Schema code already exists";

    public static final String INVALID_REQUEST_JSON = "INVALID_REQUEST_JSON";

    public static final String INVALID_JSON = "INVALID_JSON";

    public static final String INVALID_JSON_MSG = "Failed to deserialize json";

    public static final String REQUIRED_ATTRIBUTE_LIST_ERR_CODE = "REQUIRED_ATTRIBUTE_LIST_ERR";

    public static final String REQUIRED_ATTRIBUTE_LIST_EMPTY_MSG = "Required attribute list cannot be empty";

    public static final String UNIQUE_ATTRIBUTE_LIST_ERR_CODE = "UNIQUE_ATTRIBUTE_LIST_ERR";

    public static final String UNIQUE_ATTRIBUTE_LIST_EMPTY_MSG = "Unique attribute list cannot be empty";

    public static final String UNIQUE_ATTRIBUTE_LIST_INVALID_MSG = "Fields provided under unique fields must be a subset of required attributes list";

    public static final String INVALID_HISTORY_CRITERIA = "INVALID_HISTORY_CRITERIA";

    public static final String INVALID_HISTORY_CRITERIA_MSG = "HistoryCriteria is mandatory";

    public static final String INVALID_HISTORY_TENANT = "INVALID_HISTORY_TENANT";

    public static final String INVALID_HISTORY_TENANT_MSG = "tenantId is mandatory and must be at most 100 characters";

    public static final String INVALID_HISTORY_SCHEMA_CODE = "INVALID_HISTORY_SCHEMA_CODE";

    public static final String INVALID_HISTORY_SCHEMA_CODE_MSG = "schemaCode is mandatory";

    public static final String INVALID_HISTORY_UNIQUE_IDENTIFIERS = "INVALID_HISTORY_UNIQUE_IDENTIFIERS";

    public static final String INVALID_HISTORY_UNIQUE_IDENTIFIERS_MSG = "uniqueIdentifiers is mandatory: at least one value, no blank values and not more than the allowed maximum";

    public static final String INVALID_HISTORY_OFFSET = "INVALID_HISTORY_OFFSET";

    public static final String INVALID_HISTORY_OFFSET_MSG = "offset must be zero or a positive number";

    public static final String INVALID_HISTORY_LIMIT = "INVALID_HISTORY_LIMIT";

    public static final String INVALID_HISTORY_LIMIT_MSG = "limit is outside the allowed range";

    public static final String INVALID_HISTORY_MAX_VERSIONS = "INVALID_HISTORY_MAX_VERSIONS";

    public static final String INVALID_HISTORY_MAX_VERSIONS_MSG = "maxVersionsPerRecord is outside the allowed range";

    public static final String HISTORY_AUDIT_SERVICE_ERR = "HISTORY_AUDIT_SERVICE_ERR";

    public static final String HISTORY_AUDIT_SERVICE_ERR_MSG = "Failed to fetch master data history from audit service";

    public static final String SCHEMA_CODE_MISMATCH_ERR = "SCHEMA_CODE_MISMATCH_ERR";

    public static final String SCHEMA_CODE_CHANGE_MSG = "schemaCode of an existing master data record cannot be changed";

    public static final String SCHEMA_CODE_PATH_MISMATCH_MSG = "schemaCode in the path must match schemaCode in the request body";



}
