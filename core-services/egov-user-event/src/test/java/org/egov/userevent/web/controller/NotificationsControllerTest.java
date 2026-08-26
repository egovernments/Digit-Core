package org.egov.userevent.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.egov.common.contract.request.RequestInfo;
import org.egov.userevent.model.LastAccesDetails;
import org.egov.userevent.service.UserEventsService;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.NotificationCountResponse;
import org.egov.userevent.web.context.GatewayRequestInfoFactory;
import org.egov.userevent.web.context.HeaderNames;
import org.egov.userevent.web.error.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class NotificationsControllerTest {

	private static final String USER_UUID = "11111111-1111-1111-1111-111111111111";
	private static final String TENANT = "pb.amritsar";

	private UserEventsService service;
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		service = mock(UserEventsService.class);
		NotificationsController controller = new NotificationsController();
		ReflectionTestUtils.setField(controller, "service", service);
		ReflectionTestUtils.setField(controller, "requestInfoFactory", new GatewayRequestInfoFactory());
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new ApiExceptionHandler())
				.build();
	}

	@Test
	public void countReturnsTotalReadUnread() throws Exception {
		when(service.fetchCount(any(), any())).thenReturn(
				NotificationCountResponse.builder().totalCount(42L).unreadCount(12L).build());

		mockMvc.perform(post("/notification/_count")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(42))
				.andExpect(jsonPath("$.read").value(30))
				.andExpect(jsonPath("$.unread").value(12));
	}

	@Test
	public void countWithoutUserIdReturns400() throws Exception {
		mockMvc.perform(post("/notification/_count")
				.header(HeaderNames.TENANT_ID, TENANT))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].code").value(ErrorConstants.MISSING_ROLE_USERID_CODE));
	}

	@Test
	public void countWithFailedFetchReturns500ErrorArray() throws Exception {
		when(service.fetchCount(any(), any())).thenReturn(null);

		mockMvc.perform(post("/notification/_count")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$[0].code").value(ApiExceptionHandler.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void latUpdateEchoesSuppliedTime() throws Exception {
		when(service.persistLastAccessTime(any(RequestInfo.class), eq(1756200000000L))).thenReturn(
				LastAccesDetails.builder().userId(USER_UUID).lastAccessTime(1756200000000L).build());

		mockMvc.perform(post("/lat/_update")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"lastAccessTime\": 1756200000000}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(USER_UUID))
				.andExpect(jsonPath("$.lastAccessTime").value(1756200000000L));
	}

	@Test
	public void latUpdateWithoutBodyDefaultsToServerTime() throws Exception {
		when(service.persistLastAccessTime(any(RequestInfo.class), isNull())).thenReturn(
				LastAccesDetails.builder().userId(USER_UUID).lastAccessTime(999L).build());

		mockMvc.perform(post("/lat/_update")
				.header(HeaderNames.TENANT_ID, TENANT)
				.header(HeaderNames.USER_ID, USER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastAccessTime").value(999));
	}
}
