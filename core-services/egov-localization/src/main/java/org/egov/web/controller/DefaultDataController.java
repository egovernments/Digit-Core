package org.egov.web.controller;

import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.domain.service.DefaultDataService;
import org.egov.util.ResponseInfoFactory;
import org.egov.web.contract.DefaultDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/defaultdata")
public class DefaultDataController {

    private final DefaultDataService defaultDataService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public DefaultDataController(DefaultDataService defaultDataService, ResponseInfoFactory responseInfoFactory) {
        this.defaultDataService = defaultDataService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @RequestMapping(value = "_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> create(@Valid @RequestBody DefaultDataRequest defaultDataRequest) {
        defaultDataService.create(defaultDataRequest);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(defaultDataRequest.getRequestInfo(), true);
        return new ResponseEntity<>(responseInfo, HttpStatus.ACCEPTED);
    }
}
