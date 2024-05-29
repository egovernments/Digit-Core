package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.DisburseEnrichment;
import digit.kafka.Producer;
import digit.repository.DisburseRepository;
import digit.validators.DisburseValidator;
import digit.web.models.Disburse;
import digit.web.models.DisburseRequest;
import digit.web.models.DisburseSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DisburseService {

	@Autowired
	private DisburseRepository disburseRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private DisburseEnrichment disburseEnrichment;

	@Autowired
	private DisburseValidator disburseValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Disburse registerDisburse(DisburseRequest disburseRequest) {
		disburseValidator.validateDisburseApplication(disburseRequest);

		disburseEnrichment.enrichDisburseApplication(disburseRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getDisburseCreateTopic(), disburseRequest);

		// Return the response back to user
		return disburseRequest.getDisburse();
	}

	public List<Disburse> searchDisburses(DisburseSearchRequest disburseSearchRequest) {
		List<Disburse> disburses = disburseRepository.getDisburses(disburseSearchRequest.getCriteria());

		// If no disbursements are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(disburses))
			return new ArrayList<>();

		// Otherwise, return the found disbursements
		return disburses;
	}

	public Disburse updateDisburse(DisburseRequest disburseRequest) {
		// Validate whether the disburse that is being requested for update indeed exists
		disburseValidator.validateDisburseUpdateRequest(disburseRequest);

		// Enrich application upon update
		disburseEnrichment.enrichDisburseUponUpdate(disburseRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getDisburseUpdateTopic(), disburseRequest);

		return disburseRequest.getDisburse();
	}
}
