package org.egov.infra.mdms.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.service.MDMSService;
import org.egov.mdms.model.*;
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



    public MDMSController(ObjectMapper objectMapper, HttpServletRequest request, MDMSService mdmsService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.mdmsService = mdmsService;
    }
    @PostMapping("_search")
    @ResponseBody
    private ResponseEntity<?> search(@RequestBody @Valid MdmsCriteriaReq mdmsCriteriaReq) {

        Map<String, Map<String, JSONArray>> response = mdmsService.searchMaster(mdmsCriteriaReq);
        MdmsResponse mdmsResponse = new MdmsResponse();
        mdmsResponse.setMdmsRes(response);

        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);
    }


    @PostMapping("_get")
    @ResponseBody
    @Deprecated
    private ResponseEntity<?> search(@RequestParam("moduleName") String module,
                                     @RequestParam("masterName") String master,
                                     @RequestParam(value = "filter", required = false) String filter,
                                     @RequestParam("tenantId") String tenantId,
                                     @RequestBody RequestInfo requestInfo) {

        log.info("MDMSController mdmsCriteriaReq [" + module + ", " + master + ", " + filter + "]");
        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setRequestInfo(requestInfo);
        MdmsCriteria criteria = new MdmsCriteria();
        criteria.setTenantId(tenantId);

        ModuleDetail detail = new ModuleDetail();
        detail.setModuleName(module);

        MasterDetail masterDetail = new MasterDetail();
        masterDetail.setName(master);
        masterDetail.setFilter(filter);
        ArrayList<MasterDetail> masterList = new ArrayList<>();
        masterList.add(masterDetail);
        detail.setMasterDetails(masterList);

        ArrayList<ModuleDetail> moduleList = new ArrayList<>();
        moduleList.add(detail);

        criteria.setModuleDetails(moduleList);
        mdmsCriteriaReq.setMdmsCriteria(criteria);

        Map<String, Map<String, JSONArray>> response = mdmsService.searchMaster(mdmsCriteriaReq);
        MdmsResponse mdmsResponse = new MdmsResponse();
        mdmsResponse.setMdmsRes(response);
        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);

    }
    @RequestMapping(value="_create/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<org.egov.infra.mdms.model.MdmsResponse> egovMdmsServiceV1CreateSchemaCodePost(@Parameter(in = ParameterIn.DEFAULT, description = "Data create request", required=true, schema=@Schema()) @Valid @RequestBody MdmsRequest mdmsRequest, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        List<Mdms> mdmsList = mdmsService.create(mdmsRequest);
        org.egov.infra.mdms.model.MdmsResponse mdmsResponse = org.egov.infra.mdms.model.MdmsResponse.builder().mdms(mdmsList).build();
        return new ResponseEntity<org.egov.infra.mdms.model.MdmsResponse>(mdmsResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="_search/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<?> egovMdmsServiceV1SearchSchemaCodePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required=true, schema=@Schema()) @Valid @RequestBody MdmsCriteriaReq body, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        log.info("Search Criteria:",body);
        Map<String,Map<String,JSONArray>>  moduleMasterMap = mdmsService.search(body);
        return new ResponseEntity<>(moduleMasterMap, HttpStatus.OK);
    }

    @RequestMapping(value="_update/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponse> egovMdmsServiceV1UpdateSchemaCodePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required=true, schema=@Schema()) @Valid @RequestBody MdmsRequest body, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {

        return new ResponseEntity<MdmsResponse>(HttpStatus.NOT_IMPLEMENTED);
    }
}
