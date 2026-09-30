package org.egov.infra.mdms.controller;


import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import jakarta.validation.Valid;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("schema/v1")
@Slf4j
public class SchemaDefinitionController {

    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public SchemaDefinitionController(SchemaDefinitionService schemaDefinitionService) {
        this.schemaDefinitionService = schemaDefinitionService;
    }

    /**
     * Request handler for serving schema create requests.
     * @param schemaDefinitionRequest
     * @return
     */
    @RequestMapping(value = "_create", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> create(@Valid @RequestBody SchemaDefinitionRequest schemaDefinitionRequest) {
        List<SchemaDefinition> schemaDefinitions =  schemaDefinitionService.create(schemaDefinitionRequest);
        return new ResponseEntity<>(ResponseUtil.getSchemaDefinitionResponse(schemaDefinitionRequest.getRequestInfo(), schemaDefinitions), HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving schema search requests.
     * @param schemaDefinitionSearchRequest
     * @return
     */
    @RequestMapping(value = "_search", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> search(@Valid @RequestBody SchemaDefSearchRequest schemaDefinitionSearchRequest) {
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(schemaDefinitionSearchRequest);
        return new ResponseEntity<>(ResponseUtil.getSchemaDefinitionResponse(schemaDefinitionSearchRequest.getRequestInfo(), schemaDefinitions), HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving schema update requests - NOT implemented as of now.
     * @param schemaDefinitionUpdateRequest
     * @return
     */
    @RequestMapping(value = "_update", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> update(@Valid @RequestBody SchemaDefinitionRequest schemaDefinitionUpdateRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
