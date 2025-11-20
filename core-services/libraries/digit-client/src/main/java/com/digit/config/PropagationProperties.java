package com.digit.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for header propagation.
 * Uses @Value annotations for direct property binding.
 */
public class PropagationProperties {
    
    @Value("${digit.propagate.headers.allow:authorization,x-correlation-id,x-request-id,x-tenant-id,x-client-id}")
    private String allowString;
    
    @Value("${digit.propagate.headers.prefixes:x-ctx-,x-trace-}")
    private String prefixesString;

    public List<String> getAllow() {
        if (allowString == null || allowString.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(allowString.split(","));
    }

    public List<String> getPrefixes() {
        if (prefixesString == null || prefixesString.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(prefixesString.split(","));
    }

    /**
     * Determines if a header should be propagated based on the configuration.
     */
    public boolean shouldPropagate(String name) {
        String headerName = name.toLowerCase();
        
        // Check exact matches
        if (getAllow().stream().map(String::toLowerCase).anyMatch(headerName::equals)) {
            return true;
        }
        
        // Check prefix matches
        return getPrefixes().stream()
                .map(String::toLowerCase)
                .anyMatch(headerName::startsWith);
    }
}
