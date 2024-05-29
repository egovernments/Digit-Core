package digit.enrichment;

import digit.util.IdgenUtil;
import digit.web.models.Agency;
import digit.web.models.AgencyRequest;
import digit.web.models.AuditDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class AgencyEnrichment {

	@Autowired
	private IdgenUtil idgenUtil;

	public void enrichAgencyApplication(AgencyRequest agencyRequest) {
		String type = null;
		if (Objects.equals(agencyRequest.getAgency().getAgencyType(), "Funding Agency"))
			type = "Funding";
		else if (Objects.equals(agencyRequest.getAgency().getAgencyType(), "Implementing Agency"))
			type = "Implementing";

		String idFormat = "[tenantid]/" + agencyRequest.getAgency().getProgramCode() + "/" + type + "/" + agencyRequest.getAgency().getOrgNumber();
		//Retrieve list of IDs from IDGen service
		List<String> agencyRegistrationIdList = idgenUtil.getIdList(agencyRequest.getRequestInfo(), agencyRequest.getAgency().getTenantId(), "ag", idFormat, 1);
		Agency agency = agencyRequest.getAgency();

		// Enrich audit details
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(agencyRequest.getRequestInfo().getUserInfo().getUuid())
				.createdTime(System.currentTimeMillis())
				.lastModifiedBy(agencyRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		agency.setAuditDetails(auditDetails);

		// Enrich UUID
		agency.setId(UUID.randomUUID().toString());

		// Set application number from IdGen
		agency.setAgencyId(agencyRegistrationIdList.get(0));
	}

	public void enrichAgencyUponUpdate(AgencyRequest agencyRequest) {
		// Enrich lastModifiedTime and lastModifiedBy in case of update
		AuditDetails auditDetails = AuditDetails.builder()
				.lastModifiedBy(agencyRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis())
				.build();
		agencyRequest.getAgency().setAuditDetails(auditDetails);
	}
}
