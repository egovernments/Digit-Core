package org.digit.services.billing.model;

/**
 * Demand lifecycle states, mirroring the billing service's {@code DemandStatus}.
 *
 * <p>Only {@code DRAFT} and {@code ACTIVE} may be supplied on create, update or patch; the rest are
 * reached through the service's own transitions (freeze, cancel, payment, roll-forward).
 */
public enum DemandStatus {
    DRAFT,
    ACTIVE,
    FROZEN,
    PARTIALLY_PAID,
    PAID,
    ROLL_FORWARDED,
    CANCELLED
}
