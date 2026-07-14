package org.egov.access.domain.model;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ActionTest {

	@Test
	public void testShouldCheckEqualAndHashCodeForObjects() {
		Action action1 = Action.builder().id(1L).name("Create Complaint").url("/createcomplaint")
				.displayName("Create Complaint").serviceCode("test").build();
		Action action2 = Action.builder().id(1L).name("Create Complaint").url("/createcomplaint")
				.displayName("Create Complaint").serviceCode("test").build();

		assertTrue(action1.equals(action2));
		assertEquals(action1.hashCode(), action2.hashCode());
	}

	@Test
	public void testShouldCheckNotEqualAndHashCodeForObjects() {
		Action action1 = Action.builder().id(1L).name("Create Complaint").url("/createcomplaint")
				.displayName("Create Complaint").serviceCode("test").build();
		Action action2 = Action.builder().id(2L).name("Update Complaint").url("/updatecomplaint")
				.displayName("Update Complaint").serviceCode("test").build();

		assertFalse(action1.equals(action2));
		assertNotEquals(action1.hashCode(), action2.hashCode());
	}

	@Test
	public void testShouldDefaultPolicyFieldsToNullForLegacyActions() {
		Action action = Action.builder().id(1L).name("Create Complaint").url("/createcomplaint").build();

		assertNull(action.getMethod());
		assertNull(action.getResource());
		assertNull(action.getCondition());
	}

	@Test
	public void testShouldBuildAndReadOptionalPolicyFields() {
		Object condition = Map.of("==", List.of(Map.of("var", "user.type"), "CITIZEN"));

		Action action = Action.builder().id(1L).name("Search Complaint").url("/pgr-services/v2/request/_search")
				.method("POST").resource(List.of("complaint")).condition(condition).build();

		assertEquals("POST", action.getMethod());
		assertEquals(List.of("complaint"), action.getResource());
		assertEquals(condition, action.getCondition());
	}

	@Test
	public void testShouldIncludePolicyFieldsInEqualsAndHashCode() {
		Object condition = Map.of("==", List.of(1, 1));
		Action action1 = Action.builder().id(1L).name("Search").url("/search").method("POST")
				.resource(List.of("complaint")).condition(condition).build();
		Action action2 = Action.builder().id(1L).name("Search").url("/search").method("GET")
				.resource(List.of("complaint")).condition(condition).build();

		assertFalse(action1.equals(action2));
		assertNotEquals(action1.hashCode(), action2.hashCode());
	}
}
