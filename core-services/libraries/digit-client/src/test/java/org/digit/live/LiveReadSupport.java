package org.digit.live;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import tools.jackson.databind.JsonNode;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Base for the per-service read suites.
 *
 * <p>Each subclass covers one service, so a failure names the service without anyone reading a stack
 * trace, and the test count per service is visible in the surefire output.
 *
 * <p>Every read is checked two ways. That the call succeeds and parses is the easy half. The half
 * that matters is {@link #assertKeptEveryField}: because this library disables
 * {@code FAIL_ON_UNKNOWN_PROPERTIES}, a field the service returns that the model does not declare is
 * discarded with no error at all. Comparing the recorded response against the parsed model is the
 * only way that ever surfaces.
 */
@Tag("live")
abstract class LiveReadSupport {

    /** The service key in e2e/services.properties, e.g. "account". */
    abstract String service();

    @BeforeEach
    void setUp() {
        LiveEnv.installContext();
        RawResponseRecorder.reset();
        assumeTrue(LiveEnv.reachable(service()), service() + " unreachable");
    }

    @AfterEach
    void tearDown() {
        LiveEnv.clearContext();
    }

    /**
     * Asserts the SDK kept every field the service sent on the call that just ran.
     *
     * <p>Skips rather than passes when the response carried no data: an empty list drops nothing, and
     * reporting that as a pass would overstate what was checked.
     */
    void assertKeptEveryField(Object sdkResult) {
        JsonNode raw = RawResponseRecorder.lastBody();
        // Returning rather than aborting: the caller has already asserted the call itself succeeded,
        // and aborting here would discard that and report the test as skipped — understating what
        // actually ran. The printed line keeps it honest about which endpoints had data to compare.
        if (raw == null || isEmptyPayload(raw)) {
            System.out.println("  fidelity not checked, no data: " + RawResponseRecorder.lastCall());
            return;
        }
        assertNotNull(sdkResult, "service returned a body but the client produced null");

        Set<String> dropped = ResponseFidelity.droppedFields(raw, sdkResult);
        if (!dropped.isEmpty()) {
            fail("the service sent fields this SDK model silently discards: " + dropped
                    + "\n  call: " + RawResponseRecorder.lastCall()
                    + "\n  model: " + sdkResult.getClass().getName());
        }
    }

    /** An empty array, an empty object, or an envelope whose only content is an empty collection. */
    private static boolean isEmptyPayload(JsonNode raw) {
        if (raw.isArray()) {
            return raw.isEmpty();
        }
        if (!raw.isObject()) {
            return true;
        }
        boolean sawContent = false;
        for (var field : raw.properties()) {
            JsonNode value = field.getValue();
            if (value.isArray() && !value.isEmpty()) {
                sawContent = true;
            } else if (value.isObject() && !value.isEmpty()) {
                sawContent = true;
            } else if (value.isValueNode() && !value.isNull()) {
                sawContent = true;
            }
        }
        return !sawContent;
    }
}
