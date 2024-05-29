package digit.validators;

import digit.repository.EstimateRepository;
import digit.repository.ProjectRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class EstimateValidator {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private EstimateRepository estimateRepository;

	public void validateEstimateApplication(EstimateRequest estimateRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(estimateRequest.getEstimate().getTenantId()))
			throw new CustomException("EG_ES_APP_ERR", "tenantId is mandatory for creating estimate");

		// Project code validation
		List<Project> projects = projectRepository.getProjects(ProjectSearch.builder().projectCode(estimateRequest.getEstimate().getProjectCode()).build());
		if (projects.isEmpty())
			throw new CustomException("EG_PJ_APP_ERR", "Invalid project code");
	}

	public void validateEstimateUpdateRequest(EstimateRequest estimateRequest) {
		// Checking estimate existence
		String id = estimateRequest.getEstimate().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Estimate> estimates = estimateRepository.getEstimates(EstimateSearch.builder().ids(ids).build());
		if (estimates.isEmpty())
			throw new CustomException("ESTIMATE_DOES_NOT_EXIST", "Estimate ID does not exist");
	}
}
