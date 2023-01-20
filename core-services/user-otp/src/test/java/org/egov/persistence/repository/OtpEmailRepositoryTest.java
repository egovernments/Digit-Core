package org.egov.persistence.repository;

import org.egov.common.contract.request.RequestInfo;
import org.egov.domain.model.OtpRequestType;
import org.egov.persistence.contract.Email;
import org.egov.persistence.contract.EmailRequest;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OtpEmailRepositoryTest {

	private static final String EMAIL_TOPIC = "email.topic";
	@Mock
	private CustomKafkaTemplate<String, EmailRequest> kakfaTemplate;
	private OtpEmailRepository repository;

	@Before
	public void before() {
		repository = new OtpEmailRepository(kakfaTemplate, EMAIL_TOPIC);
	}

	@Test
	public void test_should_not_send_email_when_email_address_is_not_present() {
		repository.send(null, "otpNumber", OtpRequestType.PASSWORD_RESET);

		verify(kakfaTemplate, never()).send(any(), any());
	}

	@Test
	public void test_should_send_email_message() {
		final Email expectedEmail = Email.builder()
				.subject("Password Reset")
				.body("Your OTP for recovering password is otpNumber.")
				.emailTo(Collections.singleton("foo@bar.com"))
				.build();
		final EmailRequest expectedEmailRequest =
				EmailRequest.builder().requestInfo(RequestInfo.builder().build()).email(expectedEmail).build();

		repository.send("foo@bar.com", "otpNumber",OtpRequestType.PASSWORD_RESET);

		verify(kakfaTemplate).send(EMAIL_TOPIC, expectedEmailRequest);
	}

}