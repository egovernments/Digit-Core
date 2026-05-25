package org.egov.handler.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.repository.ServiceRequestRepository;
import org.egov.handler.web.models.Otp;
import org.egov.handler.web.models.OtpRequest;
import org.egov.handler.web.models.OtpResponse;
import org.egov.handler.web.models.TenantRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

import static org.egov.handler.constants.OtpConstants.OTP_TYPE_LOGIN;
import static org.egov.handler.constants.UserConstants.USER_TYPE_EMPLOYEE;

@Component
public class OtpUtil {

	@Autowired
	private ServiceConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	public OtpResponse sendOtp(TenantRequest tenantRequest){

		StringBuilder uri = new StringBuilder(config.getUserOtpHost())
				.append(config.getUserOtpSendEndpoint());

		Otp otp = Otp.builder().userName(tenantRequest.getTenant().getEmail())
				.type(OTP_TYPE_LOGIN)
				.tenantId(tenantRequest.getTenant().getCode())
				.userType(USER_TYPE_EMPLOYEE)
				.build();
		OtpRequest otpRequest = OtpRequest.builder().otp(otp).requestInfo(tenantRequest.getRequestInfo()).build();

		try{
			LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, otpRequest);
			OtpResponse otpResponse = mapper.convertValue(responseMap,OtpResponse.class);
			return otpResponse;
		}
		catch(IllegalArgumentException  e)
		{
			throw new CustomException("USER_CREATION_FAILED","Failed to create user for the tenant");
		}

	}
}

