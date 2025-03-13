package org.egov.wf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.ServiceRequestRepository;
import org.egov.wf.web.models.individual.Individual;
import org.egov.wf.web.models.individual.IndividualBulkResponse;
import org.egov.wf.web.models.individual.IndividualSearch;
import org.egov.wf.web.models.individual.IndividualSearchRequest;
import org.egov.wf.web.models.user.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndividualService implements UserAdapter {

	private WorkflowConfig config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;


	@Autowired
	public IndividualService(WorkflowConfig config, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	@Override
	public Map<String, User> searchUser(RequestInfo requestInfo, List<String> uuids) {
		IndividualSearch individualSearch = new IndividualSearch();
		individualSearch.setUserUuid(uuids);

		IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
		individualSearchRequest.setRequestInfo(requestInfo);
		individualSearchRequest.setIndividual(individualSearch);

		StringBuilder url = getIndividualSearchUrl(requestInfo.getUserInfo().getTenantId(), config.getIndividualSearchLimit(), config.getIndividualSearchOffset());
		IndividualBulkResponse individualBulkResponse = searchIndividual(individualSearchRequest, url);

		if (CollectionUtils.isEmpty(individualBulkResponse.getIndividual()))
			throw new CustomException("INVALID USER", "No user found for the uuids: " + uuids);
		Map<String, User> idToUserMap = new HashMap<>();
		individualBulkResponse.getIndividual().forEach(individual -> {
			idToUserMap.put(individual.getUserUuid(), buildUserFromIndividual(individual));
		});
		return idToUserMap;
	}

	@Override
	public List<String> searchUserUuidsBasedOnRoleCodes(UserSearchRequest userSearchRequest) {
		IndividualSearch individualSearch = new IndividualSearch();
		individualSearch.setRoleCodes(userSearchRequest.getRoleCodes());

		IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
		individualSearchRequest.setRequestInfo(userSearchRequest.getRequestInfo());
		individualSearchRequest.setIndividual(individualSearch);

		StringBuilder url = getIndividualSearchUrl(userSearchRequest.getRequestInfo().getUserInfo().getTenantId(), config.getIndividualSearchLimit(), config.getIndividualSearchOffset());
		IndividualBulkResponse individualBulkResponse = searchIndividual(individualSearchRequest, url);

		if (CollectionUtils.isEmpty(individualBulkResponse.getIndividual()))
			throw new CustomException("INVALID USER", "No user found for the roleCodes: " + userSearchRequest.getRoleCodes());
		List<String> roleSpecificUsersUuids = new ArrayList<>();
		individualBulkResponse.getIndividual().forEach(individual -> {
			roleSpecificUsersUuids.add(individual.getUserUuid());
		});
		// ############ REMOVE ME LATER
		log.info(roleSpecificUsersUuids.toString());
		// ############################
		return roleSpecificUsersUuids;
	}

	private StringBuilder getIndividualSearchUrl(String tenantId, Integer limit, Integer offset) {
		return new StringBuilder(config.getIndividualHost())
				.append(config.getIndividualSearchEndpoint())
				.append("?tenantId=").append(tenantId)
				.append("&limit=").append(limit)
				.append("&offset=").append(offset);
	}

	public IndividualBulkResponse searchIndividual(IndividualSearchRequest individualSearchRequest, StringBuilder uri) {
		try {
			Object responseMap = serviceRequestRepository.fetchResult(uri, individualSearchRequest);
			return mapper.convertValue(responseMap, IndividualBulkResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in individual search call");
		}
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
}
