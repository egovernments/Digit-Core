package org.egov.access.web.filter;

import org.egov.access.TestConfiguration;
import org.egov.access.domain.service.ActionService;
import org.egov.access.web.contract.factory.ResponseInfoFactory;
import org.egov.access.web.controller.ActionController;
import org.egov.tracer.ExceptionAdvise;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(ActionController.class)
@ImportAutoConfiguration(RestTemplateAutoConfiguration.class)
@Import({TestConfiguration.class, ExceptionAdvise.class, SecurityHeadersFilter.class})
class XssReflectionTest {

	private static final String PAYLOAD = "{\"tenantId\":\"chaduat\",\"roleCodes\":[\"SUPERUSER\"],\"actionMaster\":\"actions-test\","
			+ "\"enabled\":true,\"RequestInfo\":{\"apiId\":\"Rainmaker\",\"userInfo\":{\"id\":\"135ta8ws<script>alert(1)<\\/script>whlq4\","
			+ "\"uuid\":\"7ca04d28-90f5-4259-a341-e07d99e08e18\",\"tenantId\":\"chaduat\"},\"msgId\":\"1|en_CHADUAT\"}}";

	@MockBean
	private ActionService actionService;

	@MockBean
	private ResponseInfoFactory responseInfoFactory;

	@MockBean
	private ErrorQueueProducer errorQueueProducer;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void scriptInUserInfoIdIsNotReflectedAndResponseIsHardened() throws Exception {
		mockMvc.perform(post("/v1/actions/mdms/_get").contentType(MediaType.APPLICATION_JSON).content(PAYLOAD))
				.andExpect(content().string(not(containsString("<script>"))))
				.andExpect(content().string(not(containsString("alert(1)"))))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"));
	}
}
