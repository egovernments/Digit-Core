package org.digit.util;

import org.digit.config.PropagationProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HeaderStore {
    private final HttpHeaders headers = new HttpHeaders();

    public static HeaderStore fromRequestContext(PropagationProperties props) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return new HeaderStore();
        }
        HttpServletRequest req = sra.getRequest();
        HeaderStore store = new HeaderStore();
        Enumeration<String> names = req.getHeaderNames();
        if (names == null) {
            return store;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!props.shouldPropagate(name)) continue;
            Collections.list(req.getHeaders(name)).forEach(value -> store.add(name, value));
        }
        return store;
    }

    public static String extractTenantId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        HttpServletRequest req = sra.getRequest();
        String tenantId = req.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId;
        }
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtTokenUtil.extractTenantId(token);
        }
        return null;
    }

    public static String extractClientId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return null;
        }
        HttpServletRequest req = sra.getRequest();
        String clientId = req.getHeader("X-Client-Id");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return clientId;
        }
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtTokenUtil.extractClientId(token);
        }
        return null;
    }

    void add(String name, String value) {
        this.headers.add(name, value);
    }

    public void applyTo(HttpHeaders target, PropagationProperties props) {
        this.headers.forEach((k, v) -> v.forEach(val -> target.add(k, val)));
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public static Map<String, String> getHeadersToPropagate(PropagationProperties props) {
        HeaderStore store = HeaderStore.fromRequestContext(props);
        HashMap<String, String> result = new HashMap<>();
        store.getHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                result.put(name, values.get(0));
            }
        });
        return result;
    }
}

