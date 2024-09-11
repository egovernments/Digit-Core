package org.egov.access.util;

public class Utils {

    private Utils() {
    }

    private static final String OPENING_BRACES = "{";
    private static final String CLOSING_BRACES = "}";
    private static final String PARAMETER_PLACEHOLDER_REGEX = "\\{\\w+\\}";
    private static final String PARAMETER_DOUBLE_ASTERISK = "**";
    private static final String ANY_WORD_REGEX = "\\\\w+";
    private static final String WILDCARD_DOUBLE_ASTERISK_REGEX = ".*"; // To match any sequence of characters


    public static boolean isRegexUri(String url) {
        return url.contains(OPENING_BRACES) & url.contains(CLOSING_BRACES) || url.contains(PARAMETER_DOUBLE_ASTERISK);
    }

    public static boolean isRegexUriMatch(String actionUri, String requestUri) {

        return requestUri.matches(getRegexUri(actionUri));
    }

    private static String getRegexUri(String url) {
        String regexUrl = url.replaceAll(PARAMETER_PLACEHOLDER_REGEX, ANY_WORD_REGEX);
        regexUrl = regexUrl.replace(PARAMETER_DOUBLE_ASTERISK, WILDCARD_DOUBLE_ASTERISK_REGEX);
        return regexUrl;
    }
}
