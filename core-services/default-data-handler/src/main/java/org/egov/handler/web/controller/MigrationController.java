package org.egov.handler.web.controller;

import jakarta.validation.Valid;
import org.egov.handler.service.MigrationService;
import org.egov.handler.util.ResponseInfoFactory;
import org.egov.handler.web.models.MigrationRequest;
import org.egov.handler.web.models.MigrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("")
public class MigrationController {

    private final MigrationService migrationService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public MigrationController(MigrationService migrationService, ResponseInfoFactory responseInfoFactory) {
        this.migrationService = migrationService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @RequestMapping(value = "/migrate", method = RequestMethod.POST)
    public ResponseEntity<MigrationResponse> migrate(@Valid @RequestBody MigrationRequest migrationRequest) {
        List<String> queuedTenants = migrationService.triggerMigration(migrationRequest);

        MigrationResponse response = MigrationResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(migrationRequest.getRequestInfo(), true))
                .message("Migration queued for " + queuedTenants.size() + " tenant(s)")
                .tenantIds(queuedTenants)
                .build();

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
