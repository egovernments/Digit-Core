package org.egov.userevent.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.userevent.model.AuditDetails;
import org.egov.userevent.service.UserEventsService;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventResponse;
import org.egov.userevent.model.enums.Source;
import org.egov.userevent.model.enums.Status;
import org.egov.userevent.web.context.GatewayRequestInfoFactory;
import org.egov.userevent.web.context.HeaderNames;
import org.egov.userevent.web.error.ApiExceptionHandler;
import org.egov.userevent.web.filter.ResponseHeaderFilter;
import org.egov.userevent.web.mapper.EventApiMapper;
import org.egov.userevent.web.mapper.EventUpdateMerger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserEventsControllerTest {

	private static final String USER_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String TENANT = "pb.amritsar";

	private UserEventsService service;
	private EventUpdateMerger merger;
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		service = mock(UserEventsService.class);
		merger = mock(EventUpdateMerger.class);
		UserEventsController controller = new UserEventsController();
		ReflectionTestUtils.setField(controller, "service", service);
		ReflectionTestUtils.setField(controller, "mapper", new EventApiMapper());
		ReflectionTestUtils.setField(controller, "updateMerger", merger);
		ReflectionTestUtils.setField(controller, "requestInfoFactory", new GatewayRequestInfoFactory());
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new ApiExceptionHandler())
				.addFilters(new ResponseHeaderFilter())
				.setValidator(validator)
				.build();
	}

	private EventResponse stubbedResponse() {
		Event event = Event.builder()
				.id("event-1")
				.tenantId(TENANT)
				.eventType("BROADCAST")
				.name("some name")
				.description("some description")
				.status(Status.ACTIVE)
				.source(Source.WEBAPP)
				.auditDetails(AuditDetails.builder().createdBy(USER_UUID).createdTime(1L).build())
				.build();
		return EventResponse.builder().events(List.of(event)).build();
	}

	private String validBody() {
		return """
				{"events":[{"eventType":"BROADCAST","description":"Water supply downtime on Friday",
				"toRoles":["All"],"eventDetails":{"fromDate":1790000000000,"toDate":1790086400000}}]}
				""";
	}

	@Test
	public void createReturns201WithBareArrayAndHeaders() throws Exception {
		when(service.createEvents(any(), anyBoolean())).thenReturn(stubbedResponse());

		mockMvc.perform(post("/v1/events/_create")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID)
				.header(HeaderNames.REQUEST_ID, "req-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].id").value("event-1"))
				.andExpect(jsonPath("$[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$[0].tenantId").doesNotExist())
				.andExpect(jsonPath("$[0].name").doesNotExist())
				.andExpect(header().exists(HeaderNames.RESPONSE_TIME))
				.andExpect(header().exists(HeaderNames.RESPONSE_TIMESTAMP))
				.andExpect(header().string(HeaderNames.REQUEST_ID, "req-1"))
				.andExpect(header().string(HeaderNames.TENANT_ID, TENANT));
	}

	@Test
	public void createWithoutTenantHeaderReturns400ErrorArray() throws Exception {
		mockMvc.perform(post("/v1/events/_create")
				.header(HeaderNames.USER_ID, USER_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validBody()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].code").value(ApiExceptionHandler.MISSING_HEADER));
	}

	@Test
	public void createWithoutUserIdReturns400() throws Exception {
		mockMvc.perform(post("/v1/events/_create")
				.header(HeaderNames.TENANT_ID, TENANT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validBody()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].code").value(ErrorConstants.MISSING_ROLE_USERID_CODE));
	}

	@Test
	public void createWithEmptyEventsReturns400() throws Exception {
		mockMvc.perform(post("/v1/events/_create")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"events\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].code").value(ApiExceptionHandler.INVALID_REQUEST));
	}

	@Test
	public void updateOfUnknownEventsReturns404() throws Exception {
		when(service.updateEvents(any())).thenThrow(new CustomException(
				ErrorConstants.MEN_UPDATE_MISSING_EVENTS_CODE, ErrorConstants.MEN_UPDATE_MISSING_EVENTS_MSG));

		mockMvc.perform(post("/v1/events/_update")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"events\":[{\"id\":\"nope\",\"eventType\":\"BROADCAST\",\"description\":\"anything\",\"status\":\"CANCELED\"}]}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$[0].code").value(ErrorConstants.MEN_UPDATE_MISSING_EVENTS_CODE));
	}

	@Test
	public void searchReturnsBareArray() throws Exception {
		when(service.searchEvents(any(), any(), anyBoolean())).thenReturn(stubbedResponse());

		mockMvc.perform(post("/v1/events/_search?eventTypes=BROADCAST&status=ACTIVE&limit=50&offset=0")
				.header(HeaderNames.TENANT_ID, TENANT))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].eventType").value("BROADCAST"));
	}

	@Test
	public void searchWithInvalidStatusReturns400() throws Exception {
		mockMvc.perform(post("/v1/events/_search?status=CANCELLED")
				.header(HeaderNames.TENANT_ID, TENANT))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].code").value(ApiExceptionHandler.INVALID_REQUEST));
	}

	@Test
	public void searchLimitAboveMaxReturns400() throws Exception {
		mockMvc.perform(post("/v1/events/_search?limit=201")
				.header(HeaderNames.TENANT_ID, TENANT))
				.andExpect(status().isBadRequest());
	}
}
