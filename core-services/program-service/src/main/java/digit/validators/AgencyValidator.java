package digit.validators;

import digit.repository.AgencyRepository;
import digit.repository.ProgramRepository;
import digit.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgencyValidator {

	@Autowired
	private ProgramRepository programRepository;

	@Autowired
	private AgencyRepository agencyRepository;

	public void validateAgencyApplication(AgencyRequest agencyRequest) {
		if (ObjectUtils.isEmpty(agencyRequest.getAgency().getTenantId()))
			throw new CustomException("EG_AG_APP_ERR", "tenantId is mandatory for creating agency");
		List<Program> programs = programRepository.getPrograms(ProgramSearch.builder().programCode(agencyRequest.getAgency().getProgramCode()).build());
		if(programs.size()==0)
			throw new CustomException("PROGRAM_DOES_NOT_EXIST", "Program code does not exist");
	}

	public void validateAgencyUpdateRequest(AgencyRequest agencyRequest) {
		String id = agencyRequest.getAgency().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Agency> agencies = agencyRepository.getAgencies(AgencySearch.builder().ids(ids).build());
		if (agencies.size() == 0)
			throw new CustomException("AGENCY_DOES_NOT_EXIST", "Agency ID does not exist");
	}
}
