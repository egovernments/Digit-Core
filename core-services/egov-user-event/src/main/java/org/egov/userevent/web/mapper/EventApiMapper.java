package org.egov.userevent.web.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.userevent.model.Document;
import org.egov.userevent.model.enums.Source;
import org.egov.userevent.model.enums.Status;
import org.egov.userevent.web.contract.Action;
import org.egov.userevent.web.contract.ActionItem;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventDetails;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.egov.userevent.web.contract.Recepient;
import org.egov.userevent.web.contract.v3.ActionItemV3;
import org.egov.userevent.web.contract.v3.ActionV3;
import org.egov.userevent.web.contract.v3.AuditDetailV3;
import org.egov.userevent.web.contract.v3.EventDetailsV3;
import org.egov.userevent.web.contract.v3.EventStatusV3;
import org.egov.userevent.web.contract.v3.EventV3;
import org.egov.userevent.model.AuditDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Translates between the 3.0 API contract (web.contract.v3) and the internal
 * Event model. The internal model — including fields the 3.0 API no longer
 * exposes (tenantId, name, source, postedBy, referenceId, eventCategory,
 * recepientEventMap) — is what the service layer, the persister topics, and
 * the DB depend on, so its JSON shape must not change.
 *
 * Defaults applied on inbound mapping, because the internal model and MDMS
 * validation require them but the 3.0 contract dropped them:
 * - name: derived from description, truncated to the internal 65-char limit.
 *   Counter-event localization and the name[] search filter see this value.
 * - source: WEBAPP (the enum has only WEBAPP/MOBILEAPP; gateway traffic gets
 *   the neutral choice).
 */
@Component
public class EventApiMapper {

	private static final int NAME_MAX_LENGTH = 65;
	private static final int MAX_SEARCH_LIMIT = 200;

	public Event toInternal(EventV3 api, String tenantId) {
		return Event.builder()
				.tenantId(tenantId)
				.id(api.getId())
				.eventType(api.getEventType())
				.description(api.getDescription())
				.name(deriveName(api.getDescription()))
				.status(toInternalStatus(api.getStatus()))
				.source(Source.WEBAPP)
				.recepient(toRecepient(api.getToRoles(), api.getToUsers()))
				.actions(toInternalAction(api.getAction(), tenantId))
				.eventDetails(toInternalDetails(api.getEventDetails()))
				.auditDetails(toInternalAudit(api.getAuditDetail()))
				.build();
	}

	public List<Event> toInternal(List<EventV3> apiEvents, String tenantId) {
		return apiEvents.stream().map(e -> toInternal(e, tenantId)).collect(Collectors.toList());
	}

	public EventV3 toApi(Event internal) {
		return EventV3.builder()
				.id(internal.getId())
				.eventType(internal.getEventType())
				.description(internal.getDescription())
				.status(toApiStatus(internal.getStatus()))
				.toRoles(null != internal.getRecepient() ? internal.getRecepient().getToRoles() : null)
				.toUsers(null != internal.getRecepient() ? internal.getRecepient().getToUsers() : null)
				.action(toApiAction(internal.getActions()))
				.eventDetails(toApiDetails(internal.getEventDetails()))
				.auditDetail(toApiAudit(internal.getAuditDetails()))
				.build();
	}

	public List<EventV3> toApi(List<Event> internalEvents) {
		if (null == internalEvents)
			return List.of();
		return internalEvents.stream().map(this::toApi).collect(Collectors.toList());
	}

	public EventSearchCriteria toSearchCriteria(String tenantId, List<String> ids, EventStatusV3 status,
			List<String> name, List<String> eventTypes, List<String> roles, List<String> userIds,
			List<String> postedBy, Long fromDate, Long toDate, Integer limit, Integer offset) {
		EventSearchCriteria criteria = new EventSearchCriteria();
		criteria.setTenantId(tenantId);
		criteria.setIds(ids);
		if (null != status)
			criteria.setStatus(List.of(toInternalStatus(status).name()));
		criteria.setName(name);
		criteria.setEventTypes(eventTypes);
		criteria.setRoles(roles);
		criteria.setUserids(userIds);
		criteria.setPostedBy(postedBy);
		criteria.setFromDate(fromDate);
		criteria.setToDate(toDate);
		if (null != limit)
			criteria.setLimit(Math.min(limit, MAX_SEARCH_LIMIT));
		criteria.setOffset(offset);
		return criteria;
	}

	public Status toInternalStatus(EventStatusV3 status) {
		if (null == status)
			return null;
		return status == EventStatusV3.CANCELED ? Status.CANCELLED : Status.valueOf(status.name());
	}

	public EventStatusV3 toApiStatus(Status status) {
		if (null == status)
			return null;
		return status == Status.CANCELLED ? EventStatusV3.CANCELED : EventStatusV3.valueOf(status.name());
	}

	private String deriveName(String description) {
		return StringUtils.abbreviate(description, NAME_MAX_LENGTH);
	}

	private Recepient toRecepient(List<String> toRoles, List<String> toUsers) {
		if (CollectionUtils.isEmpty(toRoles) && CollectionUtils.isEmpty(toUsers))
			return null;
		return Recepient.builder().toRoles(toRoles).toUsers(toUsers).build();
	}

	private Action toInternalAction(ActionV3 api, String tenantId) {
		if (null == api)
			return null;
		List<ActionItem> items = null;
		if (null != api.getActionUrls()) {
			items = api.getActionUrls().stream()
					.map(item -> ActionItem.builder().actionUrl(item.getActionUrl()).code(item.getCode()).build())
					.collect(Collectors.toList());
		}
		return Action.builder().tenantId(tenantId).id(api.getId()).eventId(api.getEventId()).actionUrls(items).build();
	}

	private ActionV3 toApiAction(Action internal) {
		if (null == internal)
			return null;
		List<ActionItemV3> items = null;
		if (null != internal.getActionUrls()) {
			items = internal.getActionUrls().stream()
					.map(item -> ActionItemV3.builder().actionUrl(item.getActionUrl()).code(item.getCode()).build())
					.collect(Collectors.toList());
		}
		return ActionV3.builder().id(internal.getId()).eventId(internal.getEventId()).actionUrls(items).build();
	}

	private EventDetails toInternalDetails(EventDetailsV3 api) {
		if (null == api)
			return null;
		List<Document> documents = null;
		if (null != api.getDocuments()) {
			documents = api.getDocuments().stream()
					.map(fileStoreId -> Document.builder().fileStoreId(fileStoreId).build())
					.collect(Collectors.toList());
		}
		return EventDetails.builder()
				.fromDate(api.getFromDate())
				.toDate(api.getToDate())
				.latitude(null != api.getLatitude() ? BigDecimal.valueOf(api.getLatitude()) : null)
				.longitude(null != api.getLongitude() ? BigDecimal.valueOf(api.getLongitude()) : null)
				.documents(documents)
				.build();
	}

	private EventDetailsV3 toApiDetails(EventDetails internal) {
		if (null == internal)
			return null;
		List<String> documents = null;
		if (null != internal.getDocuments()) {
			documents = internal.getDocuments().stream()
					.map(Document::getFileStoreId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		}
		return EventDetailsV3.builder()
				.fromDate(internal.getFromDate())
				.toDate(internal.getToDate())
				.latitude(null != internal.getLatitude() ? internal.getLatitude().doubleValue() : null)
				.longitude(null != internal.getLongitude() ? internal.getLongitude().doubleValue() : null)
				.documents(documents)
				.build();
	}

	private AuditDetails toInternalAudit(AuditDetailV3 api) {
		if (null == api)
			return null;
		return AuditDetails.builder()
				.createdBy(api.getCreatedBy())
				.createdTime(api.getCreatedTime())
				.lastModifiedBy(api.getModifiedBy())
				.lastModifiedTime(api.getModifiedTime())
				.build();
	}

	private AuditDetailV3 toApiAudit(AuditDetails internal) {
		if (null == internal)
			return null;
		return AuditDetailV3.builder()
				.createdBy(internal.getCreatedBy())
				.createdTime(internal.getCreatedTime())
				.modifiedBy(internal.getLastModifiedBy())
				.modifiedTime(internal.getLastModifiedTime())
				.build();
	}
}
