package org.egov.userevent.web.contract.v3;

/**
 * Event lifecycle status as exposed by the 3.0 API contract. Note the
 * single-L CANCELED spelling mandated by the spec; the internal
 * {@link org.egov.userevent.model.enums.Status} (and the DB) use CANCELLED.
 * Mapping between the two lives in EventApiMapper.
 */
public enum EventStatusV3 {
	ACTIVE, INACTIVE, CANCELED
}
