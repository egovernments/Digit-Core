package org.egov.infra.mdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.infra.mdms.service.validator.HeaderValidator;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping(value = "/v2")
public class MDMSControllerV2 {

    private final MDMSServiceV2 mdmsServiceV2;
    private final HeaderValidator headerValidator;

    @Autowired
    public MDMSControllerV2(MDMSServiceV2 mdmsServiceV2, HeaderValidator headerValidator) {
        this.mdmsServiceV2 = mdmsServiceV2;
        this.headerValidator = headerValidator;
    }

    /**
     * REST-compliant create: POST /v2/mdms
     */
    @PostMapping
    public ResponseEntity<MdmsResponseV2> create(@Valid @RequestBody MdmsRequest mdmsRequest,
                                                @RequestHeader("X-Tenant-ID") String tenantId,
                                                @RequestHeader("X-Client-ID") String clientId) {
        headerValidator.validateRequiredHeaders(tenantId, clientId);
        // Override tenantId from header in all Mdms objects
        if (mdmsRequest.getMdms() != null) {
            mdmsRequest.getMdms().forEach(mdms -> mdms.setTenantId(tenantId));
        }
        List<Mdms> masterDataList = mdmsServiceV2.create(mdmsRequest, clientId);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.CREATED);
    }

    /**
     * REST-compliant update: PUT /v2/mdms/{id}
     */
    @PutMapping
    public ResponseEntity<MdmsResponseV2> update(@Valid @RequestBody MdmsRequest mdmsRequest,
                                                @RequestHeader("X-Tenant-ID") String tenantId,
                                                @RequestHeader("X-Client-ID") String clientId) {
        headerValidator.validateRequiredHeaders(tenantId, clientId);
        // Override tenantId from header in all Mdms objects
        if (mdmsRequest.getMdms() != null) {
            mdmsRequest.getMdms().forEach(mdms -> mdms.setTenantId(tenantId));
        }
        List<Mdms> masterDataList = mdmsServiceV2.update(mdmsRequest, clientId);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.OK);
    }

    /**
     * REST-compliant search: GET /v2/mdms with comprehensive search parameters
     * Examples:
     * - Multiple uniqueIdentifiers: ?uniqueIdentifiers=id1&uniqueIdentifiers=id2
     * - Multiple ids: ?ids=uuid1&ids=uuid2  
     * - Custom filters: ?customField=value&anotherField=anotherValue
     */
    @GetMapping
    public ResponseEntity<MdmsResponseV2> search(@RequestParam(required = false) String schemaCode,
                                                 @RequestParam(required = false) List<String> uniqueIdentifiers,
                                                 @RequestParam(required = false) List<String> ids,
                                                 @RequestParam(required = false) Boolean isActive,
                                                 @RequestParam(required = false) Integer offset,
                                                 @RequestParam(required = false) Integer limit,
                                                 @RequestParam Map<String, String> allParams,
                                                 @RequestHeader("X-Tenant-ID") String tenantId,
                                                 @RequestHeader("X-Client-ID") String clientId) {
        headerValidator.validateRequiredHeaders(tenantId, clientId);
        
        log.info("Search request received with parameters - schemaCode: {}, uniqueIdentifiers: {}, ids: {}, isActive: {}, offset: {}, limit: {}, allParams: {}", 
                 schemaCode, uniqueIdentifiers, ids, isActive, offset, limit, allParams);
        
        // Build MdmsCriteriaReqV2 from query params
        MdmsCriteriaReqV2 criteria = new MdmsCriteriaReqV2();
        MdmsCriteriaV2 mdmsCriteria = new MdmsCriteriaV2();
        
        // Set tenantId from header
        mdmsCriteria.setTenantId(tenantId);
        
        // Set optional parameters
        if (schemaCode != null) {
            mdmsCriteria.setSchemaCode(schemaCode);
        }
        
        if (uniqueIdentifiers != null && !uniqueIdentifiers.isEmpty()) {
            mdmsCriteria.setUniqueIdentifiers(new HashSet<>(uniqueIdentifiers));
        }
        
        if (ids != null && !ids.isEmpty()) {
            mdmsCriteria.setIds(new HashSet<>(ids));
        }
        
        if (isActive != null) {
            mdmsCriteria.setIsActive(isActive);
        }
        
        if (offset != null) {
            mdmsCriteria.setOffset(offset);
        }
        
        if (limit != null) {
            mdmsCriteria.setLimit(limit);
        }
        
        // Extract filter parameters from allParams (exclude known parameters)
        Map<String, String> filters = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("schemaCode") && !key.equals("uniqueIdentifiers") && 
                !key.equals("ids") && !key.equals("isActive") && 
                !key.equals("offset") && !key.equals("limit")) {
                filters.put(key, entry.getValue());
            }
        }
        
        if (!filters.isEmpty()) {
            mdmsCriteria.setFilterMap(filters);
        }
        
        criteria.setMdmsCriteria(mdmsCriteria);
        
        log.info("Final MdmsCriteria built: {}", mdmsCriteria);
        
        List<Mdms> masterDataList = mdmsServiceV2.search(criteria);
        
        log.info("Search completed. Found {} records", masterDataList != null ? masterDataList.size() : 0);
        
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.OK);
    }
}
