package org.egov.infra.persist.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Splits a multi-record bulk message into standalone single-record messages so a permanent failure
 * can be isolated to the offending record(s) instead of failing every record that happened to share
 * the same Kafka message (R1 at record granularity, not just message granularity).
 *
 * <p>Two payload shapes are split:</p>
 * <ul>
 *   <li><b>bare array</b> — {@code [ {..}, {..} ]} (base path {@code $.*}): re-emitted as one
 *       single-element array per record.</li>
 *   <li><b>object with one record array</b> — {@code { "Boundary": [ {..}, {..} ] }} (base path
 *       {@code $.Boundary.*}): the message is deep-copied and only that array is replaced with a
 *       single element, so sibling keys (e.g. {@code RequestInfo}) are preserved on the re-persist.</li>
 * </ul>
 *
 * <p>The object case only fires when EXACTLY ONE top-level field is a multi-element array AND every
 * element of that array is a JSON object. The single-array rule makes the record list unambiguous;
 * the objects-only rule keeps the splitter off list-valued COLUMNS of a single whole-message record
 * (e.g. {@code {"entityIds":["e1","e2"],...}} persisted with base path {@code $}), whose truncation
 * would corrupt the record instead of isolating it. Anything else — not JSON, a single element, zero
 * or several top-level array fields, non-object elements, or the array nested deeper — is left
 * unsplit and the caller keeps message-level handling. Residual blind spot: an object-element array
 * that is itself a column of a whole-message-mapped record is structurally indistinguishable from a
 * record list; no current persister mapping has that shape (a mapping-driven splitter is required if
 * one ever does).</p>
 */
final class RecordSplitter {

    /** Used for structural parse/re-emit only, never for domain (de)serialisation. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RecordSplitter() {
    }

    /**
     * @return one single-record JSON string per element, or null when the payload is not a
     *         multi-record array (bare or under a single object key) and the caller must keep
     *         message-level handling.
     */
    static List<String> split(String json) {
        if (json == null) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root == null) {
                return null;
            }
            if (root.isArray()) {
                return root.size() <= 1 ? null : splitArray(root, null, null);
            }
            if (root.isObject()) {
                String field = singleMultiElementArrayField((ObjectNode) root);
                if (field != null) {
                    return splitArray((ArrayNode) root.get(field), (ObjectNode) root, field);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Emit one message per element. When {@code wrapper} is null the record is a bare single-element
     * array; otherwise the wrapper object is deep-copied and its {@code field} set to a single-element
     * array, preserving every sibling key.
     */
    private static List<String> splitArray(JsonNode array, ObjectNode wrapper, String field) throws Exception {
        List<String> records = new ArrayList<>(array.size());
        for (JsonNode element : array) {
            ArrayNode single = MAPPER.createArrayNode().add(element);
            if (wrapper == null) {
                records.add(MAPPER.writeValueAsString(single));
            } else {
                ObjectNode clone = wrapper.deepCopy();
                clone.set(field, single);
                records.add(MAPPER.writeValueAsString(clone));
            }
        }
        return records;
    }

    /**
     * The single top-level field holding a multi-element array whose elements are all JSON objects,
     * or null when no field is unambiguously the record list (zero or several top-level array fields,
     * an array of 0..1 elements, or any non-object element — a scalar list is a column of one record,
     * not a record list).
     */
    private static String singleMultiElementArrayField(ObjectNode root) {
        String found = null;
        for (Map.Entry<String, JsonNode> field : root.properties()) {
            if (field.getValue().isArray()) {
                if (found != null) {
                    return null; // more than one array field -> ambiguous, do not split
                }
                found = field.getKey();
            }
        }
        if (found == null || root.get(found).size() <= 1) {
            return null;
        }
        for (JsonNode element : root.get(found)) {
            if (!element.isObject()) {
                return null; // scalar/mixed elements -> a list-valued column, not a record list
            }
        }
        return found;
    }
}
