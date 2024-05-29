package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.ProjectService;
import digit.util.ResponseInfoFactory;
import digit.web.models.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class ProjectApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private ProjectService projectService;

	private ResponseInfoFactory responseInfoFactory;


	@Autowired
	public ProjectApiController(ObjectMapper objectMapper, HttpServletRequest request, ProjectService projectService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.projectService = projectService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/project/_create", method = RequestMethod.POST)
	public ResponseEntity<ProjectResponse> projectCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new project + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProjectRequest projectRequest) {
		Project project = projectService.registerProject(projectRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(projectRequest.getRequestInfo(), true);
		ProjectResponse response = ProjectResponse.builder().project(project).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/project/_search", method = RequestMethod.POST)
	public ResponseEntity<ProjectSearchResponse> projectSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get project(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody ProjectSearchRequest projectSearchRequest) {
		List<Project> projects = projectService.searchProjects(projectSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(projectSearchRequest.getRequestInfo(), true);
		ProjectSearchResponse response = ProjectSearchResponse.builder().projects(projects).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/project/_update", method = RequestMethod.POST)
	public ResponseEntity<ProjectResponse> projectUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated project + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProjectRequest projectRequest) {
		Project project = projectService.updateProject(projectRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(projectRequest.getRequestInfo(), true);
		ProjectResponse response = ProjectResponse.builder().project(project).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
