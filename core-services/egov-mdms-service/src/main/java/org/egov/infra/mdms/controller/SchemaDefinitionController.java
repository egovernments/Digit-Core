package org.egov.infra.mdms.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-05-30T09:26:57.838+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("schema/v1")
@Slf4j
public class SchemaDefinitionController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private SchemaDefinitionService schemaDefinitionService;
    @Autowired
    public SchemaDefinitionController(ObjectMapper objectMapper, HttpServletRequest request, SchemaDefinitionService schemaDefinitionService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.schemaDefinitionService = schemaDefinitionService;
    }

    @RequestMapping(value = "_create", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> create(@Parameter(in = ParameterIn.DEFAULT,
            description = "Request body to add new master schema", required = true,
            schema = @Schema()) @Valid @RequestBody SchemaDefinitionRequest schemaDefinitionRequest) {
        List<SchemaDefinition> schemaDefinitions =  schemaDefinitionService.create(schemaDefinitionRequest);
        return new ResponseEntity<SchemaDefinitionResponse>(getSchemaDefinitionResponse(schemaDefinitionRequest.getRequestInfo(), schemaDefinitions, "v1", "Request Accepted For Create"),HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "_search", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> search(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required = true, schema = @Schema()) @Valid @RequestBody SchemaDefSearchRequest schemaDefSearchRequest) {
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(schemaDefSearchRequest);
        return new ResponseEntity<SchemaDefinitionResponse>(getSchemaDefinitionResponse(schemaDefSearchRequest.getRequestInfo(), schemaDefinitions, "v1", "Request Accepted For Create"),HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "_update", method = RequestMethod.POST)
    public ResponseEntity<SchemaDefinitionResponse> update(@Parameter(in = ParameterIn.DEFAULT, description = "Request body to update existing master schema", required = true, schema = @Schema()) @Valid @RequestBody SchemaDefinitionRequest body) {
        return new ResponseEntity<SchemaDefinitionResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    private SchemaDefinitionResponse getSchemaDefinitionResponse(RequestInfo requestInfo , List<SchemaDefinition> schemaDefinitions, String apiVersion, String resMsgId){
        SchemaDefinitionResponse schemaDefinitionResponse = SchemaDefinitionResponse.builder().schemaDefinitions(schemaDefinitions).requestInfo(buildResponseInfo(requestInfo, apiVersion, resMsgId)).build();
        return schemaDefinitionResponse;
    }
    private ResponseInfo buildResponseInfo(RequestInfo requestInfo, String apiVersion, String resMsgId) {
        return ResponseInfo.builder().apiId(requestInfo.getApiId()).msgId(requestInfo.getMsgId()).resMsgId(resMsgId).ver(apiVersion).ts(System.currentTimeMillis()).build();
    }
}
