package org.egov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.Role;
import org.egov.payment.clients.individual.IndividualClient;
import org.egov.payment.clients.individual.models.Individual;
import org.egov.payment.clients.individual.models.IndividualSearchCriteria;
import org.egov.payment.config.AppProperties;
import org.egov.payment.models.Transaction;
import org.egov.payment.models.User;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private static final String CITIZEN_ROLE_CODE = "CITIZEN";
    private static final String CITIZEN_ROLE_NAME = "Citizen";
    private static final String CITIZEN_TYPE = "CITIZEN";

    private final IndividualClient individualClient;
    private final AppProperties appProperties;

    public UserService(IndividualClient individualClient, AppProperties appProperties) {
        this.individualClient = individualClient;
        this.appProperties = appProperties;
    }

    public User createOrSearchUser(Transaction transaction, String tenantId, String clientId) {
        List<Individual> individuals = individualClient.search(tenantId, clientId,
                IndividualSearchCriteria.builder()
                        .mobileNumber(List.of(transaction.getUser().getMobileNumber()))
                        .givenName(transaction.getUser().getName())
                        .build()
        );

        Individual individual;
        if (CollectionUtils.isEmpty(individuals) && appProperties.getIsUserCreationEnable()) {
            individual = individualClient.create(tenantId, clientId, toIndividual(transaction.getUser(), tenantId));
        } else if (!CollectionUtils.isEmpty(individuals)) {
            individual = individuals.get(0);
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

    public Individual toIndividual(User user, String tenantId) {
        return Individual.builder()
                .userUuid(user.getUuid())
                .givenName(user.getName())
                .username(user.getUserName())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmailId())
                .type(CITIZEN_TYPE)
                .active(true)
                .roles(List.of(Role.builder()
                        .code(CITIZEN_ROLE_CODE)
                        .name(CITIZEN_ROLE_NAME)
                        .tenantId(tenantId.split("\\.")[0])
                        .build()))
                .build();
    }

    public User toUser(Individual individual, String tenantId) {
        return User.builder()
                .uuid(individual.getUserUuid())
                .name(individual.getGivenName())
                .userName(individual.getUsername())
                .mobileNumber(individual.getMobileNumber())
                .emailId(individual.getEmail())
                .tenantId(tenantId)
                .build();
    }
}
