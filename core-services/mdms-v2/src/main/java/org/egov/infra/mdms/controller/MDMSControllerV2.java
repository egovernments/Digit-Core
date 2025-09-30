package org.egov.infra.mdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping(value = "/v2")
public class MDMSControllerV2 {

    private MDMSServiceV2 mdmsServiceV2;

    @Autowired
    public MDMSControllerV2(MDMSServiceV2 mdmsServiceV2) {
        this.mdmsServiceV2 = mdmsServiceV2;
    }

    /**
     * REST-compliant create: POST /v2/mdms
     */
    @PostMapping
    public ResponseEntity<MdmsResponseV2> create(@Valid @RequestBody MdmsRequest mdmsRequest,
                                                @RequestHeader("X-Tenant-ID") String tenantId,
                                                @RequestHeader("X-Client-Id") String clientId) {
        validateHeaders(tenantId, clientId);
        List<Mdms> masterDataList = mdmsServiceV2.create(mdmsRequest, clientId);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.ACCEPTED);
    }

    /**
     * REST-compliant update: PUT /v2/mdms/{id}
     */
    @PutMapping
    public ResponseEntity<MdmsResponseV2> update(@Valid @RequestBody MdmsRequest mdmsRequest,
                                                @RequestHeader("X-Tenant-ID") String tenantId,
                                                @RequestHeader("X-Client-Id") String clientId) {
        validateHeaders(tenantId, clientId);
        List<Mdms> masterDataList = mdmsServiceV2.update(mdmsRequest, clientId);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.ACCEPTED);
    }

    /**
     * REST-compliant search: GET /v2/mdms?tenantId=...&schemaCode=...&uniqueIdentifier=...
     */
    @GetMapping
    public ResponseEntity<MdmsResponseV2> search(@RequestParam(required = false) String tenantId,
                                                 @RequestParam(required = false) String schemaCode,
                                                 @RequestParam(required = false) String uniqueIdentifier,
                                                 @RequestHeader("X-Tenant-ID") String headerTenantId,
                                                 @RequestHeader("X-Client-Id") String clientId) {
        validateHeaders(headerTenantId, clientId);
        // Build MdmsCriteriaReqV2 from query params
        MdmsCriteriaReqV2 criteria = new MdmsCriteriaReqV2();
        MdmsCriteriaV2 mdmsCriteria = new MdmsCriteriaV2();
        mdmsCriteria.setTenantId(tenantId != null ? tenantId : headerTenantId);
        mdmsCriteria.setSchemaCode(schemaCode);
        if (uniqueIdentifier != null) {
            mdmsCriteria.setUniqueIdentifiers(Set.of(uniqueIdentifier));
        }
        criteria.setMdmsCriteria(mdmsCriteria);
        List<Mdms> masterDataList = mdmsServiceV2.search(criteria);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(masterDataList), HttpStatus.OK);
    }

    private void validateHeaders(String tenantId, String clientId) {
        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("X-Tenant-ID header is required");
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("X-Client-Id header is required");
        }
    }
}
