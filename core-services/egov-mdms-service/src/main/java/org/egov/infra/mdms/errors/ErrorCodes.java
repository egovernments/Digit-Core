package org.egov.infra.mdms.errors;

import org.springframework.stereotype.Component;

@Component
public class ErrorCodes {

    public static final String DUPLICATE_SCHEMA_CODE = "DUPLICATE_SCHEMA_CODE";

    public static final String DUPLICATE_SCHEMA_CODE_MSG = "Schema code already exists";

    public static final String INVALID_REQUEST_JSON = "INVALID_REQUEST_JSON";

    public static final String INVALID_JSON = "INVALID_JSON";

    public static final String INVALID_JSON_MSG = "Failed to deserialize json";



}
