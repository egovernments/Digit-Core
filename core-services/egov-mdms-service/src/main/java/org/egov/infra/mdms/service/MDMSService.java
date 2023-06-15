package org.egov.infra.mdms.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.MDMSApplicationRunnerImpl;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.impl.MdmsDataRepositoryImpl;
import org.egov.infra.mdms.repository.impl.MdmsRedisDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.mdms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class MDMSService {


	private MdmsDataValidator mdmsDataValidator;

	private MdmsDataEnricher mdmsDataEnricher;

	private MdmsDataRepository mdmsDataRepository;

	private MdmsRedisDataRepository mdmsRedisDataRepository;


	@Autowired
	public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
					   MdmsDataRepository mdmsDataRepository, MdmsRedisDataRepository mdmsRedisDataRepository) {

		this.mdmsDataValidator = mdmsDataValidator;
		this.mdmsDataEnricher = mdmsDataEnricher;
		this.mdmsDataRepository = mdmsDataRepository;
		this.mdmsRedisDataRepository = mdmsRedisDataRepository;
	}



	/**
	 * Service method to collect master data from tenantIdMap and apply filter as per the request
	 * 
	 * @param mdmsCriteriaReq
	 * @return Map<String, Map<String, JSONArray>> masterData
	 */
	public Map<String, Map<String, JSONArray>> searchMaster(MdmsCriteriaReq mdmsCriteriaReq) {

		Map<String, Map<String, Map<String, JSONArray>>> tenantIdMap = MDMSApplicationRunnerImpl.getTenantMap();

		String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();
		log.info(" Incoming tenantid : " + tenantId);
		
		/* 
		 * local tenantId replica for backtracking to parent tenant when child tenant is empty
		 */
		String tenantIdWithData = tenantId;

		int countOfSubTenant = StringUtils.countOccurrencesOf(tenantId, ".");
		Map<String, Map<String, JSONArray>> tenantData = tenantIdMap.get(tenantId);
		Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();

		/*
		 * if the tenantId doesn't contain a separator
		 */
		if (countOfSubTenant == 0) {

			if (tenantData != null) {
				getDataForTenatId(mdmsCriteriaReq, tenantIdWithData, responseMap);
			}
		} else {
			/*
			 * if the tenantId contains separator, it will be backtracked until a tenant
			 * with data is found
			 */
			for (int i = countOfSubTenant; i >= 0; i--) {

				/*
				 * pick new tenantId data only from the second loop
				 */
				if (i < countOfSubTenant)
					tenantData = tenantIdMap.get(tenantIdWithData);

				if (tenantData == null) {
					/*
					 * trim the tenantId by "." separator to take the parent tenantId
					 */
					tenantIdWithData = tenantIdWithData.substring(0, tenantIdWithData.lastIndexOf("."));
				} else {
					getDataForTenatId(mdmsCriteriaReq, tenantIdWithData, responseMap);
					break;
				}
			}
		}
		return responseMap;
	}

	/**
	 * method to filter module & master data from the given tenantId data
	 * 
	 * @param mdmsCriteriaReq
	 * @param tenantId
	 * @param responseMap
	 */
	public void getDataForTenatId(MdmsCriteriaReq mdmsCriteriaReq, String tenantId,
			Map<String, Map<String, JSONArray>> responseMap) {

		List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
        for (ModuleDetail moduleDetail : moduleDetails) {
        	
            List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();
            Map<String, JSONArray> finalMasterMap = new HashMap<>();

            for (MasterDetail masterDetail : masterDetails) {
            	
				JSONArray masterData = null;
				try {
					masterData = getMasterDataFromTenantData(moduleDetail.getModuleName(), masterDetail.getName(),
							tenantId);
				} catch (Exception e) {
					log.error("Exception occurred while reading master data", e);
				}
				
				if (masterData == null)
					continue;

                if (masterDetail.getFilter() != null)
                    masterData = filterMaster(masterData, masterDetail.getFilter());

                finalMasterMap.put(masterDetail.getName(), masterData);
            }
            responseMap.put(moduleDetail.getModuleName(), finalMasterMap);
        }
	}

	/**
	 * Method to collect master data from module data
	 * Automatically backtracks to parent tenant if data is not found for the master for the given tenantId
	 * 
	 * @param moduleName
	 * @param masterName
	 * @param tenantId
	 * @return {@link JSONArray} jsonArray 
	 * @throws Exception
	 */
	private JSONArray getMasterDataFromTenantData(String moduleName, String masterName, String tenantId)
			throws Exception {

		JSONArray jsonArray = null;
		/*
		 * local tenantId for backtracking parent tenant if data not available for given master
		 */
		String localTenantId = tenantId;
		Map<String, Map<String, Map<String, JSONArray>>> tenantIdMap = MDMSApplicationRunnerImpl.getTenantMap();
		Map<String, Map<String, JSONArray>> data;

		int subTenatCount = StringUtils.countOccurrencesOf(tenantId, ".");

		for (int i = subTenatCount; i >= 0; i--) {

			data = tenantIdMap.get(localTenantId);
			if (data != null && data.get(moduleName) != null && data.get(moduleName).get(masterName) != null) {
				
				jsonArray = data.get(moduleName).get(masterName);
				/*
				 * break and stop backtracking if data is found
				 */
				break;
			} else {
				/*
				 * trim the tenantId by "." separator to take the parent tenantId
				 */
				if(localTenantId.contains("."))
					localTenantId = localTenantId.substring(0, localTenantId.lastIndexOf("."));
			}
		}

		log.info("ModuleName.... " + moduleName + " : MasterName.... " + masterName);
		return jsonArray;
	}

	/*
	 * Disabled isStateLevel tenant key 
	 * and enabled backtracking for data true by default
	 */
	
//	public Boolean isMasterBacktracingEnabled(String moduleName, String masterName) {
//		Map<String, Map<String, Object>> masterConfigMap = MDMSApplicationRunnerImpl.getMasterConfigMap();
//
//		Map<String, Object> moduleData = masterConfigMap.get(moduleName);
//		Boolean isStateLevel = false;
//
//		Object masterData = null;
//		if (moduleData != null)
//			masterData = moduleData.get(masterName);
//
//		if (null != masterData) {
//			try {
//				isStateLevel = JsonPath.read(mapper.writeValueAsString(masterData), MDMSConstants.STATE_LEVEL_JSONPATH);
//			} catch (Exception e) {
//				log.error("isStateLevelEnabled field missing default false value will be set");
//			}
//		}
//		return isStateLevel;
//	}

    public JSONArray filterMaster(JSONArray masters, String filterExp) {
        JSONArray filteredMasters = JsonPath.read(masters, filterExp);
        return filteredMasters;
    }

	public List<Mdms> create(MdmsRequest mdmsRequest) {
		mdmsDataValidator.validate(mdmsRequest);
		mdmsDataEnricher.enrichCreateReq(mdmsRequest);
		mdmsDataRepository.create(mdmsRequest);
		return Arrays.asList(mdmsRequest.getMdms());
	}

	public Map<String, Map<String, JSONArray>> search(MdmsCriteriaReq mdmsCriteriaReq) {
		Map<String, String> schemaCodeMap = getSchemaCodes(mdmsCriteriaReq.getMdmsCriteria());
		Map<String, JSONArray> masterMap = mdmsDataRepository.search(schemaCodeMap.keySet());
		return getModuleMasterMap(masterMap);
	}
	public void update(MdmsRequest mdmsRequest) {

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
}