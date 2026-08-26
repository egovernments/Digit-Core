package org.egov.userevent.web.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.egov.userevent.model.AuditDetails;
import org.egov.userevent.model.Document;
import org.egov.userevent.model.enums.Source;
import org.egov.userevent.model.enums.Status;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventDetails;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.egov.userevent.web.contract.Recepient;
import org.egov.userevent.web.contract.v3.ActionItemV3;
import org.egov.userevent.web.contract.v3.ActionV3;
import org.egov.userevent.web.contract.v3.EventDetailsV3;
import org.egov.userevent.web.contract.v3.EventStatusV3;
import org.egov.userevent.web.contract.v3.EventV3;
import org.junit.jupiter.api.Test;

public class EventApiMapperTest {

	private final EventApiMapper mapper = new EventApiMapper();

	@Test
	public void toInternalInjectsTenantAndDefaults() {
		EventV3 api = EventV3.builder()
				.eventType("BROADCAST")
				.description("Water supply downtime on Friday")
				.status(EventStatusV3.CANCELED)
				.toRoles(List.of("CITIZEN.CITIZEN"))
				.toUsers(List.of("11111111-1111-1111-1111-111111111111"))
				.action(ActionV3.builder()
						.actionUrls(List.of(ActionItemV3.builder().actionUrl("/some/url").code("REOPEN").build()))
						.build())
				.eventDetails(EventDetailsV3.builder()
						.fromDate(100L).toDate(200L)
						.latitude(12.34).longitude(56.78)
						.documents(List.of("fs-1", "fs-2"))
						.build())
				.build();

		Event internal = mapper.toInternal(api, "pb.amritsar");

		assertEquals("pb.amritsar", internal.getTenantId());
		assertEquals(Status.CANCELLED, internal.getStatus());
		assertEquals(Source.WEBAPP, internal.getSource());
		assertEquals("Water supply downtime on Friday", internal.getName());
		assertEquals(List.of("CITIZEN.CITIZEN"), internal.getRecepient().getToRoles());
		assertEquals("pb.amritsar", internal.getActions().getTenantId());
		assertEquals("REOPEN", internal.getActions().getActionUrls().get(0).getCode());
		assertEquals(BigDecimal.valueOf(12.34), internal.getEventDetails().getLatitude());
		assertEquals("fs-1", internal.getEventDetails().getDocuments().get(0).getFileStoreId());
	}

	@Test
	public void toInternalDerivesNameWithinInternalLimit() {
		String longDescription = "x".repeat(300);
		Event internal = mapper.toInternal(
				EventV3.builder().eventType("BROADCAST").description(longDescription).build(), "pb");
		assertTrue(internal.getName().length() <= 65);
		assertEquals(longDescription, internal.getDescription());
	}

	@Test
	public void toInternalLeavesRecepientNullWhenNoAddressing() {
		Event internal = mapper.toInternal(
				EventV3.builder().eventType("BROADCAST").description("something").build(), "pb");
		assertNull(internal.getRecepient());
	}

	@Test
	public void toApiDropsInternalFieldsAndMapsAudit() {
		Event internal = Event.builder()
				.id("abc")
				.tenantId("pb.amritsar")
				.eventType("SYSTEMGENERATED")
				.name("internal name")
				.description("desc")
				.status(Status.CANCELLED)
				.source(Source.MOBILEAPP)
				.postedBy("someone")
				.referenceId("ref-1")
				.recepient(Recepient.builder().toRoles(List.of("EMPLOYEE.ADMIN")).build())
				.eventDetails(EventDetails.builder()
						.latitude(BigDecimal.valueOf(1.5)).longitude(BigDecimal.valueOf(2.5))
						.documents(List.of(Document.builder().fileStoreId("fs-9").build()))
						.build())
				.auditDetails(AuditDetails.builder()
						.createdBy("creator").createdTime(1L)
						.lastModifiedBy("modifier").lastModifiedTime(2L)
						.build())
				.build();

		EventV3 api = mapper.toApi(internal);

		assertEquals("abc", api.getId());
		assertEquals(EventStatusV3.CANCELED, api.getStatus());
		assertEquals(List.of("EMPLOYEE.ADMIN"), api.getToRoles());
		assertEquals(1.5, api.getEventDetails().getLatitude());
		assertEquals(List.of("fs-9"), api.getEventDetails().getDocuments());
		assertEquals("modifier", api.getAuditDetail().getModifiedBy());
		assertEquals(Long.valueOf(2L), api.getAuditDetail().getModifiedTime());
	}

	@Test
	public void toSearchCriteriaMapsNamesAndClampsLimit() {
		EventSearchCriteria criteria = mapper.toSearchCriteria("pb", List.of("id1"), EventStatusV3.CANCELED,
				null, List.of("BROADCAST"), null, List.of("user-1"), null, 10L, 20L, 500, 5);

		assertEquals("pb", criteria.getTenantId());
		assertEquals(List.of("user-1"), criteria.getUserids());
		assertEquals(List.of("CANCELLED"), criteria.getStatus());
		assertEquals(Integer.valueOf(200), criteria.getLimit());
		assertEquals(Integer.valueOf(5), criteria.getOffset());
	}

	@Test
	public void statusRoundTrip() {
		assertEquals(Status.ACTIVE, mapper.toInternalStatus(EventStatusV3.ACTIVE));
		assertEquals(Status.CANCELLED, mapper.toInternalStatus(EventStatusV3.CANCELED));
		assertEquals(EventStatusV3.CANCELED, mapper.toApiStatus(Status.CANCELLED));
		assertEquals(EventStatusV3.INACTIVE, mapper.toApiStatus(Status.INACTIVE));
		assertNull(mapper.toInternalStatus(null));
		assertNull(mapper.toApiStatus(null));
	}
}
