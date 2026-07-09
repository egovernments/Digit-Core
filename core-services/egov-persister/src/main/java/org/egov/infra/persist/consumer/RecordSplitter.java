package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a multi-record bulk message into standalone single-record messages so a permanent failure
 * can be isolated to the offending record(s) instead of failing every record that happened to share
 * the same Kafka message (R1 at record granularity, not just message granularity).
 *
 * <p>Bulk producers publish a whole validated list as ONE message whose payload is a bare JSON
 * array, and the persister maps such topics with array base paths ({@code $.*}), so a
 * single-element array is processed identically to the full array. Only bare arrays with more than
 * one element are split; object-shaped payloads (whose base paths may not be per-element) keep
 * message-level handling.</p>
 */
final class RecordSplitter {

    /** Used for structural parse/re-emit only, never for domain (de)serialisation. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RecordSplitter() {
    }

    /**
     * @return one single-element-array JSON string per record, or null when the payload is not a
     *         multi-record bare array (not JSON / an object / an array of 0..1 elements) and the
     *         caller must keep message-level handling.
     */
    static List<String> split(String json) {
        if (json == null) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root == null || !root.isArray() || root.size() <= 1) {
                return null;
            }
            List<String> records = new ArrayList<>(root.size());
            for (JsonNode element : root) {
                ArrayNode single = MAPPER.createArrayNode();
                single.add(element);
                records.add(MAPPER.writeValueAsString(single));
            }
            return records;
        } catch (Exception e) {
            return null;
        }
    }
}
