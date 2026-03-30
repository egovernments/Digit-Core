package org.egov.user.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.user.domain.service.SessionService;
import org.egov.user.web.contract.SwitchSessionRequest;
import org.egov.user.web.contract.ValidateSessionRequest;
import org.egov.user.web.contract.ValidateSessionResponse;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Slf4j
public class SessionController {

    private final SessionService sessionService;
    private final ResponseInfoFactory responseInfoFactory;

    public SessionController(SessionService sessionService, ResponseInfoFactory responseInfoFactory) {
        this.sessionService = sessionService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/v1/_validate")
    public ResponseEntity<ValidateSessionResponse> validateSession(
            @Valid @RequestBody ValidateSessionRequest request) {

        ValidateSessionResponse response = sessionService.validateSession(
                request.getUsername(), request.getTenantId());

        response.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(
                request.getRequestInfo(), true));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/v1/_switch")
    public ResponseEntity<Map<String, Object>> switchSession(
            @Valid @RequestBody SwitchSessionRequest request) {

        log.info("Switch session request received for user: {}", request.getUsername());
        Map<String, Object> loginResponse = sessionService.switchSession(request);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }
}
