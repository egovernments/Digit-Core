package org.egov.requestvalidation.web;

import jakarta.servlet.http.HttpServletRequest;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.ViolationCode;
import org.egov.requestvalidation.core.Violation;

/** Request-local cap shared by the scalar and body hooks. */
public final class ValidationReporter {
    private static final String COUNT = ValidationReporter.class.getName() + ".count";
    private static final String INCOMPLETE = ValidationReporter.class.getName() + ".incomplete";
    // Ten observations per request; the last slot is kept for the incomplete-inspection event.
    private static final int MAX_FINDINGS = 9;
    static final String INCOMPLETE_RULE = "inspection-incomplete";
    private final ValidationAuditLogger logger;

    public ValidationReporter(ValidationAuditLogger logger) { this.logger = logger; }

    public void content(HttpServletRequest request, ValidationMode mode, Violation violation,
                        String kind, String handler) {
        record(request, mode, violation, kind, handler);
        if (mode == ValidationMode.ENFORCE) throw new ContentPolicyViolationException(violation);
    }

    public ContentPolicyViolationException rejected(HttpServletRequest request, Violation violation,
                                                    String kind, String handler) {
        record(request, ValidationMode.ENFORCE, violation, kind, handler);
        return new ContentPolicyViolationException(violation);
    }

    /**
     * Syntax, encoding and limit findings stop inspection. ENFORCE returns the rejection to throw;
     * REPORT records the finding and the incomplete inspection, and returns null so the caller
     * hands the original input to the host's converter.
     */
    public ContentPolicyViolationException structural(HttpServletRequest request, ValidationMode mode,
                                                      Violation violation, String kind, String handler) {
        if (mode == ValidationMode.ENFORCE) return rejected(request, violation, kind, handler);
        record(request, mode, violation, kind, handler);
        incomplete(request, violation.getCode(), kind, handler);
        return null;
    }

    /** Records, once per request, that REPORT inspection did not cover the whole request. */
    public void incomplete(HttpServletRequest request, ViolationCode code, String kind, String handler) {
        if (request.getAttribute(INCOMPLETE) != null) return;
        request.setAttribute(INCOMPLETE, Boolean.TRUE);
        logger.violation(ValidationMode.REPORT, new Violation(code, INCOMPLETE_RULE, "/", 0),
                kind, handler, request.getMethod());
    }

    private void record(HttpServletRequest request, ValidationMode mode, Violation violation,
                        String kind, String handler) {
        Integer count = (Integer) request.getAttribute(COUNT);
        if (count == null) count = 0;
        // An ENFORCE rejection ends the request, so it is logged even when REPORT findings filled the cap.
        if (mode == ValidationMode.ENFORCE || count < MAX_FINDINGS) {
            request.setAttribute(COUNT, count + 1);
            logger.violation(mode, violation, kind, handler, request.getMethod());
        }
    }
}
