package org.egov.user.web.controller;

import org.egov.user.domain.exception.sso.SsoException;
import org.egov.user.domain.service.TenantLookupService;
import org.egov.user.web.contract.auth.TenantLookupResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
@ConditionalOnProperty(name = "auth.oidc.enabled", havingValue = "true")
public class TenantLookupController {

    private final TenantLookupService tenantLookupService;

    public TenantLookupController(TenantLookupService tenantLookupService) {
        this.tenantLookupService = tenantLookupService;
    }

    @PostMapping(value = "/tenants", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TenantLookupResponse> tenants(@RequestParam("assertion") String assertion,
            @RequestParam(value = "tenantId", required = false) String tenantId) {
        return ResponseEntity.ok(tenantLookupService.lookup(assertion, tenantId));
    }

    @ExceptionHandler(SsoException.class)
    public ResponseEntity<Map<String, String>> handleSsoException(SsoException e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", e.getErrorCode());
        body.put("error_description", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
