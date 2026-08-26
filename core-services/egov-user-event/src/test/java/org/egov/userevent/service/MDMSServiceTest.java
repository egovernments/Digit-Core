package org.egov.userevent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.userevent.config.PropertiesManager;
import org.egov.userevent.repository.RestCallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

public class MDMSServiceTest {

	private RestCallRepository restCallRepository;
	private MDMSService service;

	@BeforeEach
	public void setup() {
		restCallRepository = mock(RestCallRepository.class);
		PropertiesManager props = new PropertiesManager();
		props.setMdmsHost("https://test-lts.digit.org");
		props.setMdmsV2Endpoint("/mdms-v2/v2");
		props.setMdmsClientId("test-tenant");
		service = new MDMSService();
		ReflectionTestUtils.setField(service, "repository", restCallRepository);
		ReflectionTestUtils.setField(service, "props", props);
	}

	private Object v2Response() {
		return Map.of("mdms", List.of(
				Map.of("schemaCode", "mseva.EventTypes", "uniqueIdentifier", "BROADCAST",
						"isActive", true, "data", Map.of("code", "BROADCAST", "active", true)),
				Map.of("schemaCode", "mseva.EventTypes", "uniqueIdentifier", "EVENTSONGROUND",
						"isActive", true, "data", Map.of("code", "EVENTSONGROUND", "active", true)),
				Map.of("schemaCode", "mseva.EventTypes", "uniqueIdentifier", "OLDTYPE",
						"isActive", false, "data", Map.of("code", "OLDTYPE", "active", false)),
				Map.of("schemaCode", "mseva.EventCategories", "uniqueIdentifier", "PUBLICHEALTH",
						"isActive", true, "data", Map.of("code", "PUBLICHEALTH", "active", true))));
	}

	@Test
	public void fetchesActiveCodesPerSchemaAndForwardsHeaders() {
		when(restCallRepository.fetchResultWithHeaders(any(), eq("pb"), eq("test-tenant"), eq("jwt-token")))
				.thenReturn(Optional.of(v2Response()));

		RequestInfo requestInfo = RequestInfo.builder().authToken("jwt-token").build();
		Map<String, List<String>> masters = service.fetchEventMasters(requestInfo, "pb.amritsar");

		assertEquals(List.of("BROADCAST", "EVENTSONGROUND"), masters.get("EventTypes"));
		assertEquals(List.of("PUBLICHEALTH"), masters.get("EventCategories"));
		assertTrue(!masters.get("EventTypes").contains("OLDTYPE"), "inactive records must be excluded");

		ArgumentCaptor<StringBuilder> uriCaptor = ArgumentCaptor.forClass(StringBuilder.class);
		verify(restCallRepository).fetchResultWithHeaders(uriCaptor.capture(), eq("pb"), eq("test-tenant"),
				eq("jwt-token"));
		assertEquals("https://test-lts.digit.org/mdms-v2/v2?schemaCodes=mseva.EventTypes,mseva.EventCategories",
				uriCaptor.getValue().toString());
	}

	@Test
	public void absentResponseThrowsMdmsError() {
		when(restCallRepository.fetchResultWithHeaders(any(), any(), any(), any())).thenReturn(Optional.empty());

		CustomException ex = assertThrows(CustomException.class,
				() -> service.fetchEventMasters(RequestInfo.builder().build(), "pb"));
		assertEquals("MEN_ERROR_FROM_MDMS", ex.getCode());
	}

	@Test
	public void emptyMdmsListYieldsEmptyMasters() {
		when(restCallRepository.fetchResultWithHeaders(any(), any(), any(), any()))
				.thenReturn(Optional.of(Map.of("mdms", List.of())));

		Map<String, List<String>> masters = service.fetchEventMasters(RequestInfo.builder().build(), "pb");

		assertTrue(masters.get("EventTypes").isEmpty());
		assertTrue(masters.get("EventCategories").isEmpty());
	}
}
