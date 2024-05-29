package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.SanctionService;
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
public class SanctionApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private SanctionService sanctionService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public SanctionApiController(ObjectMapper objectMapper, HttpServletRequest request, SanctionService sanctionService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.sanctionService = sanctionService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/sanction/_create", method = RequestMethod.POST)
	public ResponseEntity<SanctionResponse> sanctionCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new sanction + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody SanctionRequest sanctionRequest) {
		Sanction sanction = sanctionService.registerSanction(sanctionRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(sanctionRequest.getRequestInfo(), true);
		SanctionResponse response = SanctionResponse.builder().sanction(sanction).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/sanction/_search", method = RequestMethod.POST)
	public ResponseEntity<SanctionSearchResponse> sanctionSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get sanction(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody SanctionSearchRequest sanctionSearchRequest) {
		List<Sanction> sanctions = sanctionService.searchSanctions(sanctionSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(sanctionSearchRequest.getRequestInfo(), true);
		SanctionSearchResponse response = SanctionSearchResponse.builder().sanctions(sanctions).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/sanction/_update", method = RequestMethod.POST)
	public ResponseEntity<SanctionResponse> sanctionUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated sanction + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody SanctionRequest sanctionRequest) {
		Sanction sanction = sanctionService.updateSanction(sanctionRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(sanctionRequest.getRequestInfo(), true);
		SanctionResponse response = SanctionResponse.builder().sanction(sanction).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
