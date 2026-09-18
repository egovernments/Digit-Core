package org.egov.infra.mdms.service.validator;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_LIMIT_ERROR_CODE;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_LIMIT_ERROR_MESSAGE;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_OFFSET_ERROR_CODE;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_OFFSET_ERROR_MESSAGE;

/**
 * Validates and normalises pagination parameters coming from search requests so that
 * invalid values are rejected with a 400 before they reach the database, and missing
 * values are replaced with the configured defaults.
 */
@Component
@Slf4j
public class PaginationValidator {

    private final ApplicationConfig config;

    @Autowired
    public PaginationValidator(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Validates offset and limit. Null values are allowed and mean "use the default".
     *
     * @param offset requested offset, may be null
     * @param limit  requested limit, may be null
     * @throws org.egov.tracer.model.CustomException if offset is negative or limit is
     *         outside the range 1..mdms.max.limit
     */
    public void validate(Integer offset, Integer limit) {
        Map<String, String> errorMap = new HashMap<>();

        if (offset != null && offset < 0) {
            log.error("Validation failed: offset {} is negative", offset);
            errorMap.put(INVALID_OFFSET_ERROR_CODE, INVALID_OFFSET_ERROR_MESSAGE);
        }

        if (limit != null && (limit < 1 || limit > config.getMaxLimit())) {
            log.error("Validation failed: limit {} is outside 1..{}", limit, config.getMaxLimit());
            errorMap.put(INVALID_LIMIT_ERROR_CODE, INVALID_LIMIT_ERROR_MESSAGE + config.getMaxLimit());
        }

        ErrorUtil.throwCustomExceptions(errorMap);
    }

    /**
     * Returns the offset that will actually be applied to the query.
     */
    public Integer resolveOffset(Integer offset) {
        return offset == null ? config.getDefaultOffset() : offset;
    }

    /**
     * Returns the limit that will actually be applied to the query.
     */
    public Integer resolveLimit(Integer limit) {
        return limit == null ? config.getDefaultLimit() : limit;
    }
}
