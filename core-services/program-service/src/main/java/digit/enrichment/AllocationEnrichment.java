package digit.enrichment;

import digit.util.IdgenUtil;
import digit.util.UserUtil;
import digit.web.models.Allocation;
import digit.web.models.AllocationRequest;
import digit.web.models.AuditDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class AllocationEnrichment {
	@Autowired
	private IdgenUtil idgenUtil;

	@Autowired
	private UserUtil userUtils;

	public void enrichAllocationApplication(AllocationRequest allocationRequest) {
		String idFormat = "[tenantid]/" + allocationRequest.getAllocation().getProgramCode() + "/" + allocationRequest.getAllocation().getProjectCode() + "/AL-[SEQ_EGOV_COMMON]";
		//Retrieve list of IDs from IDGen service
		List<String> allocationRegistrationIdList = idgenUtil.getIdList(allocationRequest.getRequestInfo(), allocationRequest.getAllocation().getTenantId(), "al", idFormat, 1);
		Allocation allocation = allocationRequest.getAllocation();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(allocationRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(allocationRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		allocation.setAuditDetails(auditDetails);

		// Enrich UUID
		allocation.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		allocation.setAllocationId(allocationRegistrationIdList.get(0));
	}

	public void enrichAllocationUponUpdate(AllocationRequest allocationRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(allocationRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		allocationRequest.getAllocation().setAuditDetails(auditDetails);
	}
}
