package org.egov.infra.mdms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSService;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/v2")
public class MDMSControllerV2 {

    private MDMSServiceV2 mdmsServiceV2;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Autowired
    public MDMSControllerV2(ObjectMapper objectMapper, HttpServletRequest request, MDMSServiceV2 mdmsServiceV2) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.mdmsServiceV2 = mdmsServiceV2;
    }

    /**
     * Request handler for serving create requests
     * @param mdmsRequest
     * @param schemaCode
     * @return
     */
    @RequestMapping(value="_create/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponseV2> create(@Valid @RequestBody MdmsRequest mdmsRequest, @PathVariable("schemaCode") String schemaCode) {
        List<Mdms> masterDataList = mdmsServiceV2.create(mdmsRequest);
        MdmsResponseV2 mdmsCreateResponse = MdmsResponseV2.builder().mdms(masterDataList).build();
        return new ResponseEntity<>(mdmsCreateResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving search requests
     * @param masterDataSearchCriteria
     * @param schemaCode
     * @return
     */
    @RequestMapping(value="_search/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponseV2> search(@Valid @RequestBody MdmsCriteriaReqV2 masterDataSearchCriteria, @PathVariable("schemaCode") String schemaCode) {
        List<Mdms> masterDataList = mdmsServiceV2.search(masterDataSearchCriteria);
        MdmsResponseV2 mdmsCreateResponse = MdmsResponseV2.builder().mdms(masterDataList).build();
        return new ResponseEntity<>(mdmsCreateResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving update requests
     * @param mdmsRequest
     * @param schemaCode
     * @return
     */
    @RequestMapping(value="_update/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponseV2> update(@Valid @RequestBody MdmsRequest mdmsRequest, @PathVariable("schemaCode") String schemaCode) {
        List<Mdms> masterDataList = mdmsServiceV2.update(mdmsRequest);
        MdmsResponseV2 mdmsUpdateResponse = MdmsResponseV2.builder().mdms(masterDataList).build();
        return new ResponseEntity<>(mdmsUpdateResponse, HttpStatus.ACCEPTED);
    }
}
