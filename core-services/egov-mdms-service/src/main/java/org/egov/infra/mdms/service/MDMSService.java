package org.egov.infra.mdms.service;

import java.util.*;

import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
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

	private SchemaUtil schemaUtil;

	private MultiStateInstanceUtil multiStateInstanceUtil;

	@Autowired
	public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
					   MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil) {
		this.mdmsDataValidator = mdmsDataValidator;
		this.mdmsDataEnricher = mdmsDataEnricher;
		this.mdmsDataRepository = mdmsDataRepository;
		this.schemaUtil = schemaUtil;
		this.multiStateInstanceUtil = multiStateInstanceUtil;
	}

	public List<Mdms> create(MdmsRequest mdmsRequest) {

		// Fetch schema against which data is getting created
		JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

		// Validate incoming request
		mdmsDataValidator.validateCreateRequest(mdmsRequest, schemaObject);

		// Enrich incoming request
		mdmsDataEnricher.enrichCreateRequest(mdmsRequest, schemaObject);

		// Emit mdms creation request event
		mdmsDataRepository.create(mdmsRequest);

		return Arrays.asList(mdmsRequest.getMdms());
	}

	public Map<String, Map<String, JSONArray>> search(MdmsCriteriaReq mdmsCriteriaReq) {
		Map<String, Map<String, JSONArray>> tenantMasterMap = new HashMap<>();

		/*
		 * Set incoming tenantId as state level tenantId for fallback in case master data for
		 * concrete tenantId does not exist.
		 */
		String tenantId = new StringBuilder(mdmsCriteriaReq.getMdmsCriteria().getTenantId()).toString();
		mdmsCriteriaReq.getMdmsCriteria().setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));

		Map<String, String> schemaCodes = getSchemaCodes(mdmsCriteriaReq.getMdmsCriteria());
		mdmsCriteriaReq.getMdmsCriteria().setSchemaCodeFilterMap(schemaCodes);

		tenantMasterMap = mdmsDataRepository.search(mdmsCriteriaReq.getMdmsCriteria());

		Map<String, JSONArray> masterDataMap = FallbackUtil.backTrackTenantMasterDataMap(tenantMasterMap, tenantId);

		return getModuleMasterMap(masterDataMap);
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