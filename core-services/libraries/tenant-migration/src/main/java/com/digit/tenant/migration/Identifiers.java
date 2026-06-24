package com.digit.tenant.migration;

/**
 * SQL identifier quoting, reproducing the Go {@code quoteIdent}:
 * double-quote the identifier and escape embedded double quotes by doubling them.
 */
final class Identifiers {

    private Identifiers() {
    }

    static String quoteIdent(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
