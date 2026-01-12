package org.egov.web.controllers;


import org.egov.service.ApportionService;
import org.egov.web.models.ApportionRequest;
import org.egov.web.models.ApportionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.*;
import org.egov.web.models.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2019-02-25T15:07:36.183+05:30")

@Controller
@RequestMapping("/v1")
public class ApportionController {

    private final ObjectMapper objectMapper;

    @Autowired
    private ApportionService apportionService;

    @Autowired
    public ApportionController(ObjectMapper objectMapper, ApportionService apportionService) {
        this.objectMapper = objectMapper;
        this.apportionService = apportionService;
    }


    /**
     * Executes the apportioning process on the given bills
     * @param tenantId The tenant ID from header
     * @param clientId The client ID from header  
     * @param apportionRequest The ApportionRequest containing the bill to be apportioned
     * @return Apportioned Bills
     */
    @RequestMapping(value="/_apportion", method = RequestMethod.POST)
    public ResponseEntity<ApportionResponse> apportionPost(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader("X-Client-ID") String clientId,
            @Valid @RequestBody ApportionRequest apportionRequest){
        
        List<Bill> billInfos = apportionService.apportionBills(apportionRequest, tenantId, clientId);
        ApportionResponse response = ApportionResponse.builder()
                .tenantId(tenantId)
                .bills(billInfos)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}