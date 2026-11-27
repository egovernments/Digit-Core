package org.egov.infra.mdms.service.validator;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.MdmsHistoryCriteria;
import org.egov.infra.mdms.model.SchemaDefHistoryCriteria;
import org.egov.infra.mdms.utils.ErrorUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.egov.infra.mdms.errors.ErrorCodes.*;

@Component
public class HistorySearchValidator {

    private static final int MAX_TENANT_ID_LENGTH = 100;

    private static final int MAX_FILTER_VALUES = 64;

    private ApplicationConfig applicationConfig;

    @Autowired
    public HistorySearchValidator(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void validateMdmsHistorySearchCriteria(MdmsHistoryCriteria criteria) {
        validateCriteriaPresent(criteria);
        Map<String, String> errors = new HashMap<>();

        validateTenantId(criteria.getTenantId(), errors);
        validateSchemaCode(criteria.getSchemaCode(), errors);
        validateFilterSize(criteria.getUniqueIdentifiers(), errors);
        validateFilterSize(criteria.getIds(), errors);
        validatePagination(criteria.getOffset(), criteria.getLimit(), errors);
        validateTimeRange(criteria.getFromTime(), criteria.getToTime(), errors);

        ErrorUtil.throwCustomExceptions(errors);
    }

    public void validateSchemaHistorySearchCriteria(SchemaDefHistoryCriteria criteria) {
        validateCriteriaPresent(criteria);
        Map<String, String> errors = new HashMap<>();

        validateTenantId(criteria.getTenantId(), errors);
        validateCodes(criteria.getCodes(), errors);
        validateFilterSize(criteria.getCodes(), errors);
        validatePagination(criteria.getOffset(), criteria.getLimit(), errors);
        validateTimeRange(criteria.getFromTime(), criteria.getToTime(), errors);

        ErrorUtil.throwCustomExceptions(errors);
    }

    // Bean validation is not active in this service; a null criteria would drop the tenant filter
    private void validateCriteriaPresent(Object criteria) {
        if (Objects.isNull(criteria))
            throw new CustomException(INVALID_HISTORY_CRITERIA, INVALID_HISTORY_CRITERIA_MSG);
    }

    private void validateTenantId(String tenantId, Map<String, String> errors) {
        if (!StringUtils.hasText(tenantId) || tenantId.length() > MAX_TENANT_ID_LENGTH)
            errors.put(INVALID_HISTORY_TENANT, INVALID_HISTORY_TENANT_MSG);
    }

    private void validateSchemaCode(String schemaCode, Map<String, String> errors) {
        if (!StringUtils.hasText(schemaCode))
            errors.put(INVALID_HISTORY_SCHEMA_CODE, INVALID_HISTORY_SCHEMA_CODE_MSG);
    }

    private void validateCodes(Collection<String> codes, Map<String, String> errors) {
        if (CollectionUtils.isEmpty(codes) || codes.stream().anyMatch(code -> !StringUtils.hasText(code)))
            errors.put(INVALID_HISTORY_CODES, INVALID_HISTORY_CODES_MSG);
    }

    private void validateFilterSize(Collection<String> values, Map<String, String> errors) {
        if (!Objects.isNull(values) && values.size() > MAX_FILTER_VALUES)
            errors.put(INVALID_HISTORY_FILTER_SIZE, INVALID_HISTORY_FILTER_SIZE_MSG);
    }

    private void validatePagination(Integer offset, Integer limit, Map<String, String> errors) {
        if (!Objects.isNull(offset) && offset < 0)
            errors.put(INVALID_HISTORY_OFFSET, INVALID_HISTORY_OFFSET_MSG);

        if (!Objects.isNull(limit) && (limit < 1 || limit > applicationConfig.getHistoryMaxLimit()))
            errors.put(INVALID_HISTORY_LIMIT, INVALID_HISTORY_LIMIT_MSG + applicationConfig.getHistoryMaxLimit());
    }

    private void validateTimeRange(Long fromTime, Long toTime, Map<String, String> errors) {
        boolean isNegative = (!Objects.isNull(fromTime) && fromTime < 0) || (!Objects.isNull(toTime) && toTime < 0);
        boolean isInverted = !Objects.isNull(fromTime) && !Objects.isNull(toTime) && fromTime > toTime;

        if (isNegative || isInverted)
            errors.put(INVALID_HISTORY_TIME_RANGE, INVALID_HISTORY_TIME_RANGE_MSG);
    }
}
