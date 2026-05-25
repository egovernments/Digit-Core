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



}
