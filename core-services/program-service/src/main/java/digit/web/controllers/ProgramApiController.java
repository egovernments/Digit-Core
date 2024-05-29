package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.ProgramService;
import digit.util.ResponseInfoFactory;
import digit.web.models.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class ProgramApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private ProgramService programService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public ProgramApiController(ObjectMapper objectMapper, HttpServletRequest request, ProgramService programService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.programService = programService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/program/_create", method = RequestMethod.POST)
	public ResponseEntity<ProgramResponse> programCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of the new program + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProgramRequest programRequest) {
		Program program = programService.registerProgram(programRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(programRequest.getRequestInfo(), true);
		ProgramResponse response = ProgramResponse.builder().program(program).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/program/_search", method = RequestMethod.POST)
	public ResponseEntity<ProgramSearchResponse> programSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get programs based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody ProgramSearchRequest programSearchRequest) {
		List<Program> programs = programService.searchPrograms(programSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(programSearchRequest.getRequestInfo(), true);
		ProgramSearchResponse response = ProgramSearchResponse.builder().programs(programs).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/program/_update", method = RequestMethod.POST)
	public ResponseEntity<ProgramResponse> programUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of the updated program + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProgramRequest programRequest) {
		Program program = programService.updateProgram(programRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(programRequest.getRequestInfo(), true);
		ProgramResponse response = ProgramResponse.builder().program(program).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
