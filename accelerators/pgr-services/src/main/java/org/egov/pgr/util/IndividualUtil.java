package org.egov.pgr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.user.enums.UserType;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.User;
import org.egov.pgr.web.models.individual.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IndividualUtil {

	private ObjectMapper objectMapper;

	private ServiceRequestRepository serviceRequestRepository;

	private MultiStateInstanceUtil centralInstanceUtil;

	@Autowired
	public IndividualUtil(ObjectMapper objectMapper, ServiceRequestRepository serviceRequestRepository, MultiStateInstanceUtil centralInstanceUtil) {
		this.objectMapper = objectMapper;
		this.serviceRequestRepository = serviceRequestRepository;
		this.centralInstanceUtil = centralInstanceUtil;
	}

	public IndividualResponse createIndividual(IndividualRequest individualRequest, StringBuilder uri) {

		try {
			Object responseMap = serviceRequestRepository.fetchResult(uri, individualRequest);
			return objectMapper.convertValue(responseMap, IndividualResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in individual create call");
		}
	}

	public IndividualBulkResponse searchIndividual(IndividualSearchRequest individualSearchRequest, StringBuilder uri) {
		try {
			Object responseMap = serviceRequestRepository.fetchResult(uri, individualSearchRequest);
			return objectMapper.convertValue(responseMap, IndividualBulkResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in individual search call");
		}
	}

	public Individual buildIndividualFromUser(User user) {
		// Create an Individual object
		Individual individual = new Individual();

		// Set properties from User to Individual
		individual.setUserId(user.getId() != null ? user.getId().toString() : null);
		individual.setUserUuid(user.getUuid());
		individual.setTenantId(user.getTenantId());
		individual.setEmail(user.getEmailId());
		individual.setMobileNumber(user.getMobileNumber());
		individual.setIsSystemUserActive(user.getActive());

		individual.setName(buildNameFromUser(user.getName()));
		individual.setUserDetails(buildUserDetailsFromUser(user));

		return individual;
	}

	private Name buildNameFromUser(String name) {
		if (name == null || name.trim().isEmpty()) {
			return new Name(null, null, null);
		}

//		String[] nameParts = name.trim().split("\\s+"); // Splitting by one or more spaces
//		String givenName = nameParts[0]; // First word is the given name
//		String familyName = nameParts.length > 1
//				? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length)).trim()
//				: null; // Join the remaining words as the family name
//
//		return new Name(givenName, familyName, null);

		return Name.builder()
				.givenName(name)
				.build();
	}

	private UserDetails buildUserDetailsFromUser(User user) {
		UserDetails userDetails = new UserDetails();

		userDetails.setUsername(user.getUserName() != null ? user.getUserName() : null);
		userDetails.setTenantId(user.getTenantId() != null ? user.getTenantId() : null);
		userDetails.setUserType(user.getType() != null ? UserType.fromValue(user.getType()) : UserType.CITIZEN);

		// Handle null roles safely
		if (user.getRoles() != null && !user.getRoles().isEmpty()) {
			// Transform User.Role to UserDetails.Role
			userDetails.setRoles(user.getRoles().stream()
					.map(role -> new Role(
							role.getName(),
							role.getCode(),
							null,
							role.getTenantId()
					))
					.collect(Collectors.toList()));
		} else {
			userDetails.setRoles(Collections.emptyList()); // Set an empty list instead of null
		}

		return userDetails;
	}

	public User buildUserFromIndividual(Individual individual) {
		// Create a User object
		User user = new User();

		// Set properties from Individual to User
		user.setId(individual.getUserId() != null ? Long.valueOf(individual.getUserId()) : null);
		user.setUserName(individual.getUserDetails() != null && individual.getUserDetails().getUsername() != null
				? individual.getUserDetails().getUsername() : null);
		user.setName(individual.getName() != null
				? (individual.getName().getFamilyName() != null
				? String.join(" ", individual.getName().getGivenName(), individual.getName().getFamilyName())
				: individual.getName().getGivenName())
				: null);
		user.setType(individual.getUserDetails() != null && individual.getUserDetails().getUserType() != null
				? individual.getUserDetails().getUserType().toString() : "CITIZEN");
		user.setMobileNumber(individual.getMobileNumber());
		user.setEmailId(individual.getEmail());
		user.setTenantId(individual.getTenantId());
		user.setUuid(individual.getUserUuid());
		user.setActive(individual.getIsSystemUserActive());

		if (individual.getUserDetails() != null && individual.getUserDetails().getRoles() != null) {
			user.setRoles(individual.getUserDetails().getRoles().stream()
					.map(role -> new org.egov.common.contract.request.Role(
							null,
							role.getName(),
							role.getCode(),
							role.getTenantId()
					))
					.collect(Collectors.toList()));
		} else {
			user.setRoles(Collections.emptyList());
		}

		return user;
	}

	public String getStateLevelTenant(String tenantId) {
		/* return tenantId.split("\\.")[0];*/
		log.info("tenantId" + tenantId);
		return centralInstanceUtil.getStateLevelTenant(tenantId);
	}

	public void addIndividualDefaultFields(String mobileNumber, String tenantId, Individual individual) {

		UserDetails userDetails = new UserDetails();
		userDetails.setUsername(mobileNumber);
		userDetails.setTenantId(getStateLevelTenant(tenantId));
		userDetails.setUserType(UserType.CITIZEN);
		userDetails.setRoles(Collections.singletonList(getCitizenRole(tenantId)));

		individual.setUserDetails(userDetails);
		individual.setTenantId(getStateLevelTenant(tenantId));
		individual.setIsSystemUserActive(true);
	}

	private Role getCitizenRole(String tenantId) {
		Role role = new Role();
		role.setCode("CITIZEN");
		role.setName("Citizen");
		role.setTenantId(getStateLevelTenant(tenantId));
		return role;
	}

	public Individual buildIndividualFromUserInfo(org.egov.common.contract.request.User userInfo) {
		// Create an Individual object
		Individual individual = new Individual();

		// Set properties from User to Individual
		individual.setUserId(userInfo.getId() != null ? userInfo.getId().toString() : null);
		individual.setUserUuid(userInfo.getUuid());
		individual.setTenantId(userInfo.getTenantId());
		individual.setEmail(userInfo.getEmailId());
		individual.setMobileNumber(userInfo.getMobileNumber());

		individual.setName(buildNameFromUser(userInfo.getName()));
		individual.setUserDetails(buildUserDetailsFromUserInfo(userInfo));

		return individual;
	}

	private UserDetails buildUserDetailsFromUserInfo(org.egov.common.contract.request.User userInfo) {
		UserDetails userDetails = new UserDetails();

		userDetails.setUsername(userInfo.getUserName() != null ? userInfo.getUserName() : null);
		userDetails.setTenantId(userInfo.getTenantId() != null ? userInfo.getTenantId() : null);
		userDetails.setUserType(userInfo.getType() != null ? UserType.fromValue(userInfo.getType()) : UserType.CITIZEN);

		// Handle null roles safely
		if (userInfo.getRoles() != null && !userInfo.getRoles().isEmpty()) {
			// Transform User.Role to UserDetails.Role
			userDetails.setRoles(userInfo.getRoles().stream()
					.map(role -> new Role(
							role.getName(),
							role.getCode(),
							null,
							role.getTenantId()
					))
					.collect(Collectors.toList()));
		} else {
			userDetails.setRoles(Collections.emptyList()); // Set an empty list instead of null
		}

		return userDetails;
	}
}
