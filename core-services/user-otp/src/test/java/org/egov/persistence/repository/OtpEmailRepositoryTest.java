package org.egov.persistence.repository;

import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.domain.service.LocalizationService;
import org.egov.persistence.contract.Email;
import org.egov.persistence.contract.EmailRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.*;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OtpEmailRepositoryTest {

	private static final String EMAIL_TOPIC = "email.topic";
	@Mock
	private CustomKafkaTemplate<String, EmailRequest> kakfaTemplate;
	private OtpEmailRepository repository;
	@Mock
	private LocalizationService localizationService;

	@Before
	public void before() {
		repository = new OtpEmailRepository(kakfaTemplate, EMAIL_TOPIC, localizationService);
	}

	@Test
	public void test_should_not_send_email_when_email_address_is_not_present() {
		repository.send(null, "otpNumber", OtpRequest.builder().requestInfo(null).mobileNumber("9543")
				.tenantId("pb.amritsar").type(OtpRequestType.PASSWORD_RESET).userType("CITIZEN").build());

		verify(kakfaTemplate, never()).send(any(), any());
	}

//	@Test
//	public void test_should_send_email_message() {
//		final Email expectedEmail = Email.builder()
//				.subject("Password Reset")
//				.body("Your OTP for recovering password is otpNumber.")
//				.emailTo(Collections.singleton("foo@bar.com"))
//				.build();
//		final EmailRequest expectedEmailRequest =
//				EmailRequest.builder().requestInfo(RequestInfo.builder().build()).email(expectedEmail).build();
//
//		Map<String, String> map = new HashMap<>();
//		map.put("email.pwd.reset.otp.body", "Your otp for recovering password is %s.");
//		map.put("email.pwd.reset.otp.sub", "Password Reset");
//		when(localizationService.getLocalisedMessages("pb.amritsar", "en_IN","egov-user")).thenReturn(map);
//
//		repository.send("foo@bar.com", "otpNumber", OtpRequest.builder().requestInfo(null).mobileNumber("9543")
//				.tenantId("pb.amritsar").type(OtpRequestType.PASSWORD_RESET).userType("CITIZEN").build());
//
//		verify(kakfaTemplate).send(EMAIL_TOPIC, expectedEmailRequest);
//	}

}