package org.egov.user.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.service.UserSessionService;
import org.egov.user.web.contract.RevokeSessionRequest;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Administrative session-revoke for exceptional cases (e.g. a lost/unavailable device).
 * Authorization is enforced the same way as every other privileged endpoint in this
 * service: via the gateway's role-action (RBAC) mapping, not in-service Spring Security —
 * see SecurityConfig, which performs no in-service RBAC of its own.
 */
@RestController
@Slf4j
public class UserSessionController {

    private final UserSessionService userSessionService;
    private final ResponseInfoFactory responseInfoFactory;

    public UserSessionController(UserSessionService userSessionService, ResponseInfoFactory responseInfoFactory) {
        this.userSessionService = userSessionService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/user-session/v1/_revoke")
    public ResponseEntity<Map<String, Object>> revoke(@Valid @RequestBody RevokeSessionRequest request) {
        log.info("Session revoke requested for user: {} tenant: {}", request.getUserUuid(), request.getTenantId());
        userSessionService.revoke(request.getUserUuid(), request.getTenantId());

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        Map<String, Object> response = new HashMap<>();
        response.put("ResponseInfo", responseInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
