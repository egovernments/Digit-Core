package org.egov.infra.mdms.service;

import java.util.*;

import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class MDMSService {


	private MdmsDataValidator mdmsDataValidator;

	private MdmsDataEnricher mdmsDataEnricher;

	private MdmsDataRepository mdmsDataRepository;


	@Autowired
	public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
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
		String uniqueIdentifier = mdmsDataValidator.validate(mdmsRequest);
		mdmsDataEnricher.enrichCreateRequest(mdmsRequest, uniqueIdentifier);
		mdmsDataRepository.create(mdmsRequest);
		return Arrays.asList(mdmsRequest.getMdms());
	}

	public Map<String, Map<String, JSONArray>> search(MdmsCriteriaReq mdmsCriteriaReq) {
		Map<String, JSONArray> masterMap = new HashMap<>();
		Map<String, String> schemaCodes = getSchemaCodes(mdmsCriteriaReq.getMdmsCriteria());
		mdmsCriteriaReq.getMdmsCriteria().setSchemaCodeFilterMap(schemaCodes);
		log.info("Reading from DB");
		masterMap = mdmsDataRepository.search(mdmsCriteriaReq.getMdmsCriteria());
			//mdmsRedisDataRepository.write(,masterMap);
		return getModuleMasterMap(masterMap);
	}



	public void update(MdmsRequest mdmsRequest) {
	}

	private Map<String, Map<String, JSONArray>> getModuleMasterMap(Map<String, JSONArray> masterMap) {
		Map<String, Map<String, JSONArray>> moduleMasterMap = new HashMap<>();

		for (Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
			String[] moduleMaster = entry.getKey().split("\\.");
			String moduleName = moduleMaster[0];
			String masterName = moduleMaster[1];

			moduleMasterMap.computeIfAbsent(moduleName, k -> new HashMap<>())
					.put(masterName, entry.getValue());
		}
		return moduleMasterMap;
	}

	private Map<String, String> getSchemaCodes(MdmsCriteria mdmsCriteria) {
		Map<String, String> schemaCodesFilterMap = new HashMap<>();
		for (ModuleDetail moduleDetail : mdmsCriteria.getModuleDetails()) {
			for (MasterDetail masterDetail : moduleDetail.getMasterDetails()) {
				String key = moduleDetail.getModuleName().concat(".").concat(masterDetail.getName());
				String value = masterDetail.getFilter();
				schemaCodesFilterMap.put(key, value);
			}
		}
		return schemaCodesFilterMap;
	}
}