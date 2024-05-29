package digit.validators;

import digit.repository.ProgramRepository;
import digit.web.models.Program;
import digit.web.models.ProgramRequest;
import digit.web.models.ProgramSearch;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProgramValidator {

	@Autowired
	private ProgramRepository programRepository;

	public void validateProgramApplication(ProgramRequest programRequest) {
		// Tenant ID validation
		if (ObjectUtils.isEmpty(programRequest.getProgram().getTenantId()))
			throw new CustomException("EG_PG_APP_ERR", "tenantId is mandatory for creating program");

		// Program code validation
		List<Program> programs = programRepository.getPrograms(ProgramSearch.builder().programCode(programRequest.getProgram().getProgramCode()).build());
		if(!programs.isEmpty())
			throw new CustomException("EG_PG_APP_ERR", "Program Code is already present");
	}

	public void validateProgramUpdateRequest(ProgramRequest programRequest) {
		// Checking program existence
		String id = programRequest.getProgram().getId();
		List<String> ids = new ArrayList<>();
		ids.add(id);
		List<Program> programs = programRepository.getPrograms(ProgramSearch.builder().ids(ids).build());
		if (programs.isEmpty())
			throw new CustomException("PROGRAM_DOES_NOT_EXIST", "Program ID does not exist");
	}
}
