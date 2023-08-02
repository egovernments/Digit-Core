package org.egov.infra.mdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/v2")
public class MDMSControllerV2 {

    private MDMSServiceV2 mdmsServiceV2;

    @Autowired
    public MDMSControllerV2(MDMSServiceV2 mdmsServiceV2) {
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
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(mdmsRequest.getRequestInfo(), masterDataList), HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving search requests
     * @param masterDataSearchCriteria
     * @return
     */
    @RequestMapping(value="_search", method = RequestMethod.POST)
    public ResponseEntity<MdmsResponseV2> search(@Valid @RequestBody MdmsCriteriaReqV2 masterDataSearchCriteria) {
        List<Mdms> masterDataList = mdmsServiceV2.search(masterDataSearchCriteria);
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(RequestInfo.builder().build(), masterDataList), HttpStatus.OK);
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
        return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(mdmsRequest.getRequestInfo(), masterDataList), HttpStatus.ACCEPTED);
    }
}
