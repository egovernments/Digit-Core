package org.digit.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public class PropagationProperties {
    @Value(value="${digit.propagate.headers.allow:authorization,x-correlation-id,x-request-id,x-tenant-id,x-client-id}")
    private String allowString;
    @Value(value="${digit.propagate.headers.prefixes:x-ctx-,x-trace-}")
    private String prefixesString;

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

