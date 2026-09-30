package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContentDetectorTest {
    private final ContentDetector detector = new ContentDetector(ContentPolicy.builder().build());

    @Test
    void detectsEachDefaultRule() {
        assertRule("R1", "prefix <script");
        assertRule("R2", " \tjava\nscript:alert(1)");
        assertRule("R2", "data:image/svg+xml,encoded-content");
        assertRule("R3", "onrejectionhandled = value");
        assertRule("R4", "left\u0000right");
    }

    @Test
    void composesEntityAndPercentDecodingWithinConfiguredRounds() {
        assertRule("R1", "%26lt%3Bscript");
        assertRule("R1", "&percnt;3Cscript");
        assertRule("R3", "onload%3dvalue");
    }

    @Test
    void decodesMarkersThatOnlyAppearAfterAnEarlierRound() {
        assertRule("R1", "&amp;lt;b&amp;gt;");   // entity, then entity
        assertRule("R1", "&#37;3Cb");            // entity yields '%', then percent
        // Two percent rounds reach "&lt;b"; the entity is a third round, so only rounds = 3 finds it.
        assertEquals(Optional.empty(), detector.detect("%2526lt;b"));
        assertEquals(Optional.of("R1"),
                new ContentDetector(ContentPolicy.builder().decodeRounds(3).build()).detect("%2526lt;b"));
    }

    @Test
    void valuesWithoutDecodingMarkersAreJudgedOnTheirOwnText() {
        assertEquals(Optional.empty(), detector.detect("2026-09-30T12:00:00Z"));
        assertEquals(Optional.empty(), detector.detect("key=value: plain"));
        assertRule("R2", "javascript:alert(1)");
        assertRule("R3", "\" onerror = x");
    }

    @Test
    void decodesValidPrefixOfPercentRunContainingInvalidUtf8() {
        assertRule("R1", "%3C%73%63%72%69%70%74%FF");
    }

    @Test
    void normalizesBeforeFastPathEvenWithZeroDecodeRounds() {
        ContentDetector normalizing = new ContentDetector(ContentPolicy.builder()
                .decodeRounds(0)
                .normalizeNfkc(true)
                .build());

        assertEquals(Optional.of("R1"), normalizing.detect("\uFF1Cscript"));
    }

    @Test
    void acceptsBenignMultilingualAndPunctuationCorpus() {
        for (String value : Arrays.asList(
                "O'Brien", "A&B Health Centre", "age < 5 years", "x <= y", "50% complete",
                "हिंदी पता", "বাংলা নাম", "العنوان", "தமிழ் குறிப்பு", "中文地址", "🙂")) {
            assertFalse(detector.detect(value).isPresent(), value);
        }
    }

    @Test
    void policyRejectsNonC0AndExemptWhitespaceControls() {
        assertThrows(IllegalArgumentException.class, () -> ContentPolicy.builder()
                .disallowedControls(Collections.singleton(Integer.valueOf(0x80))).build());
        assertThrows(IllegalArgumentException.class, () -> ContentPolicy.builder()
                .disallowedControls(Collections.singleton(Integer.valueOf(9))).build());
    }

    @Test
    void policyCopiesMutableInputSets() {
        LinkedHashSet<String> schemes = new LinkedHashSet<String>();
        schemes.add("custom");
        ContentPolicy policy = ContentPolicy.builder().deniedSchemes(schemes).build();
        schemes.add("later");

        assertEquals(Collections.singleton("custom"), policy.getDeniedSchemes());
    }

    private void assertRule(String expected, String input) {
        assertEquals(Optional.of(expected), detector.detect(input));
    }
}
