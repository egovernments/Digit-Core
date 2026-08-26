package org.egov.userevent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.userevent.model.AuditDetails;
import org.egov.userevent.model.enums.Source;
import org.egov.userevent.model.enums.Status;
import org.egov.userevent.persistence.EventPersistenceService;
import org.egov.userevent.repository.UserEventRepository;
import org.egov.userevent.utils.ResponseInfoFactory;
import org.egov.userevent.utils.UserEventsUtils;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventDetails;
import org.egov.userevent.web.contract.EventRequest;
import org.egov.userevent.web.contract.EventResponse;
import org.egov.userevent.web.contract.Recepient;
import org.egov.userevent.web.validator.UserEventsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the BROADCAST status enrichment on update: the
 * date-based INACTIVE override must not clobber an explicit CANCELLED.
 */
public class UserEventsServiceUpdateTest {

	private static final String EVENT_ID = "event-1";
	private static final long FUTURE_FROM = System.currentTimeMillis() + 30L * 24 * 3600 * 1000;
	private static final long FUTURE_TO = FUTURE_FROM + 24L * 3600 * 1000;

	private UserEventsService service;
	private EventPersistenceService persistenceService;

	@BeforeEach
	public void setup() {
		service = new UserEventsService();
		persistenceService = mock(EventPersistenceService.class);
		UserEventRepository repository = mock(UserEventRepository.class);
		UserEventsValidator validator = mock(UserEventsValidator.class);

		service.setPersistenceService(persistenceService);
		service.setRepository(repository);
		service.setValidator(validator);
		service.setUtils(new UserEventsUtils());
		service.setResponseInfo(new ResponseInfoFactory());
		service.setLocalizationService(mock(LocalizationService.class));

		when(repository.fetchEvents(any())).thenReturn(List.of(storedEvent()));
		when(repository.fetchTotalEventCount(any())).thenReturn(1);
	}

	private Event storedEvent() {
		return Event.builder()
				.id(EVENT_ID)
				.tenantId("pb.amritsar")
				.eventType("BROADCAST")
				.name("some broadcast")
				.description("some broadcast description")
				.status(Status.INACTIVE)
				.source(Source.WEBAPP)
				.eventDetails(EventDetails.builder().id("ed-1").eventId(EVENT_ID)
						.fromDate(FUTURE_FROM).toDate(FUTURE_TO).build())
				.auditDetails(AuditDetails.builder().createdBy("creator").createdTime(1L)
						.lastModifiedBy("creator").lastModifiedTime(1L).build())
				.build();
	}

	private Event updateRequestEvent(Status requestedStatus) {
		Event event = storedEvent();
		event.setStatus(requestedStatus);
		event.setRecepient(Recepient.builder().toRoles(List.of("All")).build());
		return event;
	}

	private RequestInfo requestInfo() {
		return RequestInfo.builder()
				.userInfo(User.builder().uuid("user-1").type("EMPLOYEE").tenantId("pb.amritsar").build())
				.build();
	}

	@Test
	public void cancellingFutureBroadcastKeepsCancelledStatus() {
		EventRequest request = EventRequest.builder().requestInfo(requestInfo())
				.events(List.of(updateRequestEvent(Status.CANCELLED))).build();

		EventResponse response = service.updateEvents(request);

		assertEquals(Status.CANCELLED, response.getEvents().get(0).getStatus());
	}

	@Test
	public void activatingFutureBroadcastIsStillForcedInactive() {
		EventRequest request = EventRequest.builder().requestInfo(requestInfo())
				.events(List.of(updateRequestEvent(Status.ACTIVE))).build();

		EventResponse response = service.updateEvents(request);

		assertEquals(Status.INACTIVE, response.getEvents().get(0).getStatus());
	}
}
