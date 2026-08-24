package org.digit.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public class PropagationProperties {
    // x-user-id is not optional: billing and workflow require it on every write, and registry
    // requires it on every route including GETs, so omitting it makes those calls fail with a
    // validation error before any handler runs. x-roles is read by workflow when evaluating which
    // actions a caller may take.
    private static final String DEFAULT_ALLOW =
            "authorization,x-correlation-id,x-request-id,x-tenant-id,x-user-id,x-client-id,x-roles";
    private static final String DEFAULT_PREFIXES = "x-ctx-,x-trace-";

    @Value(value="${digit.propagate.headers.allow:" + DEFAULT_ALLOW + "}")
    private String allowString = DEFAULT_ALLOW;
    @Value(value="${digit.propagate.headers.prefixes:" + DEFAULT_PREFIXES + "}")
    private String prefixesString = DEFAULT_PREFIXES;

    public List<String> getAllow() {
        if (this.allowString == null || this.allowString.trim().isEmpty()) {
            return Arrays.asList(new String[0]);
        }
        return Arrays.asList(this.allowString.split(","));
    }

    public List<String> getPrefixes() {
        if (this.prefixesString == null || this.prefixesString.trim().isEmpty()) {
            return Arrays.asList(new String[0]);
        }
        return Arrays.asList(this.prefixesString.split(","));
    }

    public boolean shouldPropagate(String name) {
        String headerName = name.toLowerCase();
        if (this.getAllow().stream().map(String::toLowerCase).anyMatch(headerName::equals)) {
            return true;
        }
        return this.getPrefixes().stream().map(String::toLowerCase).anyMatch(headerName::startsWith);
    }
}

