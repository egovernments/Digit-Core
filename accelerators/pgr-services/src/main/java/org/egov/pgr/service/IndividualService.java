package org.egov.pgr.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.util.IndividualUtil;
import org.egov.pgr.web.models.RequestSearchCriteria;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.ServiceWrapper;
import org.egov.pgr.web.models.User;
import org.egov.pgr.web.models.individual.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;

@Service
public class IndividualService {

	private IndividualUtil individualUtil;

	private PGRConfiguration config;

	@Autowired
	public IndividualService(IndividualUtil individualUtil, PGRConfiguration config) {
		this.individualUtil = individualUtil;
		this.config = config;
	}

	/**
	 * Calls user service to enrich user from search or upsert user
	 *
	 * @param request
	 */
	public void callIndividualService(ServiceRequest request) {

		if (!StringUtils.isEmpty(request.getService().getAccountId()))
			enrichUser(request);
		else if (request.getService().getCitizen() != null)
			upsertUser(request);
	}

	/**
	 * Calls user search to fetch the list of user and enriches it in serviceWrappers
	 *
	 * @param serviceWrappers
	 */
	public void enrichIndividuals(RequestInfo requestInfo, List<ServiceWrapper> serviceWrappers) {

		// Create a map of tenantId to List of UUIDs
		Map<String, List<String>> tenantToUuidsMap = serviceWrappers.stream()
				.collect(Collectors.groupingBy(
						serviceWrapper -> serviceWrapper.getService().getTenantId(), // Group by tenantId
						Collectors.mapping(serviceWrapper -> serviceWrapper.getService().getAccountId(), Collectors.toList()) // Collect UUIDs
				));

		// Create a map to hold all Users
		Map<String, User> idToUserMap = new HashMap<>();

		// Iterate over each tenantId and fetch users
		for (Map.Entry<String, List<String>> entry : tenantToUuidsMap.entrySet()) {
			String tenantId = entry.getKey();
			List<String> individualIds = entry.getValue();

			Map<String, User> usersForTenant = searchBulkIndividual(requestInfo, tenantId, individualIds);

			if (usersForTenant != null) {
				idToUserMap.putAll(usersForTenant);
			}
		}

		serviceWrappers.forEach(serviceWrapper -> {
			serviceWrapper.getService().setCitizen(idToUserMap.get(serviceWrapper.getService().getAccountId()));
		});
	}


	/**
	 * Creates or updates the user based on if the user exists. The user existance is searched based on userName = mobileNumber
	 * If the there is already a user with that mobileNumber, the existing user is updated
	 *
	 * @param request
	 */
	private void upsertUser(ServiceRequest request) {

		User user = request.getService().getCitizen();
		Individual individual = individualUtil.buildIndividualFromUser(user);
		String tenantId = request.getService().getTenantId();
		Individual individualServiceResponse = null;

		// Search on mobile number as user name
		IndividualBulkResponse individualBulkResponse = searchIndividual(request.getRequestInfo(), individualUtil.getStateLevelTenant(tenantId), null, user.getMobileNumber());
		if (!individualBulkResponse.getIndividual().isEmpty()) {
			Individual individualFromSearch = individualBulkResponse.getIndividual().get(0);
			if (isNameDifferent(individual.getName(), individualFromSearch.getName())) {
				individualServiceResponse = updateIndividual(request.getRequestInfo(), individual, individualFromSearch);
			} else individualServiceResponse = individualBulkResponse.getIndividual().get(0);
		} else {
			individualServiceResponse = createIndividual(request.getRequestInfo(), tenantId, individual);
		}

		request.getService().setAccountId(individualServiceResponse.getIndividualId());
	}

	/**
	 * Checks if two names are different, handling null cases safely.
	 */
	private boolean isNameDifferent(Name name1, Name name2) {
		if (name1 == null || name2 == null) return true; // If either is null, consider them different

		return !safeEqualsIgnoreCase(name1.getGivenName(), name2.getGivenName()) ||
				!safeEqualsIgnoreCase(name1.getFamilyName(), name2.getFamilyName()) ||
				!safeEqualsIgnoreCase(name1.getOtherNames(), name2.getOtherNames());
	}

	/**
	 * Helper method to compare two strings safely, ignoring case and handling nulls.
	 */
	private boolean safeEqualsIgnoreCase(String str1, String str2) {
		if (str1 == null && str2 == null) return true;  // Both are null, consider equal
		if (str1 == null || str2 == null) return false; // One is null, the other isn't
		return str1.equalsIgnoreCase(str2);             // Compare normally
	}

	/**
	 * Calls individual search to fetch a individual and enriches it in request
	 *
	 * @param request
	 */
	private void enrichUser(ServiceRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();
		String userUuid = request.getRequestInfo().getUserInfo().getUuid();
		String tenantId = request.getService().getTenantId();

		IndividualBulkResponse individualBulkResponse = searchIndividual(requestInfo, individualUtil.getStateLevelTenant(tenantId), userUuid, null);
		Individual individualServiceResponse = individualBulkResponse.getIndividual().get(0);

		if (individualBulkResponse.getIndividual().isEmpty()) {
			Individual individual = individualUtil.buildIndividualFromUserInfo(request.getRequestInfo().getUserInfo());
			individualServiceResponse = createIndividual(requestInfo, tenantId, individual);
//			throw new CustomException("INVALID_ACCOUNTID", "No individual exist for the given accountId");
		}

		request.getService().setCitizen(individualUtil.buildUserFromIndividual(individualServiceResponse));
		request.getService().setAccountId(individualServiceResponse.getIndividualId());
	}

	/**
	 * Creates the individual from the given individual by calling individual service
	 *
	 * @param requestInfo
	 * @param tenantId
	 * @param individual
	 * @return
	 */
	private Individual createIndividual(RequestInfo requestInfo, String tenantId, Individual individual) {

		individualUtil.addIndividualDefaultFields(individual.getMobileNumber(), tenantId, individual);
		StringBuilder uri = new StringBuilder(config.getIndividualHost())
				.append(config.getIndividualContextPath())
				.append(config.getIndividualCreateEndpoint());


		IndividualResponse individualResponse = individualUtil.createIndividual(new IndividualRequest(requestInfo, individual), uri);

		return individualResponse.getIndividual();
	}

	/**
	 * Updates the given individual by calling individual service
	 *
	 * @param requestInfo
	 * @param individual
	 * @param individualFromSearch
	 * @return
	 */
	private Individual updateIndividual(RequestInfo requestInfo, Individual individual, Individual individualFromSearch) {

		Name name = new Name();
		name.setFamilyName(individual.getName().getFamilyName());
		name.setGivenName(individual.getName().getGivenName());
		name.setOtherNames(individual.getName().getOtherNames());

		individualFromSearch.setName(name);
		individualFromSearch.setIsSystemUserActive(true);

		StringBuilder uri = new StringBuilder(config.getIndividualHost())
				.append(config.getIndividualContextPath())
				.append(config.getIndividualUpdateEndpoint());

		IndividualResponse individualResponse = individualUtil.createIndividual(new IndividualRequest(requestInfo, individualFromSearch), uri);

		return individualResponse.getIndividual();
	}

	/**
	 * calls the individual search API based on the given accountId and userName
	 *
	 * @param requestInfo
	 * @param stateLevelTenant
	 * @param userUuid
	 * @param userName
	 * @return
	 */
	private IndividualBulkResponse searchIndividual(RequestInfo requestInfo, String stateLevelTenant, String userUuid, String userName) {

		IndividualSearch individualSearch = new IndividualSearch();
//		individualSearch.setIsSystemUserActive(true);
		individualSearch.setType(USERTYPE_CITIZEN);

		if (StringUtils.isEmpty(userUuid) && StringUtils.isEmpty(userName))
			return null;

		if (!StringUtils.isEmpty(userUuid))
			individualSearch.setUserUuid(Collections.singletonList(userUuid));

		if (!StringUtils.isEmpty(userName))
			individualSearch.setUsername(Collections.singletonList(userName));

		IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
		individualSearchRequest.setRequestInfo(requestInfo);
		individualSearchRequest.setIndividual(individualSearch);

		StringBuilder uri = getIndividualSearchUrl(stateLevelTenant, config.getIndividualSearchLimit(), config.getIndividualSearchOffset());

		return individualUtil.searchIndividual(individualSearchRequest, uri);
	}

	/**
	 * calls the individual search API based on the given list of user uuids
	 *
	 * @param requestInfo
	 * @param uuids
	 * @return
	 */
	private Map<String, User> searchBulkIndividual(RequestInfo requestInfo, String tenantId, List<String> individualIds) {

		IndividualSearch individualSearch = new IndividualSearch();
//		individualSearch.setIsSystemUserActive(true);
		individualSearch.setType(USERTYPE_CITIZEN);

		if (!CollectionUtils.isEmpty(individualIds))
			individualSearch.setIndividualId(individualIds);

		IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
		individualSearchRequest.setRequestInfo(requestInfo);
		individualSearchRequest.setIndividual(individualSearch);

		StringBuilder uri = getIndividualSearchUrl(tenantId, config.getIndividualSearchLimit(), config.getIndividualSearchOffset());

		IndividualBulkResponse individualBulkResponse = individualUtil.searchIndividual(individualSearchRequest, uri);
		List<Individual> individualList = individualBulkResponse.getIndividual();

		if (CollectionUtils.isEmpty(individualList))
			throw new CustomException("INDIVIDUAL_NOT_FOUND", "No individual found for the user uuids");

		return individualList.stream().collect(Collectors.toMap(Individual::getIndividualId, individualUtil::buildUserFromIndividual ));
	}

	/**
	 * Enriches the list of userUuids associated with the mobileNumber in the search criteria
	 *
	 * @param tenantId
	 * @param criteria
	 * @param requestInfo
	 */
	public void enrichUserIds(String tenantId, RequestSearchCriteria criteria, RequestInfo requestInfo) {

		String mobileNumber = criteria.getMobileNumber();

		IndividualSearch individualSearch = new IndividualSearch();
//		individualSearch.setIsSystemUserActive(true);
		individualSearch.setType(USERTYPE_CITIZEN);
		individualSearch.setMobileNumber(Collections.singletonList(mobileNumber));

		IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
		individualSearchRequest.setRequestInfo(requestInfo);
		individualSearchRequest.setIndividual(individualSearch);

		StringBuilder uri = getIndividualSearchUrl(tenantId, config.getIndividualSearchLimit(), config.getIndividualSearchOffset());

		IndividualBulkResponse individualBulkResponse = individualUtil.searchIndividual(individualSearchRequest, uri);
		List<Individual> individualList = individualBulkResponse.getIndividual();

		Set<String> individualIds = individualList.stream().map(Individual::getIndividualId).collect(Collectors.toSet());

		criteria.setUserIds(individualIds);
	}

	private StringBuilder getIndividualSearchUrl(String tenantId, Integer limit, Integer offset) {
		return new StringBuilder(config.getIndividualHost())
				.append(config.getIndividualContextPath())
				.append(config.getIndividualSearchEndpoint())
				.append("?tenantId=").append(tenantId)
				.append("&limit=").append(limit)
				.append("&offset=").append(offset);
	}

}
