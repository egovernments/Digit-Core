package org.egov.pg.clients.individual;

import org.egov.pg.clients.individual.models.Individual;
import org.egov.pg.clients.individual.models.IndividualSearchCriteria;

import java.util.List;

public interface IndividualClient {

	Individual create(String tenantId, String userId, Individual individual);

	List<Individual> search(String tenantId, String userId, IndividualSearchCriteria criteria);
}
