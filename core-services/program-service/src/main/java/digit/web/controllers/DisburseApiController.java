package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.DisburseService;
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

import java.io.IOException;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class DisburseApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private DisburseService disburseService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public DisburseApiController(ObjectMapper objectMapper, HttpServletRequest request, DisburseService disburseService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.disburseService = disburseService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/disburse/_create", method = RequestMethod.POST)
	public ResponseEntity<DisburseResponse> disburseCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody DisburseRequest disburseRequest) {
		Disburse disburse = disburseService.registerDisburse(disburseRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(disburseRequest.getRequestInfo(), true);
		DisburseResponse response = DisburseResponse.builder().disburse(disburse).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/disburse/_search", method = RequestMethod.POST)
	public ResponseEntity<DisburseSearchResponse> disburseSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get disbursement(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody DisburseSearchRequest disburseSearchRequest) {
		List<Disburse> disburses = disburseService.searchDisburses(disburseSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(disburseSearchRequest.getRequestInfo(), true);
		DisburseSearchResponse response = DisburseSearchResponse.builder().disburses(disburses).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/disburse/_update", method = RequestMethod.POST)
	public ResponseEntity<DisburseResponse> disburseUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody DisburseRequest disburseRequest) {
		Disburse disburse = disburseService.updateDisburse(disburseRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(disburseRequest.getRequestInfo(), true);
		DisburseResponse response = DisburseResponse.builder().disburse(disburse).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
