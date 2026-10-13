package org.egov.persistence.repository;

/**
 * Lightweight projection interface for message queries.
 * Avoids loading full JPA entities into the persistence context,
 * reducing memory usage from ~10 fields per entity to 5.
 */
public interface MessageProjection {
	String getCode();
	String getLocale();
	String getModule();
	String getMessage();
	String getTenantId();
}
