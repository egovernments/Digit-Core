package digit.validators;

import digit.repository.ProjectRepository;
import digit.repository.SanctionRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class SanctionValidator {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private SanctionRepository sanctionRepository;

	public void validateSanctionApplication(SanctionRequest sanctionRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(sanctionRequest.getSanction().getTenantId()))
			throw new CustomException("EG_SA_APP_ERR", "tenantId is mandatory for creating sanction");

		// Project code validation
		List<Project> projects = projectRepository.getProjects(ProjectSearch.builder().projectCode(sanctionRequest.getSanction().getProjectCode()).build());
		if (projects.isEmpty())
			throw new CustomException("EG_PJ_APP_ERR", "Invalid project code");
	}

	public void validateSanctionUpdateRequest(SanctionRequest sanctionRequest) {
		// Checking sanction existence
		String id = sanctionRequest.getSanction().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Sanction> sanctions = sanctionRepository.getSanctions(SanctionSearch.builder().ids(ids).build());
		if (sanctions.isEmpty())
			throw new CustomException("SANCTION_DOES_NOT_EXIST", "Sanction ID does not exist");
	}
}
