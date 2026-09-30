package org.egov.requestvalidation.core;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Thread-safe detector for content rules R1 through R4. */
public final class ContentDetector {
    private static final int MAX_VARIANTS = 40;
    private static final int MAX_NORMALIZATION_EXPANSION = 18;

    private final ContentPolicy policy;
    private final EntityAndPercentDecoder decoder = new EntityAndPercentDecoder();
    private final Set<String> eventHandlerNames;

    public ContentDetector(ContentPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.eventHandlerNames = policy.isEventHandler()
                ? EventHandlerNameList.load() : Collections.<String>emptySet();
    }

    public Optional<String> detect(String value) {
        Objects.requireNonNull(value, "value");
        if (!policy.isNormalizeNfkc() && hasNoSentinel(value)) {
            return Optional.empty();
        }

        List<String> variants = variants(value);
        if (policy.isMarkupStart()) {
            for (String variant : variants) {
                if (hasMarkupStart(variant)) {
                    return Optional.of("R1");
                }
            }
        }
        if (policy.isUrlScheme()) {
            for (String variant : variants) {
                if (hasDeniedUrl(variant)) {
                    return Optional.of("R2");
                }
            }
        }
        if (policy.isEventHandler()) {
            for (String variant : variants) {
                if (hasEventHandlerAssignment(variant)) {
                    return Optional.of("R3");
                }
            }
        }
        if (!policy.getDisallowedControls().isEmpty()) {
            for (String variant : variants) {
                if (hasDisallowedControl(variant)) {
                    return Optional.of("R4");
                }
            }
        }
        return Optional.empty();
    }

    private List<String> variants(String original) {
        // Entity decoding only changes '&' followed by a letter or '#', and percent decoding only
        // '%' followed by two hex digits. Skipping strings without those markers leaves the variant
        // set identical and keeps the decoders off the hot path.
        if (!policy.isNormalizeNfkc() && !hasDecodingMarker(original)) {
            return Collections.singletonList(original);
        }
        LinkedHashSet<String> all = new LinkedHashSet<String>();
        all.add(original);
        if (policy.isNormalizeNfkc() && policy.getDecodeRounds() == 0) {
            all.add(Normalizer.normalize(original, Normalizer.Form.NFKC));
            return new ArrayList<String>(all);
        }
        List<String> frontier = Collections.singletonList(original);
        int growthLimit = growthLimit(original.length());

        for (int round = 0; round < policy.getDecodeRounds() && all.size() < MAX_VARIANTS; round++) {
            List<String> next = new ArrayList<String>();
            for (String candidate : frontier) {
                if (EntityAndPercentDecoder.mayDecodeEntities(candidate)) {
                    addVariant(all, next, decoder.decodeEntities(candidate), growthLimit);
                }
                if (EntityAndPercentDecoder.mayDecodePercent(candidate)) {
                    addVariant(all, next, decoder.decodePercent(candidate), growthLimit);
                }
                if (policy.isNormalizeNfkc()) {
                    addVariant(all, next,
                            Normalizer.normalize(candidate, Normalizer.Form.NFKC), growthLimit);
                }
                if (all.size() >= MAX_VARIANTS) {
                    break;
                }
            }
            if (next.isEmpty()) {
                break;
            }
            frontier = next;
        }
        return new ArrayList<String>(all);
    }

    private static void addVariant(Set<String> all, List<String> next, String candidate, int growthLimit) {
        if (candidate.length() <= growthLimit && all.size() < MAX_VARIANTS && all.add(candidate)) {
            next.add(candidate);
        }
    }

    private static int growthLimit(int originalLength) {
        long limit = Math.max((long) originalLength + 64L,
                (long) originalLength * MAX_NORMALIZATION_EXPANSION);
        return limit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
    }

    private boolean hasDeniedUrl(String value) {
        String canonical = canonicalizeUrlPrefix(value);
        for (String scheme : policy.getDeniedSchemes()) {
            if (canonical.startsWith(scheme + ":")) {
                return true;
            }
        }
        if (!canonical.startsWith("data:")) {
            return false;
        }
        int start = "data:".length();
        while (start < canonical.length() && isLeadingUrlSpaceOrControl(canonical.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < canonical.length() && canonical.charAt(end) != ';'
                && canonical.charAt(end) != ',') {
            end++;
        }
        String mediaType = canonical.substring(start, end).trim();
        return policy.getDeniedDataMediaTypes().contains(mediaType);
    }

    private static String canonicalizeUrlPrefix(String value) {
        int start = 0;
        while (start < value.length() && isLeadingUrlSpaceOrControl(value.charAt(start))) {
            start++;
        }
        StringBuilder compact = new StringBuilder(value.length() - start);
        for (int index = start; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '\t' && current != '\n' && current != '\r') {
                compact.append(Character.toLowerCase(current));
            }
        }
        return compact.toString().toLowerCase(Locale.ROOT);
    }

    private boolean hasEventHandlerAssignment(String value) {
        for (int index = 0; index + 1 < value.length(); index++) {
            if ((index > 0 && isAsciiWord(value.charAt(index - 1)))
                    || !asciiEqualsIgnoreCase(value.charAt(index), 'o')
                    || !asciiEqualsIgnoreCase(value.charAt(index + 1), 'n')) {
                continue;
            }
            int after = index + 2;
            while (after < value.length() && isAsciiWord(value.charAt(after))) {
                after++;
            }
            String candidate = value.substring(index, after).toLowerCase(Locale.ROOT);
            if (!eventHandlerNames.contains(candidate)) {
                index = after - 1;
                continue;
            }
            int equals = after;
            while (equals < value.length() && Character.isWhitespace(value.charAt(equals))) {
                equals++;
            }
            if (equals < value.length() && value.charAt(equals) == '=') {
                return true;
            }
        }
        return false;
    }

    private boolean hasDisallowedControl(String value) {
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            if (policy.getDisallowedControls().contains(Integer.valueOf(codePoint))) {
                return true;
            }
            offset += Character.charCount(codePoint);
        }
        return false;
    }

    private static boolean hasMarkupStart(String value) {
        for (int index = 0; index + 1 < value.length(); index++) {
            if (value.charAt(index) == '<') {
                char next = value.charAt(index + 1);
                if (isAsciiLetter(next) || next == '/' || next == '!' || next == '?') {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasDecodingMarker(String value) {
        return EntityAndPercentDecoder.mayDecodeEntities(value) || EntityAndPercentDecoder.mayDecodePercent(value);
    }

    private static boolean hasNoSentinel(String value) {
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '<' || current == '&' || current == '%' || current == ':'
                    || current == '=' || Character.isISOControl(current)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLeadingUrlSpaceOrControl(char value) {
        return value <= 0x20 || value == 0x7f || Character.isWhitespace(value);
    }

    private static boolean isAsciiLetter(char value) {
        return (value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z');
    }

    private static boolean isAsciiWord(char value) {
        return isAsciiLetter(value) || (value >= '0' && value <= '9') || value == '_';
    }

    private static boolean asciiEqualsIgnoreCase(char actual, char lowerExpected) {
        return actual == lowerExpected || actual == Character.toUpperCase(lowerExpected);
    }
}
