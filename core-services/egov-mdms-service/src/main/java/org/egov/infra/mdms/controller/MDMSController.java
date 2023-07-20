package org.egov.infra.mdms.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSService;
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
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Autowired
    public MDMSController(ObjectMapper objectMapper, HttpServletRequest request, MDMSService mdmsService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.mdmsService = mdmsService;
    }

    /**
     * Request handler for serving v1 search requests
     * @param body
     * @param schemaCode
     * @return
     */
    @RequestMapping(value="_search/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<?> search(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required=true, schema=@Schema()) @Valid @RequestBody MdmsCriteriaReq body, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        Map<String,Map<String,JSONArray>>  moduleMasterMap = mdmsService.search(body);
        MdmsResponse mdmsResponse = MdmsResponse.builder()
                .mdmsRes(moduleMasterMap)
                .build();
        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);
    }

}
