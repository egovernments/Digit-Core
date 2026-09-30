package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class JsonDocumentInspectorTest {
    private static final InspectionLimits GENEROUS =
            new InspectionLimits(100_000, 64, 10_000, 256, 10_000, 1000, 4096);

    private final JsonDocumentInspector inspector = new JsonDocumentInspector(
            new ContentDetector(ContentPolicy.builder().build()));

    @Test
    void inspectsUnknownPropertiesNestedObjectsAndArrays() {
        List<Violation> violations = inspect(
                "{\"known\":\"ok\",\"unknown\":[{\"a\":{\"b\":{\"c\":{\"d\":\"<script\"}}}}]}",
                noSkips());

        assertEquals(1, violations.size());
        assertEquals("R1", violations.get(0).getRuleId());
        assertEquals("/unknown/0/a/b/c/d", violations.get(0).getLocation());
    }

    @Test
    void inspectsPropertyNamesAndRedactsUnsafeNameFromLocation() {
        List<Violation> violations = inspect("{\"onload=\":true}", noSkips());

        assertEquals(1, violations.size());
        assertEquals("R3", violations.get(0).getRuleId());
        assertEquals("/*", violations.get(0).getLocation());
    }

    @Test
    void locationsStayCorrectAcrossSiblingContainersAndArrays() {
        List<Violation> violations = inspect(
                "{\"a\":[{\"x\":1},{\"y\":\"<b>\"},[\"ok\",\"<i>\"]],\"b\":{\"c\":\"<u>\"},"
                        + "\"d\":[{\"<k>\":1}],\"e\":\"<s>\"}", noSkips());

        assertEquals(Arrays.asList("/a/1/y", "/a/2/1", "/b/c", "/d/0/*", "/e"), locations(violations));
        assertEquals(Arrays.asList("/1/0"), locations(inspect("[\"ok\",[\"<b>\"]]", noSkips())));
        assertEquals(Arrays.asList("/"), locations(inspect("\"<b>\"", noSkips())));
    }

    @Test
    void failureLocationsMatchRelease100() {
        InspectionLimits depth3 = new InspectionLimits(1000, 3, 100, 100, 1000, 100, 100);
        InspectionLimits tokens4 = new InspectionLimits(1000, 64, 100, 100, 4, 100, 100);
        // Expected values were produced by the verified 1.0.0 jar.
        assertFailure("{\"a\":\"x\" \"b\":1}", GENEROUS, "REQUEST_JSON_MALFORMED|json|/a");
        assertFailure("{\"Mdms\":{\"data\":[1,2", GENEROUS, "REQUEST_JSON_MALFORMED|json|/Mdms/data/1");
        assertFailure("{\"x\":{\"y\":{\"z\":{}}}}", depth3, "REQUEST_LIMIT_EXCEEDED|parser-limit|/x/y/z");
        assertFailure("{\"a\":1,\"b\":2}", tokens4, "REQUEST_LIMIT_EXCEEDED|max-tokens|/b");
        assertFailure("{\"a\":{\"b\":1}} x", GENEROUS, "REQUEST_JSON_MALFORMED|json|/");
        assertFailure("{\"a\":[1,{\"b\":2}] ,", GENEROUS, "REQUEST_JSON_MALFORMED|json|/a");
        assertFailure("[1,[2,3]", GENEROUS, "REQUEST_JSON_MALFORMED|json|/1");
        assertFailure("{\"k\":tru}", GENEROUS, "REQUEST_JSON_MALFORMED|json|/");
        assertFailure("{\"a\":{},}", GENEROUS, "REQUEST_JSON_MALFORMED|json|/a");
        assertFailure("{\"a\":[\"s\" 1]}", GENEROUS, "REQUEST_JSON_MALFORMED|json|/a/0");
    }

    private void assertFailure(String json, InspectionLimits limits, String expected) {
        InspectionException failure = assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8(json), limits, noSkips(), true, true, ignoreViolations()));
        Violation violation = failure.getViolation();
        assertEquals(expected, violation.getCode() + "|" + violation.getRuleId() + "|" + violation.getLocation(), json);
    }

    @Test
    void failureLocationsNameTheOffendingProperty() {
        InspectionException duplicate = assertThrows(InspectionException.class,
                () -> inspect("{\"a\":[{\"x\":1,\"x\":2}]}", noSkips()));
        assertEquals("/a/0/x", duplicate.getViolation().getLocation());
    }

    @Test
    void skipPathSuppressesOnlyItsMatchedSubtree() {
        List<Violation> violations = inspect(
                "{\"items\":[{\"note\":\"<b>\"},{\"note\":\"<i>\"}],\"other\":\"<u>\"}",
                new SkipPathMatcher(Collections.singletonList("/items/0")));

        assertEquals(2, violations.size());
        assertEquals("/items/1/note", violations.get(0).getLocation());
        assertEquals("/other", violations.get(1).getLocation());
    }

    @Test
    void duplicateChecksRemainActiveInsideSkippedSubtree() {
        InspectionException exception = assertThrows(InspectionException.class,
                () -> inspect("{\"safe\":{\"x\":1,\"x\":2}}",
                        new SkipPathMatcher(Collections.singletonList("/safe"))));

        assertEquals(ViolationCode.REQUEST_JSON_DUPLICATE_KEY,
                exception.getViolation().getCode());
        assertEquals("/safe/x", exception.getViolation().getLocation());
    }

    @Test
    void contentConsumerMayEnforceImmediately() {
        InspectionException exception = assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8("{\"value\":\"<script\"}"), GENEROUS, noSkips(),
                        true, true, violation -> { throw new InspectionException(violation); }));

        assertEquals(ViolationCode.REQUEST_CONTENT_NOT_ALLOWED,
                exception.getViolation().getCode());
        assertEquals("R1", exception.getViolation().getRuleId());
    }

    @Test
    void acceptsLimitsExactlyAndRejectsLimitPlusOne() {
        byte[] accepted = utf8("{\"abc\":\"xyz\",\"n\":123}");
        InspectionLimits exact = new InspectionLimits(
                accepted.length, 1, 3, 3, 6, 3, 1);
        inspector.inspect(accepted, exact, noSkips(), true, true, ignoreViolations());

        assertLimit("{\"a\":\"four\"}",
                new InspectionLimits(100, 2, 3, 10, 10, 10, 1));
        assertLimit("{\"four\":1}",
                new InspectionLimits(100, 2, 10, 3, 10, 10, 1));
        assertLimit("{\"a\":1234}",
                new InspectionLimits(100, 2, 10, 10, 10, 3, 1));
        assertLimit("{\"a\":{}}",
                new InspectionLimits(100, 1, 10, 10, 10, 10, 1));
        assertLimit("[1]",
                new InspectionLimits(100, 2, 10, 10, 2, 10, 1));
        assertLimit("{}",
                new InspectionLimits(1, 2, 10, 10, 10, 10, 1));
    }

    @Test
    void rejectsDuplicateKeysDualRequestInfoAndTrailingRoot() {
        InspectionException duplicate = structural("{\"x\":1,\"x\":2}");
        assertEquals(ViolationCode.REQUEST_JSON_DUPLICATE_KEY,
                duplicate.getViolation().getCode());

        InspectionException dual = structural(
                "{\"RequestInfo\":{},\"requestInfo\":{}}");
        assertEquals(ViolationCode.REQUEST_JSON_MALFORMED, dual.getViolation().getCode());
        assertEquals("dual-request-info", dual.getViolation().getRuleId());

        InspectionException trailing = structural("{} []");
        assertEquals(ViolationCode.REQUEST_JSON_MALFORMED,
                trailing.getViolation().getCode());
        assertEquals("trailing-content", trailing.getViolation().getRuleId());
    }

    @Test
    void duplicateAndDualChecksCanBeConfiguredIndependently() {
        inspector.inspect(utf8("{\"RequestInfo\":{},\"requestInfo\":{},\"x\":1,\"x\":2}"),
                GENEROUS, noSkips(), false, false, ignoreViolations());
    }

    @Test
    void rejectsEmptyMalformedInvalidUtf8AndUtf16BytesWithoutRawCause() {
        List<byte[]> malformed = Arrays.asList(
                new byte[0],
                utf8("{\"unterminated\":"),
                new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xc3, '"', '}'},
                "{\"x\":1}".getBytes(StandardCharsets.UTF_16LE));

        for (byte[] body : malformed) {
            InspectionException exception = assertThrows(InspectionException.class,
                    () -> inspector.inspect(body, GENEROUS, noSkips(), true, true,
                            ignoreViolations()));
            assertEquals(ViolationCode.REQUEST_JSON_MALFORMED,
                    exception.getViolation().getCode());
            assertEquals(ViolationCode.REQUEST_JSON_MALFORMED.getMessage(),
                    exception.getMessage());
            assertEquals(null, exception.getCause());
        }
    }

    @Test
    void acceptsUtf8BomAndMultilingualStrings() {
        byte[] json = utf8("{\"name\":\"বাংলা\",\"note\":\"🙂\"}");
        byte[] withBom = new byte[json.length + 3];
        withBom[0] = (byte) 0xef;
        withBom[1] = (byte) 0xbb;
        withBom[2] = (byte) 0xbf;
        System.arraycopy(json, 0, withBom, 3, json.length);

        List<Violation> violations = new ArrayList<Violation>();
        inspector.inspect(withBom, GENEROUS, noSkips(), true, true, violations::add);
        assertTrue(violations.isEmpty());
    }

    private List<Violation> inspect(String json, SkipPathMatcher skips) {
        List<Violation> violations = new ArrayList<Violation>();
        inspector.inspect(utf8(json), GENEROUS, skips, true, true, violations::add);
        return violations;
    }

    private InspectionException structural(String json) {
        return assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8(json), GENEROUS, noSkips(), true, true,
                        ignoreViolations()));
    }

    private void assertLimit(String json, InspectionLimits limits) {
        InspectionException exception = assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8(json), limits, noSkips(), true, true,
                        ignoreViolations()));
        assertEquals(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                exception.getViolation().getCode());
    }

    private static List<String> locations(List<Violation> violations) {
        List<String> result = new ArrayList<String>();
        for (Violation violation : violations) {
            result.add(violation.getLocation());
        }
        return result;
    }

    private static SkipPathMatcher noSkips() {
        return new SkipPathMatcher(Collections.<String>emptyList());
    }

    private static Consumer<Violation> ignoreViolations() {
        return violation -> { };
    }

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
