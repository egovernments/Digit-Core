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
    void objectPayloadIsNotSplit() {
        // Object-shaped payloads (e.g. {"RequestInfo":...,"Entity":{...}}) may not map per-element -
        // they must keep message-level handling.
        assertNull(RecordSplitter.split("{\"RequestInfo\":{},\"records\":[{\"id\":\"a\"},{\"id\":\"b\"}]}"));
    }

    @Test
    void unparseableOrNullPayloadIsNotSplit() {
        assertNull(RecordSplitter.split("not-json"));
        assertNull(RecordSplitter.split(null));
    }
}
