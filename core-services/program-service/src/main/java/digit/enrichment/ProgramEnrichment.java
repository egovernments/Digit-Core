package digit.enrichment;

import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.AuditDetails;
import digit.web.models.Program;
import digit.web.models.ProgramRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ProgramEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichProgramApplication(ProgramRequest programRequest) {
		String idFormat = "[tenantid]/" + programRequest.getProgram().getProgramCode();
		//Retrieve list of IDs from IDGen service
		List<String> programRegistrationIdList = idgenUtil.getIdList(programRequest.getRequestInfo(), programRequest.getProgram().getTenantId(), "pg", idFormat, 1);
		Program program = programRequest.getProgram();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(programRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(programRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		program.setAuditDetails(auditDetails);

		// Enrich UUID
		program.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		program.setProgramId(programRegistrationIdList.get(0));
	}

	public void enrichProgramUponUpdate(ProgramRequest programRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(programRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		programRequest.getProgram().setAuditDetails(auditDetails);
	}
}
