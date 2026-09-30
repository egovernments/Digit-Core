package org.egov.requestvalidation.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.jsoup.parser.Parser;

final class EntityAndPercentDecoder {
    /** False only when decodeEntities cannot change the value: jsoup needs '&' then a letter or '#'. */
    static boolean mayDecodeEntities(String value) {
        for (int index = value.indexOf('&'); index >= 0 && index + 1 < value.length();
                index = value.indexOf('&', index + 1)) {
            char next = value.charAt(index + 1);
            if (next == '#' || Character.isLetter(next)) {
                return true;
            }
        }
        return false;
    }

    /** False only when decodePercent cannot change the value: it needs '%' then two hex digits. */
    static boolean mayDecodePercent(String value) {
        for (int index = value.indexOf('%'); index >= 0; index = value.indexOf('%', index + 1)) {
            if (isEscapeAt(value, index)) {
                return true;
            }
        }
        return false;
    }

    String decodeEntities(String value) {
        return Parser.unescapeEntities(value, false);
    }

    String decodePercent(String value) {
        StringBuilder output = new StringBuilder(value.length());
        int index = 0;
        while (index < value.length()) {
            if (!isEscapeAt(value, index)) {
                output.append(value.charAt(index++));
                continue;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while (isEscapeAt(value, index)) {
                bytes.write((hex(value.charAt(index + 1)) << 4) | hex(value.charAt(index + 2)));
                index += 3;
            }
            // Replacement is intentional for invalid UTF-8 bytes: valid bytes in the same
            // run (for example %3C before %FF) must still be decoded and inspected.
            output.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        }
        return output.toString();
    }

    private static boolean isEscapeAt(String value, int index) {
        return index + 2 < value.length()
                && value.charAt(index) == '%'
                && hex(value.charAt(index + 1)) >= 0
                && hex(value.charAt(index + 2)) >= 0;
    }

    private static int hex(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }

}
