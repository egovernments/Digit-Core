package digit.validators;

import digit.repository.AllocationRepository;
import digit.repository.SanctionRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllocationValidator {

	@Autowired
	private SanctionRepository sanctionRepository;

	@Autowired
	private AllocationRepository allocationRepository;

	public void validateAllocationApplication(AllocationRequest allocationRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(allocationRequest.getAllocation().getTenantId()))
			throw new CustomException("EG_AL_APP_ERR", "tenantId is mandatory for creating allocation");

		// Sanction ID  validation
		List<Sanction> sanctions = sanctionRepository.getSanctions(SanctionSearch.builder().sanctionId(allocationRequest.getAllocation().getSanctionId()).build());
		if (sanctions.isEmpty())
			throw new CustomException("EG_SA_APP_ERR", "Invalid sanctionId");
	}

	public void validateAllocationUpdateRequest(AllocationRequest allocationRequest) {
		// Checking allocation existence
		String id = allocationRequest.getAllocation().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Allocation> allocations = allocationRepository.getAllocations(AllocationSearch.builder().ids(ids).build());
		if (allocations.isEmpty())
			throw new CustomException("ALLOCATION_DOES_NOT_EXIST", "Allocation ID does not exist");
	}
}
