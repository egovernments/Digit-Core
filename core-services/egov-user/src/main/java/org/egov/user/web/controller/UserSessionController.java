package org.egov.user.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.service.UserSessionService;
import org.egov.user.web.contract.ActiveSessionSearchRequest;
import org.egov.user.web.contract.ActiveSessionUser;
import org.egov.user.web.contract.RevokeSessionRequest;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
        String actor = request.getRequestInfo() != null && request.getRequestInfo().getUserInfo() != null
                ? request.getRequestInfo().getUserInfo().getUuid() : "UNKNOWN_ADMIN";
        if (!request.isBulkRequest()) {
            // Original single-user contract, unchanged: a missing active session propagates as
            // CustomException (error response), and success returns only ResponseInfo.
            log.info("Session revoke requested for user: {} tenant: {} by {}", request.getUserUuid(), request.getTenantId(), actor);
            userSessionService.revoke(request.getUserUuid(), request.getTenantId(), actor);
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            Map<String, Object> response = new HashMap<>();
            response.put("ResponseInfo", responseInfo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        List<String> userUuids = request.getUserUuidList();
        log.info("Bulk session revoke requested for {} users tenant: {} by {}", userUuids.size(), request.getTenantId(), actor);
        Map<String, Object> result = userSessionService.revokeAll(userUuids, request.getTenantId(), actor);

        // 200 only when every user was revoked; otherwise 207 Multi-Status, with per-user outcomes in the body.
        boolean allRevoked = ((List<?>) result.get("failed")).isEmpty();
        // LinkedHashMap keeps the JSON order: ResponseInfo first, then revoked, then failed.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ResponseInfo", responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), allRevoked));
        response.putAll(result);
        return new ResponseEntity<>(response, allRevoked ? HttpStatus.OK : HttpStatus.MULTI_STATUS);
    }

    @PostMapping("/user-session/v1/_search")
    public ResponseEntity<Map<String, Object>> search(@Valid @RequestBody ActiveSessionSearchRequest request) {
        log.info("Active session search requested for tenant: {} ({} users)", request.getTenantId(),
                request.getUserUuids().size());
        List<ActiveSessionUser> sessions = userSessionService.searchActiveSessions(request.getTenantId(),
                request.getUserUuids());

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        Map<String, Object> response = new HashMap<>();
        response.put("ResponseInfo", responseInfo);
        response.put("sessions", sessions);
        response.put("count", sessions.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
