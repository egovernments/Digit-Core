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
public class EstimateEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichEstimateApplication(EstimateRequest estimateRequest) {
		String idFormat = "[tenantid]/" + estimateRequest.getEstimate().getProgramCode() + "/" + estimateRequest.getEstimate().getProjectCode() + "/ES-[SEQ_EGOV_COMMON]";
		//Retrieve list of IDs from IDGen service
		List<String> estimateRegistrationIdList = idgenUtil.getIdList(estimateRequest.getRequestInfo(), estimateRequest.getEstimate().getTenantId(), "es", idFormat, 1);
		Estimate estimate = estimateRequest.getEstimate();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(estimateRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(estimateRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		estimate.setAuditDetails(auditDetails);

		// Enrich UUID
		estimate.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		estimate.setEstimateId(estimateRegistrationIdList.get(0));
	}

	public void enrichEstimateUponUpdate(EstimateRequest estimateRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(estimateRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		estimateRequest.getEstimate().setAuditDetails(auditDetails);
	}
}
