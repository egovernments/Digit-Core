package digit.enrichment;

import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.AuditDetails;
import digit.web.models.Sanction;
import digit.web.models.SanctionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class SanctionEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichSanctionApplication(SanctionRequest sanctionRequest) {
		String idFormat = "[tenantid]/" + sanctionRequest.getSanction().getProgramCode() + "/" + sanctionRequest.getSanction().getProjectCode() + "/SA-[SEQ_EGOV_COMMON]";
		//Retrieve list of IDs from IDGen service
		List<String> sanctionRegistrationIdList = idgenUtil.getIdList(sanctionRequest.getRequestInfo(), sanctionRequest.getSanction().getTenantId(), "sa", idFormat, 1);
		Sanction sanction = sanctionRequest.getSanction();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(sanctionRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(sanctionRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		sanction.setAuditDetails(auditDetails);

		// Enrich UUID
		sanction.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		sanction.setSanctionId(sanctionRegistrationIdList.get(0));
	}

	public void enrichSanctionUponUpdate(SanctionRequest sanctionRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(sanctionRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		sanctionRequest.getSanction().setAuditDetails(auditDetails);
	}
}
