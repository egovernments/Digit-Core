package digit.validators;

import digit.repository.ProgramRepository;
import digit.repository.ProjectRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectValidator {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProgramRepository programRepository;

	public void validateProjectApplication(ProjectRequest projectRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(projectRequest.getProject().getTenantId()))
			throw new CustomException("EG_PJ_APP_ERR", "tenantId is mandatory for creating project");

		// Checking program existence
		List<Program> programs = programRepository.getPrograms(ProgramSearch.builder().programCode(projectRequest.getProject().getProgramCode()).build());
		if(programs.isEmpty())
			throw new CustomException("EG_PG_APP_ERR", "Invalid program code");

		// Project code validation
		List<Project> projects = projectRepository.getProjects(ProjectSearch.builder().projectCode(projectRequest.getProject().getProjectCode()).build());
		if (!projects.isEmpty())
			throw new CustomException("EG_PJ_APP_ERR", "Project Code is already present");
	}

	public void validateProjectUpdateRequest(ProjectRequest projectRequest) {
		// Checking project existence
		String id = projectRequest.getProject().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Project> projects = projectRepository.getProjects(ProjectSearch.builder().ids(ids).build());
		if (projects.isEmpty())
			throw new CustomException("PROJECT_DOES_NOT_EXIST", "Project ID does not exist");
	}
}
