package org.egov.domain.service;

import org.egov.domain.exception.*;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.OtpRequestType;
import org.egov.domain.model.User;
import org.egov.persistence.repository.OtpEmailRepository;
import org.egov.persistence.repository.OtpRepository;
import org.egov.persistence.repository.OtpSMSRepository;
import org.egov.persistence.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OtpServiceTest {
	@Mock
	private OtpRepository otpRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OtpSMSRepository otpSMSRepository;

	@Mock
	private OtpEmailRepository otpEmailRepository;

	@Mock
	private OtpRequestValidator otpRequestValidator;

	private OtpService otpService;

	@Before
	public void setUp() {
		otpService = new OtpService(otpRepository, otpSMSRepository, otpEmailRepository,
				userRepository, otpRequestValidator);
	}

	@Test
	public void test_should_validate_otp_request_for_user_registration() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.REGISTER)
				.build();

		otpService.sendOtp(otpRequest);

		verify(otpRequestValidator).validate(otpRequest);
	}

	@Test
	public void test_should_validate_otp_request_for_user_login() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.LOGIN)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(new User(1L, "foo@bar.com", "123"));

		otpService.sendOtp(otpRequest);

		verify(otpRequestValidator).validate(otpRequest);
	}

	@Test(expected = UserAlreadyExistInSystemException.class)
	public void test_should_throwException_when_userAlreadyExist_IncaseOfRegister() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.REGISTER)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(new User(1L, "foo@bar.com", "123"));

		otpService.sendOtp(otpRequest);
	}

	@Test(expected = UserNotExistingInSystemException.class)
	public void test_should_throwException_when_userNotExist_IncaseOfLogin() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.LOGIN)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(null);

		otpService.sendOtp(otpRequest);
	}

	@Test
	public void test_should_validate_otp_request_for_password_reset() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(new User(1L, "foo@bar.com", "123"));

		otpService.sendOtp(otpRequest);

		verify(otpRequestValidator).validate(otpRequest);
	}

	@Test(expected = UserNotFoundException.class)
	public void test_should_throwException_when_user_not_found_for_password_reset() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(null);

		otpService.sendOtp(otpRequest);
	}

	@Test(expected = UserMobileNumberNotFoundException.class)
	public void test_should_throwException_when_mobilenumber_is_null() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(new User(1L, "foo@bar.com", null));

		otpService.sendOtp(otpRequest);
	}

	@Test(expected = UserMobileNumberNotFoundException.class)
	public void test_should_throwException_when_mobilenumber_is_empty() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.build();
		when(userRepository.fetchUser("1234567890", "tenant", null))
				.thenReturn(new User(1L, "foo@bar.com", ""));

		otpService.sendOtp(otpRequest);
	}

	@Test
	public void test_should_send_sms_otp_for_user_registration() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.REGISTER)
				.build();
		final String otpNumber = "otpNumber";
		when(otpRepository.fetchOtp(otpRequest)).thenReturn(otpNumber);

		otpService.sendOtp(otpRequest);

		verify(otpSMSRepository).send(otpRequest, otpNumber);
	}

	@Test
	public void test_should_send_sms_otp_for_password_reset() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.userType("CITIZEN")
				.build();
		final String otpNumber = "otpNumber";
		when(otpRepository.fetchOtp(otpRequest)).thenReturn(otpNumber);
		when(userRepository.fetchUser("1234567890", "tenant", "CITIZEN"))
				.thenReturn(new User(1L, "foo@bar.com", "1234"));

		otpService.sendOtp(otpRequest);

		verify(otpSMSRepository).send(otpRequest, otpNumber);
	}

	@Test
	public void test_should_send_email_otp_for_password_reset() {
		final OtpRequest otpRequest = OtpRequest.builder()
				.tenantId("tenant")
				.mobileNumber("1234567890")
				.type(OtpRequestType.PASSWORD_RESET)
				.userType("CITIZEN")
				.requestInfo(null)
				.build();
		final String otpNumber = "otpNumber";
		when(otpRepository.fetchOtp(otpRequest)).thenReturn(otpNumber);
		when(userRepository.fetchUser("1234567890", "tenant", "CITIZEN"))
				.thenReturn(new User(1L, "foo@bar.com", "123"));

		otpService.sendOtp(otpRequest);

		verify(otpEmailRepository).send("foo@bar.com", otpNumber, otpRequest);
	}
}
