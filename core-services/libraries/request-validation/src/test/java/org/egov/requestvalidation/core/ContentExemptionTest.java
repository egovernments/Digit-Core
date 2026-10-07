package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ContentExemptionTest {
    private static final InspectionLimits GENEROUS =
            new InspectionLimits(100_000, 64, 10_000, 256, 10_000, 1000, 4096);
    private static final Set<String> HTML_CODES = new HashSet<String>(Arrays.asList("EMAIL_BODY", "EMAIL_NOTES"));

    private final JsonDocumentInspector inspector = new JsonDocumentInspector(
            new ContentDetector(ContentPolicy.builder().build()));

    /** The localization shape: markup is allowed only in the message of listed codes, and never script. */
    private static final ContentExemption LOCALIZATION = value -> value.matches("/messages/*/message")
            && value.sibling("code").map(HTML_CODES::contains).orElse(false)
            && !value.value().toLowerCase().contains("<script");

    @Test
    void acceptsOnlyTheValuesTheRuleAllows() {
        String body = "{\"RequestInfo\":{},\"messages\":["
                + "{\"code\":\"EMAIL_BODY\",\"message\":\"<p>Hello <b>{name}</b></p>\"},"
                + "{\"code\":\"OTHER\",\"message\":\"<p>not listed</p>\"},"
                + "{\"code\":\"EMAIL_NOTES\",\"message\":\"<script>alert(1)</script>\"},"
                + "{\"code\":\"EMAIL_BODY\",\"module\":\"<b>other field</b>\"}]}";

        assertEquals(Arrays.asList("/messages/1/message", "/messages/2/message", "/messages/3/module"),
                locations(inspect(body, LOCALIZATION)));
        assertEquals(Arrays.asList("/messages/0/message", "/messages/1/message", "/messages/2/message",
                "/messages/3/module"), locations(inspect(body, null)));
    }

    @Test
    void seesASiblingThatFollowsTheValue() {
        String body = "{\"messages\":[{\"message\":\"<p>Hi</p>\",\"locale\":\"en_IN\",\"code\":\"EMAIL_BODY\"}]}";

        assertEquals(Collections.emptyList(), inspect(body, LOCALIZATION));
    }

    @Test
    void neverOffersFieldNames() {
        AtomicInteger calls = new AtomicInteger();
        List<Violation> violations = inspect("{\"<b>\":\"x\",\"a\":\"<b>\"}", value -> {
            calls.incrementAndGet();
            return true;
        });

        assertEquals(Arrays.asList("/*"), locations(violations));
        assertEquals(1, calls.get());
    }

    @Test
    void keepsDocumentOrderAcrossNamesAndValues() {
        String body = "{\"a\":\"<i>\",\"<b>\":{\"c\":\"javascript:x\"},\"d\":[\"<u>\"]}";

        assertEquals(locations(inspect(body, null)), locations(inspect(body, value -> false)));
        assertEquals(Arrays.asList("/a", "/*", "/*/c", "/d/0"), locations(inspect(body, value -> false)));
    }

    @Test
    void anExemptionThatThrowsKeepsTheFinding() {
        List<Violation> violations = inspect("{\"a\":\"<b>\"}", value -> {
            throw new IllegalStateException("broken rule");
        });

        assertEquals(Arrays.asList("/a"), locations(violations));
    }

    @Test
    void aSyntaxFailureReportsEarlierFindingsUnchangedWithoutConsultingTheExemption() {
        AtomicInteger calls = new AtomicInteger();
        List<Violation> found = new ArrayList<Violation>();

        InspectionException failure = assertThrows(InspectionException.class, () -> inspector.inspect(
                utf8("{\"a\":\"<b>\",\"b\":}"), GENEROUS, noSkips(), true, true, value -> {
                    calls.incrementAndGet();
                    return true;
                }, found::add));

        assertEquals(ViolationCode.REQUEST_JSON_MALFORMED, failure.getViolation().getCode());
        assertEquals(Arrays.asList("/a"), locations(found));
        assertEquals(0, calls.get());
    }

    @Test
    void limitFailureBehavesAsWithoutAnExemption() {
        InspectionLimits tight = new InspectionLimits(100_000, 64, 10_000, 256, 5, 1000, 4096);
        String body = "{\"a\":\"<b>\",\"b\":1,\"c\":2,\"d\":3}";
        List<Violation> plain = new ArrayList<Violation>();
        List<Violation> exempt = new ArrayList<Violation>();

        InspectionException first = assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8(body), tight, noSkips(), true, true, plain::add));
        InspectionException second = assertThrows(InspectionException.class,
                () -> inspector.inspect(utf8(body), tight, noSkips(), true, true, value -> true, exempt::add));

        assertEquals(first.getViolation().getRuleId(), second.getViolation().getRuleId());
        assertEquals(first.getViolation().getLocation(), second.getViolation().getLocation());
        assertEquals(locations(plain), locations(exempt));
    }

    @Test
    void skippedPathsAreNotOffered() {
        AtomicInteger calls = new AtomicInteger();
        List<Violation> violations = new ArrayList<Violation>();
        inspector.inspect(utf8("{\"skip\":\"<b>\",\"keep\":\"<b>\"}"), GENEROUS,
                new SkipPathMatcher(Arrays.asList("/skip")), true, true, value -> {
                    calls.incrementAndGet();
                    return false;
                }, violations::add);

        assertEquals(Arrays.asList("/keep"), locations(violations));
        assertEquals(1, calls.get());
    }

    @Test
    void exposesPathRuleAndOtherStringsOfTheBody() {
        List<FlaggedValue> seen = new ArrayList<FlaggedValue>();
        inspect("{\"Mdms\":{\"schemaCode\":\"HCM.AppFieldType\",\"isActive\":true,\"n\":7,"
                + "\"data\":{\"a/b\":{\"x~y\":\"List<String>\"}},\"list\":[\"<u>\"]}}", value -> {
                    seen.add(value);
                    return false;
                });

        assertEquals(2, seen.size());
        FlaggedValue type = seen.get(0);
        assertEquals(Arrays.asList("Mdms", "data", "a/b", "x~y"), type.path());
        assertEquals("/Mdms/data/a~1b/x~0y", type.pointer());
        assertEquals("List<String>", type.value());
        assertEquals("R1", type.rule());
        assertTrue(type.matches("/Mdms/data/*/x~0y"));
        assertFalse(type.matches("/Mdms/data"));
        assertFalse(type.matches("/Mdms/data/*/x~0y/z"));
        assertEquals(Optional.of("HCM.AppFieldType"), type.string("/Mdms/schemaCode"));
        assertEquals(Optional.of("List<String>"), type.string("/Mdms/data/a~1b/x~0y"));
        assertEquals(Optional.empty(), type.string("/Mdms/isActive"));
        assertEquals(Optional.empty(), type.string("/Mdms/n"));
        assertEquals(Optional.empty(), type.string("/Mdms/missing"));
        assertEquals(Optional.empty(), type.sibling("schemaCode"));
        assertThrows(IllegalArgumentException.class, () -> type.string("Mdms"));
        assertThrows(IllegalArgumentException.class, () -> type.string("/a~2"));
        assertFalse(type.toString().contains("List<String>"));

        FlaggedValue element = seen.get(1);
        assertEquals("/Mdms/list/0", element.pointer());
        assertEquals(Optional.empty(), element.sibling("schemaCode"));
    }

    @Test
    void aBareStringBodyHasTheRootPointer() {
        List<FlaggedValue> seen = new ArrayList<FlaggedValue>();
        inspect("\"<b>\"", value -> seen.add(value) && false);

        assertEquals("", seen.get(0).pointer());
        assertEquals(Optional.of("<b>"), seen.get(0).string(""));
        assertEquals(Optional.empty(), seen.get(0).sibling("x"));
    }

    @Test
    void withDuplicateKeysAllowedTheLastOccurrenceWins() {
        List<FlaggedValue> seen = new ArrayList<FlaggedValue>();
        List<Violation> violations = new ArrayList<Violation>();
        inspector.inspect(utf8("{\"code\":\"EMAIL_BODY\",\"message\":\"<p>x</p>\",\"code\":\"OTHER\"}"), GENEROUS,
                noSkips(), false, true, value -> {
                    seen.add(value);
                    return value.sibling("code").map(HTML_CODES::contains).orElse(false);
                }, violations::add);

        assertEquals(Optional.of("OTHER"), seen.get(0).sibling("code"));
        assertEquals(Arrays.asList("/message"), locations(violations));
    }

    @Test
    void withoutAnExemptionBothOverloadsAgree() {
        String[] bodies = {
            "{\"a\":\"<b>\"}", "[\"ok\",[\"javascript:x\"]]", "\"<b>\"", "{\"onload=\":true}",
            "{\"x\":{\"y\":[{\"z\":\"\\u0001\"}]},\"<i>\":\"<u>\"}", "{\"RequestInfo\":{\"a\":\"<svg onload=1>\"}}"
        };
        for (String body : bodies) {
            List<Violation> six = new ArrayList<Violation>();
            List<Violation> seven = new ArrayList<Violation>();
            inspector.inspect(utf8(body), GENEROUS, noSkips(), true, true, six::add);
            inspector.inspect(utf8(body), GENEROUS, noSkips(), true, true, null, seven::add);
            assertEquals(describe(six), describe(seven), body);
        }
    }

    private List<Violation> inspect(String json, ContentExemption exemption) {
        List<Violation> violations = new ArrayList<Violation>();
        inspector.inspect(utf8(json), GENEROUS, noSkips(), true, true, exemption, violations::add);
        return violations;
    }

    private static List<String> locations(List<Violation> violations) {
        List<String> locations = new ArrayList<String>();
        for (Violation violation : violations) {
            locations.add(violation.getLocation());
        }
        return locations;
    }

    private static List<String> describe(List<Violation> violations) {
        List<String> described = new ArrayList<String>();
        for (Violation violation : violations) {
            described.add(violation.getCode() + " " + violation.getRuleId() + " " + violation.getLocation()
                    + " " + violation.getLength());
        }
        return described;
    }

    private static SkipPathMatcher noSkips() {
        return new SkipPathMatcher(Collections.<String>emptyList());
    }

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
