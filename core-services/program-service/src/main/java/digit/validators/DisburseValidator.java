package digit.validators;

import digit.repository.DisburseRepository;
import digit.repository.SanctionRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class DisburseValidator {

	@Autowired
	private SanctionRepository sanctionRepository;

	@Autowired
	private DisburseRepository disburseRepository;

	public void validateDisburseApplication(DisburseRequest disburseRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(disburseRequest.getDisburse().getTenantId()))
			throw new CustomException("EG_DI_APP_ERR", "tenantId is mandatory for creating disburse");

		// Sanction ID validation
		List<Sanction> sanctions = sanctionRepository.getSanctions(SanctionSearch.builder().sanctionId(disburseRequest.getDisburse().getSanctionId()).build());
		if (sanctions.isEmpty())
			throw new CustomException("EG_SA_APP_ERR", "Invalid sanctionId");
	}

	public void validateDisburseUpdateRequest(DisburseRequest disburseRequest) {
		// Checking disburse existence
		String id = disburseRequest.getDisburse().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Disburse> disburses = disburseRepository.getDisburses(DisburseSearch.builder().ids(ids).build());
		if (disburses.isEmpty())
			throw new CustomException("DISBURSE_DOES_NOT_EXIST", "Disburse ID does not exist");
	}
}
