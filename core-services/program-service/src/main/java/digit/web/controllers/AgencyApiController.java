package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.AgencyService;
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
public class AgencyApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private ResponseInfoFactory responseInfoFactory;

	private AgencyService agencyService;

	@Autowired
	public AgencyApiController(ObjectMapper objectMapper, HttpServletRequest request, ResponseInfoFactory responseInfoFactory, AgencyService agencyService) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.responseInfoFactory = responseInfoFactory;
		this.agencyService = agencyService;
	}

	@RequestMapping(value = "/agency/_create", method = RequestMethod.POST)
	public ResponseEntity<AgencyResponse> agencyCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new agency + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AgencyRequest agencyRequest) {
		Agency agency = agencyService.registerAgency(agencyRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(agencyRequest.getRequestInfo(), true);
		AgencyResponse response = AgencyResponse.builder().agency(agency).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/agency/_search", method = RequestMethod.POST)
	public ResponseEntity<AgencySearchResponse> agencySearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get agency(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody AgencySearchRequest agencySearchRequest) {
		List<Agency> agencies = agencyService.searchAgencies(agencySearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(agencySearchRequest.getRequestInfo(), true);
		AgencySearchResponse response = AgencySearchResponse.builder().agencies(agencies).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/agency/_update", method = RequestMethod.POST)
	public ResponseEntity<AgencyResponse> agencyUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated agency + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AgencyRequest agencyRequest) {
		Agency agency = agencyService.updateAgency(agencyRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(agencyRequest.getRequestInfo(), true);
		AgencyResponse response = AgencyResponse.builder().agency(agency).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
