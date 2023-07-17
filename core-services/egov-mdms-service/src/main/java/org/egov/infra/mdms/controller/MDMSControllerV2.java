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

    @RequestMapping(value="_create/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponseV2> create(@Parameter(in = ParameterIn.DEFAULT, description = "Data create request", required=true, schema=@Schema()) @Valid @RequestBody MdmsRequest mdmsRequest, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        List<Mdms> mdmsList = mdmsServiceV2.create(mdmsRequest);
        MdmsResponseV2 mdmsCreateResponse = MdmsResponseV2.builder().mdms(mdmsList).build();
        return new ResponseEntity<MdmsResponseV2>(mdmsCreateResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="_search/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<?> search(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required=true, schema=@Schema()) @Valid @RequestBody MdmsCriteriaReqV2 body, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        List<Mdms>  mdmsList = mdmsServiceV2.searchV2(body);
        MdmsResponseV2 mdmsCreateResponse = MdmsResponseV2.builder().mdms(mdmsList).build();
                //.responseInfo(ResponseInfoUtil.buildResponseInfo(body.getRequestInfo(),"v1",HttpStatus.OK.toString()))

        return new ResponseEntity<>(mdmsCreateResponse, HttpStatus.OK);
    }

    @RequestMapping(value="_update/{schemaCode}", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponse> update(@Parameter(in = ParameterIn.DEFAULT, description = "Details of module and master which need to be search using MDMS .", required=true, schema=@Schema()) @Valid @RequestBody MdmsRequest body, @Parameter(in = ParameterIn.PATH, description = "Unique schema code from schema master", required=true, schema=@Schema()) @PathVariable("schemaCode") String schemaCode) {
        return new ResponseEntity<MdmsResponse>(HttpStatus.NOT_IMPLEMENTED);
    }
}
