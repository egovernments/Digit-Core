package org.egov.infra.mdms.controller;

import java.util.Map;

import jakarta.validation.Valid;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSService;
import org.egov.infra.mdms.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@RestController
@Slf4j
@RequestMapping(value = "/v1")
public class MDMSController {

    private MDMSService mdmsService;

    private final CacheUtil cacheUtil;

    @Autowired
    public MDMSController(MDMSService mdmsService, CacheUtil cacheUtil) {
        this.mdmsService = mdmsService;
        this.cacheUtil = cacheUtil;
    }

    /**
     * REST-compliant search: GET /v1/mdms?tenantId=...&schemaCode=...&uniqueIdentifier=...
     */
    @GetMapping("/mdms")
    public ResponseEntity<?> search(@RequestParam(required = false) String tenantId,
                                    @RequestParam(required = false) String schemaCode,
                                    @RequestParam(required = false) String uniqueIdentifier) {
        // Build MdmsCriteriaReq from query params
        MdmsCriteriaReq criteriaReq = new MdmsCriteriaReq();
        MdmsCriteria criteria = new MdmsCriteria();
        criteria.setTenantId(tenantId);
        criteria.setUniqueIdentifier(uniqueIdentifier);
        // schemaCode is not a direct field, so may need to set in moduleDetails if needed
        criteriaReq.setMdmsCriteria(criteria);
        MdmsResponse mdmsResponse = mdmsService.searchWithCacheSupport(criteriaReq);
        return ResponseEntity.ok(mdmsResponse);
    }

}
