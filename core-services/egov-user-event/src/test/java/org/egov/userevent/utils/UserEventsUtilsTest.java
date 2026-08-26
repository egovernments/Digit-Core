package org.egov.userevent.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.egov.userevent.model.RecepientEvent;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.egov.userevent.web.contract.Recepient;
import org.junit.jupiter.api.Test;

public class UserEventsUtilsTest {

	private final UserEventsUtils utils = new UserEventsUtils();

	@Test
	public void dotlessJwtRoleIsNormalizedInSearchExpansion() {
		EventSearchCriteria criteria = new EventSearchCriteria();
		criteria.setTenantId("pb.amritsar");
		criteria.setRoles(List.of("SUPERUSER"));

		utils.buildRecepientListForSearch(criteria); // must not throw on dotless roles

		assertTrue(criteria.getRecepients().contains("SUPERUSER|SUPERUSER|pb.amritsar"));
		assertTrue(criteria.getRecepients().contains("SUPERUSER|*|*"));
		assertTrue(criteria.getRecepients().contains("*|SUPERUSER|*"));
		assertTrue(criteria.getRecepients().contains("All"));
	}

	@Test
	public void legacyDottedRoleExpansionIsUnchanged() {
		EventSearchCriteria criteria = new EventSearchCriteria();
		criteria.setTenantId("pb.amritsar");
		criteria.setRoles(List.of("CITIZEN.CITIZEN"));

		utils.buildRecepientListForSearch(criteria);

		assertTrue(criteria.getRecepients().contains("CITIZEN|CITIZEN|pb.amritsar"));
		assertTrue(criteria.getRecepients().contains("CITIZEN|*|*"));
	}

	@Test
	public void dotlessRoleWritesNormalizedRegistryRow() {
		Event event = Event.builder()
				.id("event-1")
				.tenantId("pb.amritsar")
				.recepient(Recepient.builder().toRoles(new ArrayList<>(List.of("SUPERUSER"))).build())
				.build();
		List<RecepientEvent> rows = new ArrayList<>();

		utils.manageRecepients(event, rows);

		assertEquals(1, rows.size());
		assertEquals("SUPERUSER|SUPERUSER|pb.amritsar", rows.get(0).getRecepient());
	}
}
