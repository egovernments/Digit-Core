package org.digit.live;

import org.digit.util.DigitJson;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Compares what a service actually sent against what the SDK's model kept.
 *
 * <p>This exists because the library disables {@code FAIL_ON_UNKNOWN_PROPERTIES} on purpose: a field
 * the service adds must never break a consumer. The cost of that choice is that a field the model
 * does not declare is dropped in complete silence — no error, no log, just missing data. Every gap in
 * the esMagico report was some version of this. A green endpoint test proves the call succeeded; only
 * this comparison proves the SDK kept what the call returned.
 *
 * <p>Both sides come from a single request: {@link RawResponseRecorder} captures the bytes while the
 * message converter builds the model. Nothing is fetched twice and no URL is reconstructed by hand,
 * so the comparison cannot drift onto a different endpoint than the one under test.
 */
final class ResponseFidelity {

    /** Envelope keys a client deliberately unwraps rather than models. Not drops. */
    private static final Set<String> ENVELOPE = Set.of(
            "ResponseMetadata", "responseMetadata", "ResponseInfo", "responseInfo",
            "RequestInfo", "requestInfo", "pagination", "Pagination",
            "totalCount", "total", "count", "page", "size", "offset", "limit", "hasMore",
            // Registry wraps payloads as {"success":true,"data":...}. Only the flag is listed here,
            // not "data": once the flag is discounted, "data" is the single container and the
            // unwrapper finds it. Listing "data" outright would excuse a model that genuinely
            // dropped a field of that name, and several models really do declare one.
            "success");

    private ResponseFidelity() {
    }

    /**
     * Field paths present in the service's response but absent from what the SDK parsed.
     *
     * <p>Paths are compared by leaf name within their container rather than by exact position,
     * because a client legitimately unwraps an envelope — the same record can sit at
     * {@code data[0].id} on the wire and at {@code [0].id} in the model.
     */
    static Set<String> droppedFields(JsonNode serviceResponse, Object sdkResult) {
        JsonNode payload = unwrapEnvelope(serviceResponse);
        Set<String> onTheWire = leafNames(payload);
        Set<String> keptByModel = leafNames(DigitJson.mapper().valueToTree(sdkResult));

        Set<String> dropped = new TreeSet<>(onTheWire);
        dropped.removeAll(keptByModel);
        dropped.removeAll(ENVELOPE);

        // A field the service sent as null tells us nothing: the model may well declare it, and
        // NON_NULL means it would not appear on the model side either.
        dropped.removeIf(name -> isAlwaysNull(payload, name));
        return dropped;
    }

    /**
     * Strips a pure envelope so the comparison starts at the payload the client returns.
     *
     * <p>"Pure" is the load-bearing word. An object whose only substantive key is one container —
     * {@code {"tenants":[...],"hasMore":false}} or {@code {"definitions":[...]}} — is a wrapper the
     * client unwraps by design, so its key is not a dropped field. An object that carries real
     * fields *alongside* a container, such as a demand with its line items, is not unwrapped: that
     * container is part of the record, and a model missing it is a genuine drop this must still
     * catch. Anything else is returned untouched.
     */
    private static JsonNode unwrapEnvelope(JsonNode serviceResponse) {
        if (serviceResponse == null || !serviceResponse.isObject()) {
            return serviceResponse;
        }
        JsonNode container = null;
        for (var field : serviceResponse.properties()) {
            if (ENVELOPE.contains(field.getKey()) || field.getValue().isNull()) {
                continue;
            }
            boolean isContainer = field.getValue().isObject() || field.getValue().isArray();
            if (!isContainer || container != null) {
                // A scalar of its own, or a second container: not a pure envelope.
                return serviceResponse;
            }
            container = field.getValue();
        }
        return container == null ? serviceResponse : container;
    }

    /**
     * Every field name appearing anywhere in the tree.
     *
     * <p>Deliberately flattened rather than path-qualified: array indices and unwrapped envelopes
     * make full paths differ for responses that are in fact equivalent, which would bury the real
     * signal — a name the service used and the model has no home for — under false positives.
     */
    private static Set<String> leafNames(JsonNode node) {
        Set<String> names = new LinkedHashSet<>();
        collect(node, names);
        return names;
    }

    private static void collect(JsonNode node, Set<String> names) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            for (var field : node.properties()) {
                names.add(field.getKey());
                collect(field.getValue(), names);
            }
        } else if (node.isArray()) {
            node.forEach(element -> collect(element, names));
        }
    }

    /** True when every occurrence of {@code name} in the response is null. */
    private static boolean isAlwaysNull(JsonNode node, String name) {
        return allOccurrencesNull(node, name, true);
    }

    private static boolean allOccurrencesNull(JsonNode node, String name, boolean soFar) {
        if (node == null || !soFar) {
            return soFar;
        }
        if (node.isObject()) {
            for (var field : node.properties()) {
                if (field.getKey().equals(name) && !field.getValue().isNull()) {
                    return false;
                }
                soFar = allOccurrencesNull(field.getValue(), name, soFar);
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                soFar = allOccurrencesNull(element, name, soFar);
            }
        }
        return soFar;
    }
}
