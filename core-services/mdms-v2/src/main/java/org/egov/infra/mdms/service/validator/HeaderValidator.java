package org.egov.infra.mdms.service.validator;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.egov.infra.mdms.errors.ErrorCodes.TENANT_ID_HEADER;
import static org.egov.infra.mdms.errors.ErrorCodes.CLIENT_ID_HEADER ;
import static org.egov.infra.mdms.errors.ErrorCodes.TENANT_ID_MISSING_ERROR_CODE;
import static org.egov.infra.mdms.errors.ErrorCodes.CLIENT_ID_MISSING_ERROR_CODE ;
import static org.egov.infra.mdms.errors.ErrorCodes.TENANT_ID_MISSING_ERROR_MESSAGE ;
import static org.egov.infra.mdms.errors.ErrorCodes.CLIENT_ID_MISSING_ERROR_MESSAGE ;

/**
 * Validator class for validating HTTP headers such as tenantId and clientId.
 * Provides centralized header validation for all controllers.
 */
@Component
@Slf4j
public class HeaderValidator {

    /**
     * Validates that both tenantId and clientId headers are present and not empty.
     *
     * @param tenantId The tenant ID from request header
     * @param clientId The client ID from request header
     * @throws CustomException if any required header is missing or empty
     */
    public void validateRequiredHeaders(String tenantId, String clientId) {
        Map<String, String> errorMap = new HashMap<>();

        if (!StringUtils.hasText(tenantId)) {
            log.error("Validation failed: {} header is missing or empty", TENANT_ID_HEADER);
            errorMap.put(TENANT_ID_MISSING_ERROR_CODE, TENANT_ID_MISSING_ERROR_MESSAGE);
        }

        if (!StringUtils.hasText(clientId)) {
            log.error("Validation failed: {} header is missing or empty", CLIENT_ID_HEADER);
            errorMap.put(CLIENT_ID_MISSING_ERROR_CODE, CLIENT_ID_MISSING_ERROR_MESSAGE);
        }

        // Throw validation errors
        ErrorUtil.throwCustomExceptions(errorMap);
    }

}
