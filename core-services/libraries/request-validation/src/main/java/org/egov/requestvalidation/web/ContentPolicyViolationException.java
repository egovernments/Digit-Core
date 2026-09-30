package org.egov.requestvalidation.web;

import org.egov.requestvalidation.core.Violation;
import org.egov.tracer.model.CustomException;

/** Only fixed text reaches tracer; deliberately retains no input or parser exception. */
public final class ContentPolicyViolationException extends CustomException {
    private static final long serialVersionUID = 1L;

    public ContentPolicyViolationException(Violation violation) {
        super(violation.getCode().name(), violation.getCode().getMessage());
    }
}
