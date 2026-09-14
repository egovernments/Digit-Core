package org.egov.user.web.controller;

import org.egov.user.identity.IdentityBridgeService;
import org.egov.user.identity.IdentityContext;
import org.egov.user.identity.IdentityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/identity/v1")
public class IdentityController {

    private final IdentityBridgeService service;

    public IdentityController(IdentityBridgeService service) {
        this.service = service;
    }

    @PostMapping("/contexts/_resolve")
    public Map<String, Object> resolve(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        service.requireWorkload(authorization);
        return response("contexts", service.resolveContexts(request));
    }

    @PostMapping("/sessions/_exchange")
    public Map<String, Object> exchange(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        return service.exchange(authorization, request);
    }

    @PostMapping("/subjects/_ensure")
    public Map<String, Object> ensureSubject(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        service.requireWorkload(authorization);
        return response("digitUserUuid", service.ensureSubject(request));
    }

    @PostMapping("/employees/_ensure")
    public Map<String, Object> ensureEmployee(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        service.requireWorkload(authorization);
        return service.ensureEmployee(request);
    }

    @PostMapping("/organizations/_ensure")
    public Map<String, Object> ensureOrganization(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        service.requireWorkload(authorization);
        return response("tenantId", service.ensureOrganization(request));
    }

    @PostMapping("/memberships/_reconcile")
    public Map<String, Object> reconcileMembership(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> request) {
        service.requireWorkload(authorization);
        return response("membershipId", service.reconcileMembership(request));
    }

    @PostMapping("/reconciliation/_snapshot")
    public Map<String, Object> reconciliationSnapshot(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        service.requireWorkload(authorization);
        return response("organizations", service.reconciliationSnapshot());
    }

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Map<String, Object>> identityError(IdentityException exception) {
        return ResponseEntity.status(HttpStatus.valueOf(exception.getStatus()))
                .body(response("error", exception.getMessage()));
    }

    private Map<String, Object> response(String key, Object value) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put(key, value);
        return response;
    }
}
