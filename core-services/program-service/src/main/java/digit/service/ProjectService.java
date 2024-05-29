package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.ProjectEnrichment;
import digit.kafka.Producer;
import digit.repository.ProjectRepository;
import digit.validators.ProjectValidator;
import digit.web.models.Project;
import digit.web.models.ProjectRequest;
import digit.web.models.ProjectSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProjectService {
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private ProjectEnrichment projectEnrichment;

	@Autowired
	private ProjectValidator projectValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Project registerProject(ProjectRequest projectRequest) {
		projectValidator.validateProjectApplication(projectRequest);

		projectEnrichment.enrichProjectApplication(projectRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getProjectCreateTopic(), projectRequest);

		// Return the response back to user
		return projectRequest.getProject();
	}

	public List<Project> searchProjects(ProjectSearchRequest projectSearchRequest) {
		List<Project> projects = projectRepository.getProjects(projectSearchRequest.getCriteria());

		// If no projects are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(projects))
			return new ArrayList<>();

		// Otherwise, return the found projects
		return projects;
	}

	public Project updateProject(ProjectRequest projectRequest) {
		// Validate whether the project that is being requested for update indeed exists
		projectValidator.validateProjectUpdateRequest(projectRequest);

		// Enrich application upon update
		projectEnrichment.enrichProjectUponUpdate(projectRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getProjectUpdateTopic(), projectRequest);

		return projectRequest.getProject();
	}
}
