package org.egov.elasticrequestcrypt.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.elasticrequestcrypt.models.HttpRequestLog;
import org.egov.elasticrequestcrypt.models.PlainCorrelator;
import org.egov.elasticrequestcrypt.service.CorrelatorRequestProcessingService;
import org.egov.encryption.web.contract.EncryptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/elastic-request-crypt")
public class TestController {

    private CorrelatorRequestProcessingService correlatorRequestProcessingService;

    @Autowired
    public TestController(CorrelatorRequestProcessingService correlatorRequestProcessingService) {
        this.correlatorRequestProcessingService = correlatorRequestProcessingService;
    }

    @RequestMapping(value="/_test", method = RequestMethod.POST)
    public ResponseEntity<Void> create(@RequestBody @Valid HttpRequestLog httpRequestLog) {
//        correlatorRequestProcessingService.processEncryptedCorrelatorRequest(httpRequestLog);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
