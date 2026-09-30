package org.egov.requestvalidation.web;

import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.Violation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ValidationAuditLogger {
    private static final Logger LOG = LoggerFactory.getLogger(ValidationAuditLogger.class);
    private final double reportSampleRate;

    public ValidationAuditLogger(double reportSampleRate) {
        this.reportSampleRate = reportSampleRate;
    }

    public void violation(ValidationMode mode, Violation violation, String kind, String handler, String method) {
        if (mode == ValidationMode.REPORT && ThreadLocalRandom.current().nextDouble() >= reportSampleRate) return;
        // A plain logback encoder may also print MDC. Sanitize that context for this log only.
        Map<String, String> original = MDC.getCopyOfContextMap();
        try {
            if (original != null) {
                Map<String, String> safe = new HashMap<>();
                original.forEach((key, value) -> safe.put(safeText(key), safeText(value)));
                MDC.setContextMap(safe);
            }
            LOG.warn("request_validation mode={} code={} rule={} location={} kind={} handler={} method={} length={}",
                    mode, violation.getCode(), safeText(violation.getRuleId()), safeText(violation.getLocation()),
                    kind, safeText(handler), safeText(method), violation.getLength());
        } finally {
            if (original == null) MDC.clear(); else MDC.setContextMap(original);
        }
    }

    public static String safeText(String value) {
        if (value == null) return "";
        StringBuilder safe = new StringBuilder(Math.min(value.length(), 256));
        for (int i = 0; i < value.length() && i < 256; i++) {
            char ch = value.charAt(i);
            safe.append(ch >= 32 && ch <= 126 && ch != '<' && ch != '>' ? ch : '_');
        }
        return safe.toString();
    }
}
