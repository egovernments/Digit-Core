package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.EstimateService;
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

import java.io.IOException;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class EstimateApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private EstimateService estimateService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public EstimateApiController(ObjectMapper objectMapper, HttpServletRequest request, EstimateService estimateService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.estimateService = estimateService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/estimate/_create", method = RequestMethod.POST)
	public ResponseEntity<EstimateResponse> estimateCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new estimate + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody EstimateRequest estimateRequest) {
		Estimate estimate = estimateService.registerEstimate(estimateRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(estimateRequest.getRequestInfo(), true);
		EstimateResponse response = EstimateResponse.builder().estimate(estimate).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/estimate/_search", method = RequestMethod.POST)
	public ResponseEntity<EstimateSearchResponse> estimateSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get esimate(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody EstimateSearchRequest estimateSearchRequest) {
		List<Estimate> estimates = estimateService.searchEstimates(estimateSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(estimateSearchRequest.getRequestInfo(), true);
		EstimateSearchResponse response = EstimateSearchResponse.builder().estimates(estimates).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/estimate/_update", method = RequestMethod.POST)
	public ResponseEntity<EstimateResponse> estimateUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new estimate + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody EstimateRequest estimateRequest) {
		Estimate estimate = estimateService.updateEstimate(estimateRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(estimateRequest.getRequestInfo(), true);
		EstimateResponse response = EstimateResponse.builder().estimate(estimate).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
