package org.egov.userevent.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.egov.tracer.model.CustomException;
import org.egov.userevent.model.AuditDetails;
import org.egov.userevent.model.LastAccesDetails;
import org.egov.userevent.model.RecepientEvent;
import org.egov.userevent.model.enums.Source;
import org.egov.userevent.model.enums.Status;
import org.egov.userevent.persistence.entity.RecepientEventRegistryEntity;
import org.egov.userevent.persistence.entity.UserEventEntity;
import org.egov.userevent.persistence.entity.UserLastAccessEntity;
import org.egov.userevent.persistence.repository.RecepientEventRegistryJpaRepository;
import org.egov.userevent.persistence.repository.UserEventJpaRepository;
import org.egov.userevent.persistence.repository.UserLastAccessJpaRepository;
import org.egov.userevent.web.contract.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

public class EventPersistenceServiceTest {

	private UserEventJpaRepository eventRepository;
	private RecepientEventRegistryJpaRepository registryRepository;
	private UserLastAccessJpaRepository lastAccessRepository;
	private EventPersistenceService service;

	@BeforeEach
	public void setup() {
		eventRepository = mock(UserEventJpaRepository.class);
		registryRepository = mock(RecepientEventRegistryJpaRepository.class);
		lastAccessRepository = mock(UserLastAccessJpaRepository.class);
		service = new EventPersistenceService();
		ReflectionTestUtils.setField(service, "eventRepository", eventRepository);
		ReflectionTestUtils.setField(service, "registryRepository", registryRepository);
		ReflectionTestUtils.setField(service, "lastAccessRepository", lastAccessRepository);
	}

	private Event sampleEvent() {
		return Event.builder()
				.id("event-1")
				.tenantId("pb.amritsar")
				.eventType("BROADCAST")
				.eventCategory("PUBLICHEALTH")
				.name("some name")
				.description("some description")
				.status(Status.ACTIVE)
				.source(Source.WEBAPP)
				.postedBy("user-uuid")
				.referenceId("ref-1")
				.auditDetails(AuditDetails.builder()
						.createdBy("creator").createdTime(1L)
						.lastModifiedBy("modifier").lastModifiedTime(2L).build())
				.recepientEventMap(List.of(
						RecepientEvent.builder().recepient("*|*|pb.amritsar").eventId("event-1").build()))
				.build();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void saveEventsWritesEventRowAndRegistryRows() {
		service.saveEvents(List.of(sampleEvent()));

		ArgumentCaptor<List<UserEventEntity>> eventCaptor = ArgumentCaptor.forClass(List.class);
		verify(eventRepository).saveAll(eventCaptor.capture());
		UserEventEntity entity = eventCaptor.getValue().get(0);
		assertEquals("event-1", entity.getId());
		assertEquals("pb.amritsar", entity.getTenantId());
		assertEquals("ACTIVE", entity.getStatus());
		assertEquals("WEBAPP", entity.getSource());
		assertEquals("PUBLICHEALTH", entity.getCategory());
		assertEquals("creator", entity.getCreatedBy());
		assertEquals(Long.valueOf(2L), entity.getLastModifiedTime());

		ArgumentCaptor<List<RecepientEventRegistryEntity>> registryCaptor = ArgumentCaptor.forClass(List.class);
		verify(registryRepository).saveAll(registryCaptor.capture());
		assertEquals("*|*|pb.amritsar", registryCaptor.getValue().get(0).getRecepient());
		assertEquals("event-1", registryCaptor.getValue().get(0).getEventId());
	}

	@Test
	public void updateEventsMutatesOnlyMutableColumnsAndReplacesRegistry() {
		UserEventEntity existing = UserEventEntity.builder()
				.id("event-1")
				.tenantId("original-tenant")
				.source("MOBILEAPP")
				.eventType("ORIGINALTYPE")
				.postedBy("original-poster")
				.referenceId("original-ref")
				.status("ACTIVE")
				.description("old description")
				.build();
		when(eventRepository.findById("event-1")).thenReturn(Optional.of(existing));

		Event update = sampleEvent();
		update.setStatus(Status.CANCELLED);
		service.updateEvents(List.of(update));

		ArgumentCaptor<UserEventEntity> captor = ArgumentCaptor.forClass(UserEventEntity.class);
		verify(eventRepository).save(captor.capture());
		UserEventEntity saved = captor.getValue();
		assertEquals("CANCELLED", saved.getStatus());
		assertEquals("some description", saved.getDescription());
		assertEquals("modifier", saved.getLastModifiedBy());
		// immutable columns keep their stored values, mirroring the persister UPDATE
		assertEquals("original-tenant", saved.getTenantId());
		assertEquals("MOBILEAPP", saved.getSource());
		assertEquals("ORIGINALTYPE", saved.getEventType());
		assertEquals("original-poster", saved.getPostedBy());
		assertEquals("original-ref", saved.getReferenceId());

		verify(registryRepository).deleteByEventId("event-1");
		verify(registryRepository).saveAll(anyList());
	}

	@Test
	public void updateOfMissingEventThrows() {
		when(eventRepository.findById("event-1")).thenReturn(Optional.empty());
		assertThrows(CustomException.class, () -> service.updateEvents(List.of(sampleEvent())));
	}

	@Test
	public void saveLastAccessTimeUpserts() {
		service.saveLastAccessTime(LastAccesDetails.builder().userId("u-1").lastAccessTime(42L).build());

		ArgumentCaptor<UserLastAccessEntity> captor = ArgumentCaptor.forClass(UserLastAccessEntity.class);
		verify(lastAccessRepository).save(captor.capture());
		assertEquals("u-1", captor.getValue().getUserId());
		assertEquals(Long.valueOf(42L), captor.getValue().getLastAccessTime());
	}
}
