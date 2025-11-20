package com.digit.util;

import com.digit.config.PropagationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Utility class for extracting and storing headers from the current request context.
 * Used for automatic header propagation in outbound API calls.
 */
public class HeaderStore {

    private final HttpHeaders headers = new HttpHeaders();

    /**
     * Creates a HeaderStore from the current request context.
     * Extracts headers that should be propagated based on the configuration.
     * 
     * @param props the propagation properties configuration
     * @return HeaderStore containing headers to propagate
     */
    public static HeaderStore fromRequestContext(PropagationProperties props) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return new HeaderStore(); // no incoming HTTP request (scheduler, test, etc.)
        }
        
        HttpServletRequest req = sra.getRequest();
        HeaderStore store = new HeaderStore();
        Enumeration<String> names = req.getHeaderNames();
        
        if (names == null) {
            return store;
        }
        
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (props.shouldPropagate(name)) {
                Collections.list(req.getHeaders(name))
                    .forEach(value -> store.add(name, value));
            }
        }
        
        return store;
    }

    /**
     * Extracts tenant ID from the current request context headers.
     * Looks for X-Tenant-ID header first, then falls back to JWT token extraction.
     * 
     * @return tenant ID or null if not found
     */
    public static String extractTenantId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        
        HttpServletRequest req = sra.getRequest();
        
        // First try X-Tenant-ID header
        String tenantId = req.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId;
        }
        
        // Fallback to JWT token extraction
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtTokenUtil.extractTenantId(token);
        }
        
        return null;
    }

    /**
     * Extracts client ID from the current request context headers.
     * Looks for X-Client-Id header first, then falls back to JWT token extraction.
     * 
     * @return client ID or null if not found
     */
    public static String extractClientId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        
        HttpServletRequest req = sra.getRequest();
        
        // First try X-Client-Id header
        String clientId = req.getHeader("X-Client-Id");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return clientId;
        }
        
        // Fallback to JWT token extraction
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtTokenUtil.extractClientId(token);
        }
        
        return null;
    }

    /**
     * Adds a header to the store.
     * 
     * @param name the header name
     * @param value the header value
     */
    void add(String name, String value) {
        headers.add(name, value);
    }

    /**
     * Applies stored headers to the target HttpHeaders object.
     * 
     * @param target the target headers to apply to
     * @param props the propagation properties (unused but kept for consistency)
     */
    public void applyTo(HttpHeaders target, PropagationProperties props) {
        headers.forEach((k, v) -> v.forEach(val -> target.add(k, val)));
    }

    /**
     * Gets the stored headers.
     * 
     * @return the HttpHeaders object containing stored headers
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Static method to get headers to propagate from current request context.
     * 
     * @param props the propagation properties configuration
     * @return Map of header names to values that should be propagated
     */
    public static java.util.Map<String, String> getHeadersToPropagate(PropagationProperties props) {
        HeaderStore store = fromRequestContext(props);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        
        store.getHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                result.put(name, values.get(0)); // Use first value if multiple
            }
        });
        
        return result;
    }
}
