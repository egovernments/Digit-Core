package org.egov.domain.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class OtpRequestTest {

	@Test
	public void test_should_return_true_when_tenant_id_is_not_present() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId(null).mobileNumber("mobile number").build();

		assertTrue(otpRequest.isTenantIdAbsent());
	}

	@Test
	public void test_should_return_true_when_mobile_number_is_not_present() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId("tenantId").mobileNumber(null).build();

		assertTrue(otpRequest.isMobileNumberAbsent());
	}

	@Test
	public void test_should_return_true_when_type_is_not_present() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId("tenantId").mobileNumber("mobileNumber").type(null)
				.build();

		assertTrue(otpRequest.isInvalidType());
	}

	@Test
	public void test_should_return_false_when_all_required_fields_present() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId("tenantId").mobileNumber("1234567890")
				.type(OtpRequestType.REGISTER).build();

		assertFalse(otpRequest.isTenantIdAbsent());
		assertFalse(otpRequest.isMobileNumberAbsent());
		assertFalse(otpRequest.isInvalidType());
	}

	@Test
	public void test_should_return_true_when_requesttype_login() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId("tenantId").mobileNumber("mobileNumber")
				.type(OtpRequestType.LOGIN).build();

		assertTrue(otpRequest.isLoginRequestType());
		assertFalse(otpRequest.isRegistrationRequestType());
	}

	@Test
	public void test_should_return_true_when_requesttype_register() {
		final OtpRequest otpRequest = OtpRequest.builder().tenantId("tenantId").mobileNumber("mobileNumber")
				.type(OtpRequestType.REGISTER).build();

		assertTrue(otpRequest.isRegistrationRequestType());
		assertFalse(otpRequest.isLoginRequestType());
	}

	@Test
	public void test_hasMdmsValidationError_returns_true_when_error_message_set() {
		final OtpRequest otpRequest = OtpRequest.builder().build();
		otpRequest.setMdmsValidationErrorMessage("Error message");

		assertTrue(otpRequest.hasMdmsValidationError());
	}

	@Test
	public void test_hasMdmsValidationError_returns_false_when_no_error_message() {
		final OtpRequest otpRequest = OtpRequest.builder().build();

		assertFalse(otpRequest.hasMdmsValidationError());
	}
}
