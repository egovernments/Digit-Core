package org.egov.infra.mdms.service;

import java.util.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.FormConfigCacheRepository;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.repository.querybuilder.FormConfigMdmsDataQueryBuilder;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.util.ObjectUtils;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Service
@Slf4j
public class MDMSService {

	private MdmsDataValidator mdmsDataValidator;

	private MdmsDataEnricher mdmsDataEnricher;

	private MdmsDataRepository mdmsDataRepository;

	private SchemaUtil schemaUtil;

	private MultiStateInstanceUtil multiStateInstanceUtil;

	private FormConfigCacheRepository formConfigCacheRepository;

	@Autowired
	public MDMSService(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
					   MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil,
					   FormConfigCacheRepository formConfigCacheRepository) {
		this.mdmsDataValidator = mdmsDataValidator;
		this.mdmsDataEnricher = mdmsDataEnricher;
		this.mdmsDataRepository = mdmsDataRepository;
		this.schemaUtil = schemaUtil;
		this.multiStateInstanceUtil = multiStateInstanceUtil;
		this.formConfigCacheRepository = formConfigCacheRepository;
	}

	/**
	 * This method processes the requests that come for master data creation.
	 * @param mdmsRequest
	 * @return
	 */
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

	/**
	 * This method processes the requests that come for master data search.
	 * @param mdmsCriteriaReq
	 * @return
	 */
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

		boolean hasFormConfigFilter = schemaCodes.containsKey(FORM_CONFIG_SCHEMA_CODE)
				&& schemaCodes.get(FORM_CONFIG_SCHEMA_CODE) != null;

		// Make a call to the repository layer to fetch data as per given criteria
		if (hasFormConfigFilter) {
			tenantMasterMap = searchFormConfigWithCache(mdmsCriteriaReq.getMdmsCriteria(), schemaCodes, tenantId);
		} else {
			tenantMasterMap = mdmsDataRepository.search(mdmsCriteriaReq.getMdmsCriteria());
		}

		// Apply in-memory JSONPath filters; skip FormConfig since filtering was already done at DB level
		Map<String, String> inMemoryFilterMap = new HashMap<>(schemaCodes);
		if (hasFormConfigFilter) {
			inMemoryFilterMap.remove(FORM_CONFIG_SCHEMA_CODE);
		}
		tenantMasterMap = applyFilterToData(tenantMasterMap, inMemoryFilterMap);

		// Perform fallback
		Map<String, JSONArray> masterDataMap = FallbackUtil.backTrackTenantMasterDataMap(tenantMasterMap, tenantId);

		// Return response in MDMS v1 search response format for backward compatibility
		return getModuleMasterMap(masterDataMap);
	}

	/**
	 * Fetches FormConfig master data, serving it from the Redis cache when possible and
	 * falling back to the database on a miss. Caching is applied only for FormConfig-only
	 * requests (single master) carrying a project filter; mixed/multi-schema requests and
	 * filters without a project bypass the cache and hit the database directly.
	 *
	 * @param mdmsCriteria the search criteria (tenantId already normalized to state level)
	 * @param schemaCodes  schemaCode -> filter expression map derived from the request
	 * @param cacheTenantId the exact incoming tenantId used as the cache key tenant segment
	 */
	private Map<String, Map<String, JSONArray>> searchFormConfigWithCache(MdmsCriteria mdmsCriteria,
																		  Map<String, String> schemaCodes, String cacheTenantId) {
		boolean isFormConfigOnly = schemaCodes.size() == 1 && schemaCodes.containsKey(FORM_CONFIG_SCHEMA_CODE);
		String project = isFormConfigOnly
				? FormConfigMdmsDataQueryBuilder.extractProject(schemaCodes.get(FORM_CONFIG_SCHEMA_CODE))
				: null;

		// Only requests scoped to a single FormConfig project are cacheable
		if (project == null) {
			return mdmsDataRepository.searchFormConfig(mdmsCriteria);
		}

		Map<String, Map<String, JSONArray>> cached =
				formConfigCacheRepository.get(cacheTenantId, FORM_CONFIG_SCHEMA_CODE, project);
		if (cached != null) {
			return cached;
		}

		Map<String, Map<String, JSONArray>> tenantMasterMap = mdmsDataRepository.searchFormConfig(mdmsCriteria);
		formConfigCacheRepository.put(cacheTenantId, FORM_CONFIG_SCHEMA_CODE, project, tenantMasterMap);
		return tenantMasterMap;
	}

	private Map<String, Map<String, JSONArray>> applyFilterToData(Map<String, Map<String, JSONArray>> tenantMasterMap, Map<String, String> schemaCodeFilterMap) {
		Map<String, Map<String, JSONArray>> tenantMasterMapPostFiltering = new HashMap<>();

		tenantMasterMap.keySet().forEach(tenantId -> {
			Map<String, JSONArray> schemaCodeVsFilteredMasters = new HashMap<>();
			tenantMasterMap.get(tenantId).keySet().forEach(schemaCode -> {
				JSONArray masters = tenantMasterMap.get(tenantId).get(schemaCode);
				if(!ObjectUtils.isEmpty(schemaCodeFilterMap.get(schemaCode))) {
					schemaCodeVsFilteredMasters.put(schemaCode, filterMasters(masters, schemaCodeFilterMap.get(schemaCode)));
				} else {
					schemaCodeVsFilteredMasters.put(schemaCode, masters);
				}
			});
			tenantMasterMapPostFiltering.put(tenantId, schemaCodeVsFilteredMasters);
		});

		return tenantMasterMapPostFiltering;
	}

	private JSONArray filterMasters(JSONArray masters, String filterExp) {
		JSONArray filteredMasters = JsonPath.read(masters, filterExp);
		return filteredMasters;
	}

	private Map<String, Map<String, JSONArray>> getModuleMasterMap(Map<String, JSONArray> masterMap) {
		Map<String, Map<String, JSONArray>> moduleMasterMap = new HashMap<>();

		for (Map.Entry<String, JSONArray> entry : masterMap.entrySet()) {
			String[] moduleMaster = entry.getKey().split(DOT_REGEX);
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
				String key = moduleDetail.getModuleName().concat(DOT_SEPARATOR).concat(masterDetail.getName());
				String value = masterDetail.getFilter();
				schemaCodesFilterMap.put(key, value);
			}
		}
		return schemaCodesFilterMap;
	}
}