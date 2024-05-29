package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.SanctionEnrichment;
import digit.kafka.Producer;
import digit.repository.SanctionRepository;
import digit.validators.SanctionValidator;
import digit.web.models.Sanction;
import digit.web.models.SanctionRequest;
import digit.web.models.SanctionSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SanctionService {

	@Autowired
	private SanctionRepository sanctionRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private SanctionEnrichment sanctionEnrichment;

	@Autowired
	private SanctionValidator sanctionValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Sanction registerSanction(SanctionRequest sanctionRequest) {
		sanctionValidator.validateSanctionApplication(sanctionRequest);

		sanctionEnrichment.enrichSanctionApplication(sanctionRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getSanctionCreateTopic(), sanctionRequest);

		// Return the response back to user
		return sanctionRequest.getSanction();
	}

	public List<Sanction> searchSanctions(SanctionSearchRequest sanctionSearchRequest) {
		List<Sanction> sanctions = sanctionRepository.getSanctions(sanctionSearchRequest.getCriteria());

		// If no sanctions are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(sanctions))
			return new ArrayList<>();

		// Otherwise, return the found sanctions
		return sanctions;
	}

	public Sanction updateSanction(SanctionRequest sanctionRequest) {
		// Validate whether the sanction that is being requested for update indeed exists
		sanctionValidator.validateSanctionUpdateRequest(sanctionRequest);

		// Enrich application upon update
		sanctionEnrichment.enrichSanctionUponUpdate(sanctionRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getSanctionUpdateTopic(), sanctionRequest);

		return sanctionRequest.getSanction();
	}
}
