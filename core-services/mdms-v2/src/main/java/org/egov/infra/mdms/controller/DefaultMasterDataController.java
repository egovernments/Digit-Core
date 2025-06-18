package org.egov.infra.mdms.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.infra.mdms.model.DefaultMasterDataRequest;
import org.egov.infra.mdms.service.DefaultMasterDataService;
import org.egov.infra.mdms.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("defaultdata")
@Slf4j
public class DefaultMasterDataController {

	private final DefaultMasterDataService defaultMasterDataService;

	private final ResponseInfoFactory responseInfoFactory;

	@Autowired
	public DefaultMasterDataController(DefaultMasterDataService defaultMasterDataService, ResponseInfoFactory responseInfoFactory) {
		this.defaultMasterDataService = defaultMasterDataService;
		this.responseInfoFactory = responseInfoFactory;
	}

	@RequestMapping(value = "_create", method = RequestMethod.POST)
	public ResponseEntity<ResponseInfo> create(@Valid @RequestBody DefaultMasterDataRequest defaultMasterDataRequest) {
		defaultMasterDataService.create(defaultMasterDataRequest);
		ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(defaultMasterDataRequest.getRequestInfo(), true);
		return new ResponseEntity<>(responseInfo, HttpStatus.ACCEPTED);
	}

}
