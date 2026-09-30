package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class SafeLocationFormatterTest {
    @Test
    void preservesOnlySafeAsciiSegments() {
        assertEquals("/RequestInfo/0/user-name", SafeLocationFormatter.format(
                Arrays.asList("RequestInfo", "0", "user-name")));
        assertEquals("/*/*/*", SafeLocationFormatter.format(
                Arrays.asList("onload=", "नाव", repeat('a', 65))));
        assertEquals("/", SafeLocationFormatter.format(Collections.<String>emptyList()));
    }

    @Test
    void characterScanMatchesThePreviousRegexDefinition() {
        Pattern previous = Pattern.compile("[A-Za-z0-9_.-]{1,64}");
        char[] alphabet = "aZ09_.-/ *~<>=:%&\u00e9\u0928\u0000\uFF1C".toCharArray();
        Random random = new Random(7);
        for (int run = 0; run < 20000; run++) {
            char[] value = new char[random.nextInt(70)];
            for (int index = 0; index < value.length; index++) {
                value[index] = alphabet[random.nextInt(alphabet.length)];
            }
            String segment = new String(value);
            String expected = previous.matcher(segment).matches() ? segment : "*";
            assertEquals(expected, SafeLocationFormatter.scalar(segment), segment);
        }
        assertEquals(repeat('a', 64), SafeLocationFormatter.scalar(repeat('a', 64)));
        assertEquals("*", SafeLocationFormatter.scalar(null));
    }

    @Test
    void violationConstructorSanitizesExternallySuppliedLocation() {
        Violation violation = new Violation(
                ViolationCode.REQUEST_CONTENT_NOT_ALLOWED, "R1", "/safe/raw value", 9);

        assertEquals("/safe/*", violation.getLocation());
    }

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, value);
        return new String(chars);
    }
}
