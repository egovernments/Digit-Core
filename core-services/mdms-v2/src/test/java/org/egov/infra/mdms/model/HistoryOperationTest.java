package org.egov.infra.mdms.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryOperationTest {

    @Test
    void storedValuesResolveToOperations() {
        assertEquals(HistoryOperation.MIGRATION, HistoryOperation.fromValue("MIGRATION"));
        assertEquals(HistoryOperation.INSERT, HistoryOperation.fromValue("INSERT"));
        assertEquals(HistoryOperation.UPDATE, HistoryOperation.fromValue("UPDATE"));
        assertEquals(HistoryOperation.DELETE, HistoryOperation.fromValue("DELETE"));
        assertEquals(HistoryOperation.TRUNCATE, HistoryOperation.fromValue("TRUNCATE"));
    }

    @Test
    void nullAndUnknownValuesResolveToNull() {
        assertNull(HistoryOperation.fromValue(null));
        assertNull(HistoryOperation.fromValue("X"));
    }
}
