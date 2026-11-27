package org.egov.infra.mdms.service.validator;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.MdmsHistoryCriteria;
import org.egov.infra.mdms.model.SchemaDefHistoryCriteria;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.egov.infra.mdms.errors.ErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;

class HistorySearchValidatorTest {

    private HistorySearchValidator validator;

    @BeforeEach
    void setUp() {
        ApplicationConfig config = new ApplicationConfig();
        config.setHistoryMaxLimit(100);
        validator = new HistorySearchValidator(config);
    }

    @Test
    void validCriteriaPasses() {
        assertDoesNotThrow(() -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").offset(0).limit(100).fromTime(0L).toTime(0L).build()));
        assertDoesNotThrow(() -> validator.validateSchemaHistorySearchCriteria(
                SchemaDefHistoryCriteria.builder().tenantId("pg").codes(java.util.List.of("common.Dept")).build()));
    }

    @Test
    void negativeOffsetIsRejected() {
        CustomException e = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").offset(-1).build()));
        assertTrue(e.getErrors().containsKey(INVALID_HISTORY_OFFSET));
    }

    @Test
    void limitOutsideRangeIsRejected() {
        for (int limit : new int[]{0, -5, 101}) {
            CustomException e = assertThrows(CustomException.class, () -> validator.validateSchemaHistorySearchCriteria(
                    SchemaDefHistoryCriteria.builder().tenantId("pg").codes(java.util.List.of("common.Dept")).limit(limit).build()));
            assertTrue(e.getErrors().containsKey(INVALID_HISTORY_LIMIT));
        }
    }

    @Test
    void invalidTimeRangeIsRejected() {
        CustomException inverted = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").fromTime(10L).toTime(5L).build()));
        assertTrue(inverted.getErrors().containsKey(INVALID_HISTORY_TIME_RANGE));

        CustomException negative = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").fromTime(-1L).build()));
        assertTrue(negative.getErrors().containsKey(INVALID_HISTORY_TIME_RANGE));
    }

    @Test
    void allErrorsAreReportedTogether() {
        CustomException e = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").offset(-1).limit(0).fromTime(5L).toTime(1L).build()));
        assertEquals(3, e.getErrors().size());
    }

    @Test
    void missingCriteriaIsRejected() {
        CustomException mdms = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(null));
        assertEquals(INVALID_HISTORY_CRITERIA, mdms.getCode());

        CustomException schema = assertThrows(CustomException.class, () -> validator.validateSchemaHistorySearchCriteria(null));
        assertEquals(INVALID_HISTORY_CRITERIA, schema.getCode());
    }

    @Test
    void missingBlankOrLongTenantIsRejected() {
        for (String tenantId : new String[]{null, "", "   ", "x".repeat(101)}) {
            CustomException mdms = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                    MdmsHistoryCriteria.builder().tenantId(tenantId).build()));
            assertTrue(mdms.getErrors().containsKey(INVALID_HISTORY_TENANT));

            CustomException schema = assertThrows(CustomException.class, () -> validator.validateSchemaHistorySearchCriteria(
                    SchemaDefHistoryCriteria.builder().tenantId(tenantId).build()));
            assertTrue(schema.getErrors().containsKey(INVALID_HISTORY_TENANT));
        }
    }

    @Test
    void missingOrBlankSchemaCodeIsRejected() {
        for (String schemaCode : new String[]{null, "", "   "}) {
            CustomException e = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                    MdmsHistoryCriteria.builder().tenantId("pg").schemaCode(schemaCode).build()));
            assertTrue(e.getErrors().containsKey(INVALID_HISTORY_SCHEMA_CODE));
        }
    }

    @Test
    void missingEmptyOrBlankCodesAreRejected() {
        for (java.util.List<String> codes : java.util.Arrays.asList(null, java.util.List.<String>of(), java.util.List.of("common.Dept", " "))) {
            CustomException e = assertThrows(CustomException.class, () -> validator.validateSchemaHistorySearchCriteria(
                    SchemaDefHistoryCriteria.builder().tenantId("pg").codes(codes).build()));
            assertTrue(e.getErrors().containsKey(INVALID_HISTORY_CODES));
        }
    }

    @Test
    void moreThan64FilterValuesAreRejected() {
        java.util.Set<String> tooMany = java.util.stream.IntStream.range(0, 65).mapToObj(i -> "v" + i)
                .collect(java.util.stream.Collectors.toSet());

        CustomException uniqueIdentifiers = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").uniqueIdentifiers(tooMany).build()));
        assertTrue(uniqueIdentifiers.getErrors().containsKey(INVALID_HISTORY_FILTER_SIZE));

        CustomException ids = assertThrows(CustomException.class, () -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").ids(tooMany).build()));
        assertTrue(ids.getErrors().containsKey(INVALID_HISTORY_FILTER_SIZE));

        CustomException codes = assertThrows(CustomException.class, () -> validator.validateSchemaHistorySearchCriteria(
                SchemaDefHistoryCriteria.builder().tenantId("pg").codes(new java.util.ArrayList<>(tooMany)).build()));
        assertTrue(codes.getErrors().containsKey(INVALID_HISTORY_FILTER_SIZE));

        java.util.Set<String> max = java.util.stream.IntStream.range(0, 64).mapToObj(i -> "v" + i)
                .collect(java.util.stream.Collectors.toSet());
        assertDoesNotThrow(() -> validator.validateMdmsHistorySearchCriteria(
                MdmsHistoryCriteria.builder().tenantId("pg").schemaCode("common.Dept").uniqueIdentifiers(max).ids(max).build()));
    }
}
