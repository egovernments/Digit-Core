package org.egov.infra.mdms.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSService;
import org.egov.infra.mdms.service.validator.HeaderValidator;
import org.egov.infra.mdms.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@RestController
@Slf4j
@RequestMapping(value = "/v1/mdms")
public class MDMSController {

    private final MDMSService mdmsService;
    private final CacheUtil cacheUtil;
    private final HeaderValidator headerValidator;

    @Autowired
    public MDMSController(MDMSService mdmsService, CacheUtil cacheUtil, HeaderValidator headerValidator) {
        this.mdmsService = mdmsService;
        this.cacheUtil = cacheUtil;
        this.headerValidator = headerValidator;
    }

    /**
     * REST-compliant search: GET /v1/mdms?uniqueIdentifier=...&moduleName=...&masterName=...
     */
    @GetMapping
    public ResponseEntity<?> search(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader("X-Client-ID") String clientId,
            @RequestParam(required = false) String uniqueIdentifier,
            @RequestParam(required = false) List<String> moduleName,
            @RequestParam(required = false) List<String> masterName,
            @RequestParam(required = false) List<String> masterFilter
    ) {
        headerValidator.validateRequiredHeaders(tenantId, clientId);
        MdmsCriteriaReq criteriaReq = new MdmsCriteriaReq();
        MdmsCriteria criteria = new MdmsCriteria();
        criteria.setTenantId(tenantId);
        criteria.setUniqueIdentifier(uniqueIdentifier);
        if (moduleName != null && masterName != null) {
            List<ModuleDetail> moduleDetails = new ArrayList<>();
            for (int i = 0; i < moduleName.size(); i++) {
                ModuleDetail moduleDetail = new ModuleDetail();
                moduleDetail.setModuleName(moduleName.get(i));
                List<MasterDetail> masterDetails = new ArrayList<>();
                if (masterName.size() > i) {
                    MasterDetail masterDetail = new MasterDetail();
                    masterDetail.setName(masterName.get(i));
                    if (masterFilter != null && masterFilter.size() > i) {
                        masterDetail.setFilter(masterFilter.get(i));
                    }
                    masterDetails.add(masterDetail);
                }
                moduleDetail.setMasterDetails(masterDetails);
                moduleDetails.add(moduleDetail);
            }
            criteria.setModuleDetails(moduleDetails);
        }
        criteriaReq.setMdmsCriteria(criteria);
        MdmsResponse mdmsResponse = mdmsService.searchWithCacheSupport(criteriaReq);
        return ResponseEntity.ok(mdmsResponse);
    }

    /**
     * Standard search: POST /v1/_search
     */
    // @PostMapping
    // public ResponseEntity<MdmsResponse> searchV1Post(@Valid @RequestBody MdmsCriteriaReq mdmsCriteriaReq) {
    //     MdmsResponse mdmsResponse = mdmsService.searchWithCacheSupport(mdmsCriteriaReq);
    //     return ResponseEntity.ok(mdmsResponse);
    // }

}
