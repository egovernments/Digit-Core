package org.egov.userevent.persistence;

import java.util.ArrayList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.userevent.model.LastAccesDetails;
import org.egov.userevent.model.RecepientEvent;
import org.egov.userevent.persistence.entity.RecepientEventRegistryEntity;
import org.egov.userevent.persistence.entity.UserEventEntity;
import org.egov.userevent.persistence.entity.UserLastAccessEntity;
import org.egov.userevent.persistence.repository.RecepientEventRegistryJpaRepository;
import org.egov.userevent.persistence.repository.UserEventJpaRepository;
import org.egov.userevent.persistence.repository.UserLastAccessJpaRepository;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Direct, transactional persistence for user events. Replaces the former
 * Kafka round-trip through egov-persister (topics save-user-events /
 * update-user-events / user-events-lat) — writes now commit before the API
 * responds, so create/update are no longer eventually consistent.
 *
 * The semantics deliberately mirror the retired egov-user-event-persister.yml:
 * - create: insert the event row plus one registry row per recepientEventMap
 *   entry;
 * - update: only description, status, name, category and the three jsonb
 *   columns plus lastmodifiedby/lastmodifiedtime are mutable (tenantid,
 *   source, eventtype, postedby, referenceid are immutable), and the
 *   recipient registry is fully replaced (delete + reinsert);
 * - last-access time: upsert on userid.
 */
@Slf4j
@Service
public class EventPersistenceService {

	@Autowired
	private UserEventJpaRepository eventRepository;

	@Autowired
	private RecepientEventRegistryJpaRepository registryRepository;

	@Autowired
	private UserLastAccessJpaRepository lastAccessRepository;

	@Transactional
	public void saveEvents(List<Event> events) {
		List<UserEventEntity> entities = new ArrayList<>();
		List<RecepientEventRegistryEntity> registryRows = new ArrayList<>();
		for (Event event : events) {
			entities.add(toEntity(event));
			registryRows.addAll(toRegistryRows(event));
		}
		eventRepository.saveAll(entities);
		registryRepository.saveAll(registryRows);
	}

	@Transactional
	public void updateEvents(List<Event> events) {
		for (Event event : events) {
			UserEventEntity entity = eventRepository.findById(event.getId())
					.orElseThrow(() -> new CustomException(ErrorConstants.MEN_UPDATE_MISSING_EVENTS_CODE,
							ErrorConstants.MEN_UPDATE_MISSING_EVENTS_MSG));
			entity.setDescription(event.getDescription());
			entity.setStatus(null != event.getStatus() ? event.getStatus().name() : null);
			entity.setName(event.getName());
			entity.setCategory(event.getEventCategory());
			entity.setEventDetails(event.getEventDetails());
			entity.setActions(event.getActions());
			entity.setRecepient(event.getRecepient());
			if (null != event.getAuditDetails()) {
				entity.setLastModifiedBy(event.getAuditDetails().getLastModifiedBy());
				entity.setLastModifiedTime(event.getAuditDetails().getLastModifiedTime());
			}
			eventRepository.save(entity);

			registryRepository.deleteByEventId(event.getId());
			registryRepository.saveAll(toRegistryRows(event));
		}
	}

	@Transactional
	public void saveLastAccessTime(LastAccesDetails details) {
		lastAccessRepository.save(UserLastAccessEntity.builder()
				.userId(details.getUserId())
				.lastAccessTime(details.getLastAccessTime())
				.build());
	}

	private UserEventEntity toEntity(Event event) {
		return UserEventEntity.builder()
				.id(event.getId())
				.tenantId(event.getTenantId())
				.source(null != event.getSource() ? event.getSource().name() : null)
				.eventType(event.getEventType())
				.category(event.getEventCategory())
				.name(event.getName())
				.description(event.getDescription())
				.status(null != event.getStatus() ? event.getStatus().name() : null)
				.postedBy(event.getPostedBy())
				.referenceId(event.getReferenceId())
				.recepient(event.getRecepient())
				.eventDetails(event.getEventDetails())
				.actions(event.getActions())
				.createdBy(null != event.getAuditDetails() ? event.getAuditDetails().getCreatedBy() : null)
				.createdTime(null != event.getAuditDetails() ? event.getAuditDetails().getCreatedTime() : null)
				.lastModifiedBy(null != event.getAuditDetails() ? event.getAuditDetails().getLastModifiedBy() : null)
				.lastModifiedTime(null != event.getAuditDetails() ? event.getAuditDetails().getLastModifiedTime() : null)
				.build();
	}

	private List<RecepientEventRegistryEntity> toRegistryRows(Event event) {
		List<RecepientEventRegistryEntity> rows = new ArrayList<>();
		List<RecepientEvent> recepientEventMap = event.getRecepientEventMap();
		if (CollectionUtils.isEmpty(recepientEventMap)) {
			log.warn("Event {} has no recepientEventMap; no registry rows written", event.getId());
			return rows;
		}
		for (RecepientEvent recepientEvent : recepientEventMap) {
			rows.add(RecepientEventRegistryEntity.builder()
					.recepient(recepientEvent.getRecepient())
					.eventId(recepientEvent.getEventId())
					.build());
		}
		return rows;
	}
}
