package org.egov.infra.mdms.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class MdmsHistoryController {

    private MDMSServiceV2 mdmsServiceV2;

    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public MdmsHistoryController(MDMSServiceV2 mdmsServiceV2, SchemaDefinitionService schemaDefinitionService) {
        this.mdmsServiceV2 = mdmsServiceV2;
        this.schemaDefinitionService = schemaDefinitionService;
    }

    @RequestMapping(value = "data/history/_search", method = RequestMethod.POST)
    public ResponseEntity<MdmsHistoryResponse> searchDataHistory(@Valid @RequestBody MdmsHistorySearchRequest mdmsHistorySearchRequest) {
        List<MdmsHistory> mdmsHistoryList = mdmsServiceV2.searchHistory(mdmsHistorySearchRequest);
        return new ResponseEntity<>(ResponseUtil.getMasterDataHistoryResponse(mdmsHistorySearchRequest.getRequestInfo(), mdmsHistoryList), HttpStatus.OK);
    }

    @RequestMapping(value = "schema/history/_search", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionHistoryResponse> searchSchemaHistory(@Valid @RequestBody SchemaDefHistorySearchRequest schemaDefHistorySearchRequest) {
        List<SchemaDefinitionHistory> schemaDefinitionHistoryList = schemaDefinitionService.searchHistory(schemaDefHistorySearchRequest);
        return new ResponseEntity<>(ResponseUtil.getSchemaDefinitionHistoryResponse(schemaDefHistorySearchRequest.getRequestInfo(), schemaDefinitionHistoryList), HttpStatus.OK);
    }
}
