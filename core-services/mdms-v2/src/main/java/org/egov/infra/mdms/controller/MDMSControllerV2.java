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
     * REST-compliant search: GET /v2/mdms?schemaCode=...&uniqueIdentifier=...
     */
    @GetMapping
    public ResponseEntity<MdmsResponseV2> search(@RequestParam(required = false) String schemaCode,
                                                 @RequestParam(required = false) String uniqueIdentifier,
                                                 @RequestHeader("X-Tenant-ID") String tenantId,
                                                 @RequestHeader("X-Client-ID") String clientId) {
        headerValidator.validateRequiredHeaders(tenantId, clientId);
        // Build MdmsCriteriaReqV2 from query params
        MdmsCriteriaReqV2 criteria = new MdmsCriteriaReqV2();
        MdmsCriteriaV2 mdmsCriteria = new MdmsCriteriaV2();
        mdmsCriteria.setTenantId(tenantId);
        mdmsCriteria.setSchemaCode(schemaCode);
        if (uniqueIdentifier != null) {
            mdmsCriteria.setUniqueIdentifiers(Set.of(uniqueIdentifier));
        }
        criteria.setMdmsCriteria(mdmsCriteria);
        List<Mdms> masterDataList = mdmsServiceV2.search(criteria);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.OK);
    }
}
