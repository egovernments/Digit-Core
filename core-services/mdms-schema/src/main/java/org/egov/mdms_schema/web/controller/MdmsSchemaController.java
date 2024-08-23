package org.egov.mdms_schema.web.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.mdms_schema.service.MdmsSchemaService;
import org.egov.mdms_schema.util.ResponseInfoFactory;
import org.egov.mdms_schema.web.model.MdmsSchemaRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/v1")
public class MdmsSchemaController {

	private MdmsSchemaService mdmsSchemaService;

	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	public MdmsSchemaController(MdmsSchemaService mdmsSchemaService, ResponseInfoFactory responseInfoFactory) {
		this.mdmsSchemaService = mdmsSchemaService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "_create", method = RequestMethod.POST)
	public ResponseEntity<ResponseInfo> create(@Valid @RequestBody MdmsSchemaRequest mdmsSchemaRequest) {
		mdmsSchemaService.create(mdmsSchemaRequest.getRequestInfo(), mdmsSchemaRequest.getTenantId());
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(mdmsSchemaRequest.getRequestInfo(), true);
		return new ResponseEntity<>(responseInfo, HttpStatus.ACCEPTED);
	}
}
