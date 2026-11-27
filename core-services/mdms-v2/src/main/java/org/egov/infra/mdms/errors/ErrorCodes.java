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

    public static final String INVALID_HISTORY_CRITERIA_MSG = "History search criteria is mandatory";

    public static final String INVALID_HISTORY_TENANT = "INVALID_HISTORY_TENANT";

    public static final String INVALID_HISTORY_TENANT_MSG = "tenantId is mandatory and must be at most 100 characters";

    public static final String INVALID_HISTORY_SCHEMA_CODE = "INVALID_HISTORY_SCHEMA_CODE";

    public static final String INVALID_HISTORY_SCHEMA_CODE_MSG = "schemaCode is mandatory";

    public static final String INVALID_HISTORY_CODES = "INVALID_HISTORY_CODES";

    public static final String INVALID_HISTORY_CODES_MSG = "codes is mandatory and must not contain blank values";

    public static final String INVALID_HISTORY_FILTER_SIZE = "INVALID_HISTORY_FILTER_SIZE";

    public static final String INVALID_HISTORY_FILTER_SIZE_MSG = "uniqueIdentifiers, ids and codes can each have at most 64 values";

    public static final String INVALID_HISTORY_OFFSET = "INVALID_HISTORY_OFFSET";

    public static final String INVALID_HISTORY_OFFSET_MSG = "Offset must be zero or a positive number";

    public static final String INVALID_HISTORY_LIMIT = "INVALID_HISTORY_LIMIT";

    public static final String INVALID_HISTORY_LIMIT_MSG = "Limit must be between 1 and ";

    public static final String INVALID_HISTORY_TIME_RANGE = "INVALID_HISTORY_TIME_RANGE";

    public static final String INVALID_HISTORY_TIME_RANGE_MSG = "fromTime and toTime must be zero or positive epoch milliseconds and fromTime must not be after toTime";



}
