package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.impl.MdmsRedisDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class MDMSServiceV2 {


    private MdmsDataValidator mdmsDataValidator;

    private MdmsDataEnricher mdmsDataEnricher;

    private MdmsDataRepository mdmsDataRepository;



    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository) {

        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
    }

   /* public JSONArray getFilterData(Map<String, String> schemaCodeFilterMap, Map<String, JSONArray> masterMap) {
		for(Map.Entry<String,String> entry : schemaCodeFilterMap.entrySet()) {
			masterMap.get(entry.getKey());
			JSONArray filteredMasters = JsonPath.read(masters, filterExp);
		}

        return filteredMasters;
    }*/

    public List<Mdms> create(MdmsRequest mdmsRequest) {
        // Performs validations on incoming request and returns unique identifier for the record if all validations pass
        String uniqueIdentifier = mdmsDataValidator.validate(mdmsRequest);

        // Enriches the incoming data
        mdmsDataEnricher.enrichCreateRequest(mdmsRequest, uniqueIdentifier);

        // Emit MDMS create event to be listened by persister
        mdmsDataRepository.create(mdmsRequest);

        return Arrays.asList(mdmsRequest.getMdms());
    }

    //TODO: APPLY filter on the data set
    public List<Mdms> searchV2(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
        log.info("Reading from DB");
        List<Mdms> mdmsList = mdmsDataRepository.searchV2(mdmsCriteriaReqV2.getMdmsCriteria());
        //mdmsRedisDataRepository.write(,masterMap);
        return mdmsList;
    }

    public List<Mdms> updateV2(MdmsRequest mdmsRequest) {
        mdmsDataValidator.validate(mdmsRequest);
        mdmsDataEnricher.enrichUpdateRequest(mdmsRequest);
        return null;
    }

}
