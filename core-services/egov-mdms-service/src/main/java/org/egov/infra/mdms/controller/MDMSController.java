package org.egov.infra.mdms.controller;

import java.util.ArrayList;
import java.util.Map;

import javax.validation.Valid;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.egov.common.contract.request.RequestInfo;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@RestController
@Slf4j
@RequestMapping(value = "/v1")
public class MDMSController {

    private MDMSService mdmsService;

    @Autowired
    public MDMSController(MDMSService mdmsService) {
        this.mdmsService = mdmsService;
    }

    /**
     * Request handler for serving v1 search requests.
     * @param body
     * @return
     */
    @RequestMapping(value="_search", method = RequestMethod.POST)
    public ResponseEntity<?> search(@Valid @RequestBody MdmsCriteriaReq body) {
        Map<String,Map<String,JSONArray>>  moduleMasterMap = mdmsService.search(body);
        MdmsResponse mdmsResponse = MdmsResponse.builder()
                .mdmsRes(moduleMasterMap)
                .build();
        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);
    }

    @PostMapping("_get")
    @ResponseBody
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

        Map<String, Map<String, JSONArray>> response = mdmsService.search(mdmsCriteriaReq);
        MdmsResponse mdmsResponse = new MdmsResponse();
        mdmsResponse.setMdmsRes(response);
        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);

    }

}
