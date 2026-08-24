package org.digit.services.individual.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for an individual search, matching the query parameters the service accepts.
 *
 * <p>{@code ids}, {@code individualIds} and {@code userIds} are repeatable; {@code mobileNumber} is
 * single-valued server-side, so only one can be searched at a time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualSearchCriteria {
    /** Internal UUIDs; the service rejects a value that is not a UUID. */
    private List<String> ids;
    private List<String> individualIds;
    private List<String> userIds;
    private String givenName;
    private String mobileNumber;
    /** MALE, FEMALE or OTHER. */
    private String gender;
    /** {@code yyyy-MM-dd}. */
    private String dateOfBirth;
    private Boolean includeDeleted;
    /** One-based; the service defaults to 1. */
    private Integer page;
    /** 1..100; the service defaults to 20. */
    private Integer size;
}
