package digit.service;

import digit.config.ProgramServiceConfiguration;
import digit.enrichment.ProgramEnrichment;
import digit.kafka.Producer;
import digit.repository.ProgramRepository;
import digit.validators.ProgramValidator;
import digit.web.models.Program;
import digit.web.models.ProgramRequest;
import digit.web.models.ProgramSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProgramService {

	@Autowired
	private ProgramRepository programRepository;

	@Autowired
	private Producer producer;

	@Autowired
	private ProgramEnrichment programEnrichment;

	@Autowired
	private ProgramValidator programValidator;

	@Autowired
	private ProgramServiceConfiguration configuration;

	public Program registerProgram(ProgramRequest programRequest) {
		programValidator.validateProgramApplication(programRequest);

		programEnrichment.enrichProgramApplication(programRequest);

		// Push the application to the topic for persister to listen and persist
		producer.push(configuration.getProgramCreateTopic(), programRequest);

		// Return the response back to user
		return programRequest.getProgram();
	}

	public List<Program> searchPrograms(ProgramSearchRequest programSearchRequest) {
		List<Program> programs = programRepository.getPrograms(programSearchRequest.getCriteria());

		// If no programs are found matching the given criteria, return an empty list
		if (CollectionUtils.isEmpty(programs))
			return new ArrayList<>();

		// Otherwise, return the found programs
		return programs;
	}

	public Program updateProgram(ProgramRequest programRequest) {
		// Validate whether the program that is being requested for update indeed exists
		programValidator.validateProgramUpdateRequest(programRequest);

		// Enrich application upon update
		programEnrichment.enrichProgramUponUpdate(programRequest);

		// Just like create request, update request will be handled asynchronously by the persister
		producer.push(configuration.getProgramUpdateTopic(), programRequest);

		return programRequest.getProgram();
	}
}
