package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.AllocationService;
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
public class AllocationApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	private AllocationService allocationService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public AllocationApiController(ObjectMapper objectMapper, HttpServletRequest request, AllocationService allocationService, ResponseInfoFactory responseInfoFactory) {
		this.objectMapper = objectMapper;
		this.request = request;
		this.allocationService = allocationService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/allocation/_create", method = RequestMethod.POST)
	public ResponseEntity<AllocationResponse> allocationCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AllocationRequest allocationRequest) {
		Allocation allocation = allocationService.registerAllocation(allocationRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(allocationRequest.getRequestInfo(), true);
		AllocationResponse response = AllocationResponse.builder().allocation(allocation).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/allocation/_search", method = RequestMethod.POST)
	public ResponseEntity<AllocationSearchResponse> allocationSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get allocation(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody AllocationSearchRequest allocationSearchRequest) {
		List<Allocation> allocations = allocationService.searchAllocations(allocationSearchRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(allocationSearchRequest.getRequestInfo(), true);
		AllocationSearchResponse response = AllocationSearchResponse.builder().allocations(allocations).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/allocation/_update", method = RequestMethod.POST)
	public ResponseEntity<AllocationResponse> allocationUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AllocationRequest allocationRequest) {
		Allocation allocation = allocationService.updateAllocation(allocationRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(allocationRequest.getRequestInfo(), true);
		AllocationResponse response = AllocationResponse.builder().allocation(allocation).responseInfo(responseInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
