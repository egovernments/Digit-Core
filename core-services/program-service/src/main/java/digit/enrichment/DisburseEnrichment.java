package digit.enrichment;

import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.AuditDetails;
import digit.web.models.Disburse;
import digit.web.models.DisburseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DisburseEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichDisburseApplication(DisburseRequest disburseRequest) {
		String idFormat = "[tenantid]/" + disburseRequest.getDisburse().getProgramCode() + "/" + disburseRequest.getDisburse().getProjectCode() + "/DI-[SEQ_EGOV_COMMON]";
		//Retrieve list of IDs from IDGen service
		List<String> disburseRegistrationIdList = idgenUtil.getIdList(disburseRequest.getRequestInfo(), disburseRequest.getDisburse().getTenantId(), "di", idFormat, 1);
		Disburse disburse = disburseRequest.getDisburse();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(disburseRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(disburseRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		disburse.setAuditDetails(auditDetails);

		// Enrich UUID
		disburse.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		disburse.setDisburseId(disburseRegistrationIdList.get(0));
	}

	public void enrichDisburseUponUpdate(DisburseRequest disburseRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(disburseRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		disburseRequest.getDisburse().setAuditDetails(auditDetails);
	}
}
