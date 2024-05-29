package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.EstimateEnrichment;
import digit.kafka.Producer;
import digit.repository.EstimateRepository;
import digit.validators.EstimateValidator;
import digit.web.models.Estimate;
import digit.web.models.EstimateRequest;
import digit.web.models.EstimateSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EstimateService {

	@Autowired
	private EstimateRepository estimateRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private EstimateEnrichment estimateEnrichment;

	@Autowired
	private EstimateValidator estimateValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Estimate registerEstimate(EstimateRequest estimateRequest) {
		estimateValidator.validateEstimateApplication(estimateRequest);

		estimateEnrichment.enrichEstimateApplication(estimateRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getEstimateCreateTopic(), estimateRequest);

		// Return the response back to user
		return estimateRequest.getEstimate();
	}

	public List<Estimate> searchEstimates(EstimateSearchRequest estimateSearchRequest) {
		List<Estimate> estimates = estimateRepository.getEstimates(estimateSearchRequest.getCriteria());

		// If no estimates are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(estimates))
			return new ArrayList<>();

		// Otherwise, return the found estimates
		return estimates;
	}

	public Estimate updateEstimate(EstimateRequest estimateRequest) {
		// Validate whether the estimate that is being requested for update indeed exists
		estimateValidator.validateEstimateUpdateRequest(estimateRequest);

		// Enrich application upon update
		estimateEnrichment.enrichEstimateUponUpdate(estimateRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getEstimateUpdateTopic(), estimateRequest);

		return estimateRequest.getEstimate();
	}
}
