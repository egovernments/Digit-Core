package org.egov.infra.mdms.controller;

import java.util.Map;

import javax.validation.Valid;
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

}
