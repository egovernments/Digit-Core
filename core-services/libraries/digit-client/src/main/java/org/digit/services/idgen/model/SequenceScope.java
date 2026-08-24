package org.digit.services.idgen.model;

/** When a template's sequence counter resets. Mirrors the idgen service's {@code SequenceScope}. */
public enum SequenceScope {
    /** Never resets. */
    GLOBAL,
    DAILY,
    MONTHLY,
    YEARLY
}
