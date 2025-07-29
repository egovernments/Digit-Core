package org.egov.infra.mdms.controller;


import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("/v1/schema")
@Slf4j
public class SchemaDefinitionController {

    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public SchemaDefinitionController(SchemaDefinitionService schemaDefinitionService) {
        this.schemaDefinitionService = schemaDefinitionService;
    }

    /**
     * REST-compliant create: POST /schema/v1/schema
     */
    @PostMapping
    public ResponseEntity<SchemaDefinitionResponse> create(@Valid @RequestBody SchemaDefinitionRequest schemaDefinitionRequest) {
        List<SchemaDefinition> schemaDefinitions =  schemaDefinitionService.create(schemaDefinitionRequest);
        return new ResponseEntity<>(ResponseUtil.getSchemaDefinitionResponse(schemaDefinitionRequest.getRequestInfo(), schemaDefinitions), HttpStatus.ACCEPTED);
    }

    /**
     * REST-compliant search: GET /schema/v1/schema?tenantId=...&code=...
     */
    @GetMapping
    public ResponseEntity<SchemaDefinitionResponse> search(@RequestParam(required = false) String tenantId,
                                                          @RequestParam(required = false) String code) {
        // Build SchemaDefSearchRequest from query params
        SchemaDefSearchRequest searchRequest = new SchemaDefSearchRequest();
        SchemaDefCriteria criteria = new SchemaDefCriteria();
        criteria.setTenantId(tenantId);
        criteria.setCodes(code != null ? List.of(code) : null);
        searchRequest.setSchemaDefCriteria(criteria);
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(searchRequest);
        return new ResponseEntity<>(ResponseUtil.getSchemaDefinitionResponse(null, schemaDefinitions), HttpStatus.ACCEPTED);
    }

}
