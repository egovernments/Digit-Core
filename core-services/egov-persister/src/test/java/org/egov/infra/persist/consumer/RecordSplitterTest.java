package org.egov.infra.persist.consumer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecordSplitterTest {

    @Test
    void multiRecordArraySplitsIntoSingleElementArraysPreservingContentAndOrder() {
        List<String> records = RecordSplitter.split("[{\"id\":\"a\"},{\"id\":\"b\"},{\"id\":\"c\"}]");

        assertEquals(3, records.size());
        assertEquals("[{\"id\":\"a\"}]", records.get(0));
        assertEquals("[{\"id\":\"b\"}]", records.get(1));
        assertEquals("[{\"id\":\"c\"}]", records.get(2));
    }

    @Test
    void singleElementArrayIsNotSplit() {
        assertNull(RecordSplitter.split("[{\"id\":\"a\"}]"));
    }

    @Test
    void emptyArrayIsNotSplit() {
        assertNull(RecordSplitter.split("[]"));
    }

    @Test
    void objectWithSingleRecordArraySplitsPreservingSiblingKeys() {
        // {"RequestInfo":{...},"Boundary":[a,b]} -> one message per element, RequestInfo preserved.
        List<String> records =
                RecordSplitter.split("{\"RequestInfo\":{\"ver\":\"1.0\"},\"Boundary\":[{\"id\":\"a\"},{\"id\":\"b\"}]}");

        assertEquals(2, records.size());
        assertEquals("{\"RequestInfo\":{\"ver\":\"1.0\"},\"Boundary\":[{\"id\":\"a\"}]}", records.get(0));
        assertEquals("{\"RequestInfo\":{\"ver\":\"1.0\"},\"Boundary\":[{\"id\":\"b\"}]}", records.get(1));
    }

    @Test
    void objectWithSingleElementArrayIsNotSplit() {
        assertNull(RecordSplitter.split("{\"Boundary\":[{\"id\":\"a\"}]}"));
    }

    @Test
    void objectWithMultipleArrayFieldsIsNotSplit() {
        // Ambiguous which array is the record list -> keep message-level handling.
        assertNull(RecordSplitter.split("{\"Boundary\":[{\"id\":\"a\"},{\"id\":\"b\"}],\"extra\":[{\"x\":1}]}"));
    }

    @Test
    void objectWithScalarArrayIsNotSplit() {
        // A scalar list is a list-valued COLUMN of one whole-message record (e.g. privacy-audit
        // enc-user-audit-info entityIds, base path $), not a record list - splitting would truncate
        // the record's column and duplicate its key. Must keep message-level handling.
        assertNull(RecordSplitter.split(
                "{\"id\":\"u1\",\"userId\":\"9\",\"entityIds\":[\"e1\",\"e2\",\"e3\"],\"purpose\":{\"code\":\"kyc\"}}"));
    }

    @Test
    void objectWithMixedElementArrayIsNotSplit() {
        // Every element must be a JSON object for the array to count as a record list.
        assertNull(RecordSplitter.split("{\"records\":[{\"id\":\"a\"},\"stray\",{\"id\":\"b\"}]}"));
    }

    @Test
    void objectWithNoTopLevelArrayIsNotSplit() {
        // Array nested deeper (e.g. $.bill.billDetails.*) is not reached -> message-level handling.
        assertNull(RecordSplitter.split("{\"bill\":{\"billDetails\":[{\"id\":\"a\"},{\"id\":\"b\"}]}}"));
    }

    @Test
    void unparseableOrNullPayloadIsNotSplit() {
        assertNull(RecordSplitter.split("not-json"));
        assertNull(RecordSplitter.split(null));
    }
}
