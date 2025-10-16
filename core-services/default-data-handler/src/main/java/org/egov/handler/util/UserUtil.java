package org.egov.handler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.repository.ServiceRequestRepository;
import org.egov.handler.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.egov.handler.constants.UserConstants.DEFAULT_ROOT_USER_ROLE;
import static org.egov.handler.constants.UserConstants.USER_TYPE_EMPLOYEE;

@Slf4j
@Component
public class UserUtil {

	private final ServiceConfiguration serviceConfig;

	private final ObjectMapper mapper;

	private final ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public UserUtil(ServiceConfiguration serviceConfig, ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository) {
		this.serviceConfig = serviceConfig;
		this.mapper = mapper;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	/**
	 * Creates admin user for the tenant
	 *
	 * @return
	 */
	public User createUser(TenantRequest tenantRequest) {

		StringBuilder uri = new StringBuilder(serviceConfig.getUserHost())
				.append(serviceConfig.getUserContextPath())
				.append(serviceConfig.getUserCreateEndpoint());

		Tenant tenant = tenantRequest.getTenant();

		Role role = Role.builder().tenantId(tenant.getCode()).code(DEFAULT_ROOT_USER_ROLE).build();
		User user = User.builder()
				.tenantId(tenant.getCode())
				.userName(tenant.getEmail())
				.emailId(tenant.getEmail())
				.mobileNumber("9999999999")
				.active(true)
				.name(tenant.getName())
				.type(USER_TYPE_EMPLOYEE)
				.roles(Collections.singletonList(role))
				.build();

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUser(user);
		createUserRequest.setRequestInfo(tenantRequest.getRequestInfo());

		UserDetailResponse userDetailResponse = userCall(createUserRequest, uri);

		return userDetailResponse.getUser().get(0);

	}

	public UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;

		if (uri.toString().contains(serviceConfig.getUserSearchEndpoint()) || uri.toString().contains(serviceConfig.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(serviceConfig.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("USER_CREATION_FAILED", "Failed to create user for the tenant");
		}
	}

	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
						map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
						if ((String) map.get("lastModifiedDate") != null)
							map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
						if ((String) map.get("dob") != null)
							map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
						if ((String) map.get("pwdExpiryDate") != null)
							map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
					}
			);
		}
	}

	/**
	 * Converts date to long
	 *
	 * @param date   date to be parsed
	 * @param format Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			throw new CustomException("INVALID_DATE_FORMAT", "Failed to parse date format in user");
		}
		return d.getTime();
	}

}

