package org.egov.userevent.web.mapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.userevent.repository.UserEventRepository;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventDetails;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * The 3.0 update payload no longer carries the internal-only fields
 * (name, referenceId, eventCategory, source, postedBy, auditDetails, ...) and
 * the persister UPDATE rewrites the eventdetails/actions/recepient JSONB
 * columns wholesale. Before handing mapped events to
 * UserEventsService.updateEvents, this component restores those fields from
 * the current DB rows so that:
 * - enrichUpdateEvent does not NPE on a null auditDetails,
 * - counter-event generation keeps its name/referenceId basis,
 * - fields the API omitted are not silently wiped from the JSONB columns.
 *
 * Events whose id has no DB row are left untouched — the service's own
 * validateForUpdate then raises MEN_UPDATE_MISSING_EVENTS.
 */
@Slf4j
@Component
public class EventUpdateMerger {

	@Autowired
	private UserEventRepository repository;

	public void merge(List<Event> mappedEvents, String tenantId) {
		List<String> ids = mappedEvents.stream().map(Event::getId).filter(Objects::nonNull)
				.collect(Collectors.toList());
		if (ids.isEmpty())
			return;
		EventSearchCriteria criteria = new EventSearchCriteria();
		criteria.setIds(ids);
		Map<String, Event> dbEvents = repository.fetchEvents(criteria).stream()
				.collect(Collectors.toMap(Event::getId, Function.identity(), (a, b) -> a));

		for (Event event : mappedEvents) {
			Event dbEvent = dbEvents.get(event.getId());
			if (null == dbEvent)
				continue;
			if (null != tenantId && !tenantId.equals(dbEvent.getTenantId())) {
				log.warn("Header tenant {} differs from stored tenant {} for event {}", tenantId,
						dbEvent.getTenantId(), event.getId());
			}
			event.setAuditDetails(dbEvent.getAuditDetails());
			if (null != dbEvent.getName())
				event.setName(dbEvent.getName());
			event.setReferenceId(dbEvent.getReferenceId());
			event.setEventCategory(dbEvent.getEventCategory());
			event.setSource(dbEvent.getSource());
			event.setPostedBy(dbEvent.getPostedBy());
			if (null == event.getRecepient())
				event.setRecepient(dbEvent.getRecepient());
			mergeActions(event, dbEvent);
			mergeEventDetails(event, dbEvent);
		}
	}

	private void mergeActions(Event event, Event dbEvent) {
		if (null == event.getActions()) {
			event.setActions(dbEvent.getActions());
			return;
		}
		if (null == dbEvent.getActions())
			return;
		if (null == event.getActions().getId())
			event.getActions().setId(dbEvent.getActions().getId());
		if (null == event.getActions().getEventId())
			event.getActions().setEventId(dbEvent.getActions().getEventId());
		if (null == event.getActions().getTenantId())
			event.getActions().setTenantId(dbEvent.getActions().getTenantId());
	}

	private void mergeEventDetails(Event event, Event dbEvent) {
		if (null == event.getEventDetails()) {
			event.setEventDetails(dbEvent.getEventDetails());
			return;
		}
		EventDetails dbDetails = dbEvent.getEventDetails();
		if (null == dbDetails)
			return;
		EventDetails details = event.getEventDetails();
		details.setId(dbDetails.getId());
		details.setEventId(dbDetails.getEventId());
		if (null == details.getOrganizer())
			details.setOrganizer(dbDetails.getOrganizer());
		if (null == details.getAddress())
			details.setAddress(dbDetails.getAddress());
		if (null == details.getFees())
			details.setFees(dbDetails.getFees());
		if (null == details.getFromDate())
			details.setFromDate(dbDetails.getFromDate());
		if (null == details.getToDate())
			details.setToDate(dbDetails.getToDate());
		if (null == details.getDocuments())
			details.setDocuments(dbDetails.getDocuments());
		if (null == details.getLatitude())
			details.setLatitude(dbDetails.getLatitude());
		if (null == details.getLongitude())
			details.setLongitude(dbDetails.getLongitude());
	}
}
