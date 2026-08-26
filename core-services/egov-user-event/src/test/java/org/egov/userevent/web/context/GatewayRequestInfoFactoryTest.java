package org.egov.userevent.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GatewayRequestInfoFactoryTest {

	private GatewayRequestInfoFactory factory;

	@BeforeEach
	public void setup() {
		factory = new GatewayRequestInfoFactory();
		ReflectionTestUtils.setField(factory, "objectMapper", new ObjectMapper());
	}

	/** Fabricates an unsigned JWT with the given payload JSON. */
	private String jwt(String payloadJson) {
		String header = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
		String payload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		return header + "." + payload + ".fake-signature";
	}

	private MockHttpServletRequest request(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HeaderNames.TENANT_ID, "pb.amritsar");
		request.addHeader(HeaderNames.USER_ID, "user-uuid-1");
		if (null != token) {
			request.addHeader(HeaderNames.AUTHORIZATION, "Bearer " + token);
		}
		return request;
	}

	@Test
	public void rolesComeFromRealmAccessClaimWithPlumbingFiltered() {
		// mirrors the sample Keycloak token: plumbing roles must be dropped
		String token = jwt("{\"realm_access\":{\"roles\":[\"default-roles-test9\",\"SUPERUSER\","
				+ "\"offline_access\",\"uma_authorization\",\"EMPLOYEE.ADMIN\"]},"
				+ "\"preferred_username\":\"someone@example.org\"}");

		RequestInfo requestInfo = factory.from(request(token), "EMPLOYEE");

		List<String> codes = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).toList();
		assertEquals(List.of("SUPERUSER", "EMPLOYEE.ADMIN"), codes);
		assertEquals(token, requestInfo.getAuthToken());
	}

	@Test
	public void missingTokenFallsBackToSynthesizedTypeRole() {
		RequestInfo requestInfo = factory.from(request(null), "CITIZEN");

		assertEquals(1, requestInfo.getUserInfo().getRoles().size());
		assertEquals("CITIZEN", requestInfo.getUserInfo().getRoles().get(0).getCode());
	}

	@Test
	public void malformedTokenFallsBackToSynthesizedTypeRole() {
		RequestInfo requestInfo = factory.from(request("not-a-jwt"), "EMPLOYEE");

		assertEquals(List.of("EMPLOYEE"),
				requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).toList());
	}

	@Test
	public void tokenWithoutRealmAccessFallsBack() {
		RequestInfo requestInfo = factory.from(request(jwt("{\"sub\":\"abc\"}")), "EMPLOYEE");

		assertEquals(List.of("EMPLOYEE"),
				requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).toList());
	}
}
