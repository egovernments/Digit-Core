package org.egov.payment.clients.individual;

import org.egov.payment.clients.individual.models.Individual;
import org.egov.payment.clients.individual.models.IndividualSearchCriteria;

import java.util.List;

public interface IndividualClient {

    Individual create(String tenantId, String clientId, Individual individual);

    List<Individual> search(String tenantId, String clientId, IndividualSearchCriteria criteria);
}
