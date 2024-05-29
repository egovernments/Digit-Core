package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.AllocationEnrichment;
import digit.kafka.Producer;
import digit.repository.AllocationRepository;
import digit.validators.AllocationValidator;
import digit.web.models.Allocation;
import digit.web.models.AllocationRequest;
import digit.web.models.AllocationSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AllocationService {

	@Autowired
	private AllocationRepository allocationRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private AllocationEnrichment allocationEnrichment;

	@Autowired
	private AllocationValidator allocationValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Allocation registerAllocation(AllocationRequest allocationRequest) {
		allocationValidator.validateAllocationApplication(allocationRequest);

		allocationEnrichment.enrichAllocationApplication(allocationRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getAllocationCreateTopic(), allocationRequest);

		// Return the response back to user
		return allocationRequest.getAllocation();
	}

	public List<Allocation> searchAllocations(AllocationSearchRequest allocationSearchRequest) {
		List<Allocation> allocations = allocationRepository.getAllocations(allocationSearchRequest.getCriteria());

		// If no allocations are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(allocations))
			return new ArrayList<>();

		// Otherwise, return the found sanctions
		return allocations;
	}

	public Allocation updateAllocation(AllocationRequest allocationRequest) {
		// Validate whether the allocation that is being requested for update indeed exists
		allocationValidator.validateAllocationUpdateRequest(allocationRequest);

		// Enrich application upon update
		allocationEnrichment.enrichAllocationUponUpdate(allocationRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getAllocationUpdateTopic(), allocationRequest);

		return allocationRequest.getAllocation();
	}
}
