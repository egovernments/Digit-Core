package org.egov.handler.web.controller;

import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.service.DataHandlerService;
import org.egov.handler.util.ResponseInfoFactory;
import org.egov.handler.web.models.DataSetupRequest;
import org.egov.handler.web.models.DataSetupResponse;
import org.egov.handler.web.models.DefaultDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("")
public class DataHandlerController {

	private final DataHandlerService dataHandlerService;

	private final ResponseInfoFactory responseInfoFactory;

	@Autowired
	public DataHandlerController(DataHandlerService dataHandlerService, ResponseInfoFactory responseInfoFactory) {
		this.dataHandlerService = dataHandlerService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "/defaultdata/setup", method = RequestMethod.POST)
	public ResponseEntity<DataSetupResponse> DefaultDataCreatePost(@Valid @RequestBody DataSetupRequest dataSetupRequest) {
		dataHandlerService.addMdmsData(dataSetupRequest);
		DefaultDataRequest defaultDataRequest = dataHandlerService.setupDefaultData(dataSetupRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(dataSetupRequest.getRequestInfo(), true);

		DataSetupResponse dataSetupResponse = DataSetupResponse.builder()
				.responseInfo(responseInfo)
				.targetTenantId(defaultDataRequest.getTargetTenantId())
				.module(dataSetupRequest.getModule())
				.schemaCodes(defaultDataRequest.getSchemaCodes())
				.onlySchemas(defaultDataRequest.getOnlySchemas())
				.build();
		return new ResponseEntity<>(dataSetupResponse, HttpStatus.ACCEPTED);
	}

}
