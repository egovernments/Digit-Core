package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.AgencyEnrichment;
import digit.kafka.Producer;
import digit.repository.AgencyRepository;
import digit.validators.AgencyValidator;
import digit.web.models.Agency;
import digit.web.models.AgencyRequest;
import digit.web.models.AgencySearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgencyService {

	@Autowired
	private AgencyRepository agencyRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private AgencyEnrichment agencyEnrichment;

	@Autowired
	private AgencyValidator agencyValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Agency registerAgency(AgencyRequest agencyRequest) {
		agencyValidator.validateAgencyApplication(agencyRequest);

		agencyEnrichment.enrichAgencyApplication(agencyRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getAgencyCreateTopic(), agencyRequest);

		// Return the response back to user
		return agencyRequest.getAgency();
	}

	public List<Agency> searchAgencies(AgencySearchRequest agencySearchRequest) {
		List<Agency> agencies = agencyRepository.getAgencies(agencySearchRequest.getCriteria());

		// If no agencies are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(agencies))
			return new ArrayList<>();

		// Otherwise, return the found agencies
		return agencies;
	}

	public Agency updateAgency(AgencyRequest agencyRequest) {
		// Validate whether the agency that is being requested for update indeed exists
		agencyValidator.validateAgencyUpdateRequest(agencyRequest);

		// Enrich application upon update
		agencyEnrichment.enrichAgencyUponUpdate(agencyRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getAgencyUpdateTopic(), agencyRequest);

		return agencyRequest.getAgency();
	}
}
