package org.egov.user.web.controller;

import org.egov.user.domain.service.UserTenantMappingService;
import org.egov.user.web.contract.TenantMappingUpsertRequest;
import org.egov.user.web.contract.TenantMappingUpsertResponse;
import org.egov.user.web.contract.TenantMappingUpsertResponse.Result;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Internal admin endpoint for backfilling user tenant mappings.
 *
 * <p>Like the other {@code /sso} admin endpoints this is for operational use and must be
 * protected at the API gateway; it must not be exposed to untrusted callers.</p>
 */
@RestController
@RequestMapping("/sso")
public class UserTenantMappingAdminController {

    private final UserTenantMappingService userTenantMappingService;
    private final ResponseInfoFactory responseInfoFactory;

    public UserTenantMappingAdminController(UserTenantMappingService userTenantMappingService,
            ResponseInfoFactory responseInfoFactory) {
        this.userTenantMappingService = userTenantMappingService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/tenant-mappings/_upsert")
    public ResponseEntity<TenantMappingUpsertResponse> upsert(@RequestBody @Valid TenantMappingUpsertRequest request) {
        if (CollectionUtils.isEmpty(request.getUserNames()) && CollectionUtils.isEmpty(request.getUserIds())) {
            return ResponseEntity.badRequest().body(TenantMappingUpsertResponse.builder()
                    .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), false))
                    .build());
        }

        List<Result> results = userTenantMappingService.upsertMappings(request.getTenantId(), request.getUserType(),
                request.getUserNames(), request.getUserIds(), request.getRequestInfo());

        return ResponseEntity.ok(TenantMappingUpsertResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .results(results)
                .build());
    }
}
