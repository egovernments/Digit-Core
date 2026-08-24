package org.digit.services.billing.model;

/** How a business service accepts collection. Mirrors the billing service's {@code CollectionMode}. */
public enum CollectionMode {
    ONLINE,
    OFFLINE,
    COUNTER,
    FIELD,
    BOTH
}
