package org.egov.pg.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.pg.clients.individual.IndividualClient;
import org.egov.pg.clients.individual.models.Individual;
import org.egov.pg.clients.individual.models.IndividualSearchCriteria;
import org.egov.pg.config.AppProperties;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.User;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {

	private static final String USERNAME_ATTRIBUTE = "username";
	// ponytail: txn user carries no gender but the individual spec mandates it
	private static final String DEFAULT_GENDER = "OTHER";

	private final IndividualClient individualClient;
	private final AppProperties appProperties;

	public UserService(IndividualClient individualClient, AppProperties appProperties) {
		this.individualClient = individualClient;
		this.appProperties = appProperties;
	}

	public User createOrSearchUser(Transaction transaction, String tenantId, String userId) {

		List<Individual> individuals = searchByMobile(transaction.getUser().getMobileNumber(), tenantId, userId);

		Individual individual;
		if (!CollectionUtils.isEmpty(individuals)) {
			individual = individuals.get(0);
		} else if (appProperties.getIsUserCreationEnable()) {
			individual = createIndividual(transaction, tenantId, userId);
		} else {
			throw new CustomException("INDIVIDUAL_NOT_FOUND",
					"Individual not found and creation is disabled");
		}

		User user = toUser(individual, tenantId);

		if (user.getUuid() == null || user.getName() == null || user.getMobileNumber() == null || user.getTenantId() == null) {
			throw new CustomException("INVALID_INDIVIDUAL_DETAILS", "User UUID, Name, Mobile Number and Tenant Id are mandatory");
		}

		return user;
	}

	private List<Individual> searchByMobile(String mobileNumber, String tenantId, String userId) {
		return individualClient.search(tenantId, userId,
				IndividualSearchCriteria.builder().mobileNumber(mobileNumber).build());
	}

	private Individual createIndividual(Transaction transaction, String tenantId, String userId) {
		try {
			return individualClient.create(tenantId, userId, toIndividual(transaction.getUser()));
		} catch (ServiceCallException e) {
			// A concurrent request may have won the tenant-uniqueness race (409) — re-search once
			List<Individual> winners = searchByMobile(transaction.getUser().getMobileNumber(), tenantId, userId);
			if (!CollectionUtils.isEmpty(winners)) {
				log.info("Individual create conflicted, using existing individual {}", winners.get(0).getId());
				return winners.get(0);
			}
			throw e;
		}
	}

	public Individual toIndividual(User user) {
		Individual.IndividualBuilder builder = Individual.builder().gender(DEFAULT_GENDER);

		if (StringUtils.hasText(user.getUuid()))
			builder.userId(user.getUuid());
		if (StringUtils.hasText(user.getName()))
			builder.givenName(user.getName());
		if (StringUtils.hasText(user.getMobileNumber()))
			builder.mobileNumber(user.getMobileNumber());
		if (StringUtils.hasText(user.getEmailId()))
			builder.email(user.getEmailId());
		if (StringUtils.hasText(user.getUserName()))
			builder.additionalAttributes(Map.of(USERNAME_ATTRIBUTE, user.getUserName()));

		return builder.build();
	}

	public User toUser(Individual individual, String tenantId) {
		User.UserBuilder builder = User.builder().tenantId(tenantId);

		if (StringUtils.hasText(individual.getUserId()))
			builder.uuid(individual.getUserId());
		if (StringUtils.hasText(individual.getGivenName()))
			builder.name(individual.getGivenName());
		if (StringUtils.hasText(individual.getMobileNumber()))
			builder.mobileNumber(individual.getMobileNumber());
		if (StringUtils.hasText(individual.getEmail()))
			builder.emailId(individual.getEmail());

		Map<String, String> attributes = individual.getAdditionalAttributes();
		if (attributes != null && StringUtils.hasText(attributes.get(USERNAME_ATTRIBUTE)))
			builder.userName(attributes.get(USERNAME_ATTRIBUTE));

		return builder.build();
	}
}
