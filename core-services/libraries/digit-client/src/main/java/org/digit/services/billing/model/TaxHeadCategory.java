package org.digit.services.billing.model;

/** Classification of a tax head. Mirrors the billing service's {@code TaxHeadCategory}. */
public enum TaxHeadCategory {
    TAX,
    CESS,
    PENALTY,
    INTEREST,
    REBATE,
    ROUNDING,
    ARREAR,
    OTHER
}
