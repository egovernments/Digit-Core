package digit.enrichment;

import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ProjectEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichProjectApplication(ProjectRequest projectRequest) {
		String idFormat = "[tenantid]/" + projectRequest.getProject().getProgramCode() + "/" + projectRequest.getProject().getProjectCode();
		//Retrieve list of IDs from IDGen service
		List<String> projectRegistrationIdList = idgenUtil.getIdList(projectRequest.getRequestInfo(), projectRequest.getProject().getTenantId(), "pj", idFormat, 1);
		Project project = projectRequest.getProject();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(projectRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(projectRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		project.setAuditDetails(auditDetails);

		// Enrich UUID
		project.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		project.setProjectId(projectRegistrationIdList.get(0));
	}

	public void enrichProjectUponUpdate(ProjectRequest projectRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(projectRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		projectRequest.getProject().setAuditDetails(auditDetails);
	}
}
