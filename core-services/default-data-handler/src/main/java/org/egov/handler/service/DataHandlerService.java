package org.egov.handler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.util.WorkflowUtil;
import org.egov.handler.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.egov.handler.config.ServiceConstants.TENANT_BOUNDARY_SCHEMA;

@Slf4j
@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final LocalizationUtil localizationUtil;

	private final TenantManagementUtil tenantManagementUtil;

	private final ServiceConfiguration serviceConfig;

	private final ObjectMapper objectMapper;

	private final ResourceLoader resourceLoader;

	private final WorkflowUtil workflowUtil;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util,
							  LocalizationUtil localizationUtil,
							  TenantManagementUtil tenantManagementUtil,
							  ServiceConfiguration serviceConfig,
							  ObjectMapper objectMapper,
							  ResourceLoader resourceLoader,
							  WorkflowUtil workflowUtil) {
		this.mdmsV2Util = mdmsV2Util;
		this.localizationUtil = localizationUtil;
		this.tenantManagementUtil = tenantManagementUtil;
		this.serviceConfig = serviceConfig;
		this.objectMapper = objectMapper;
		this.resourceLoader = resourceLoader;
		this.workflowUtil = workflowUtil;
	}

	public void createDefaultData(DefaultDataRequest defaultDataRequest) {
		if (defaultDataRequest.getSchemaCodes() != null) {
			if (defaultDataRequest.getSchemaCodes().contains(TENANT_BOUNDARY_SCHEMA)) {
				createTenantBoundarydata(defaultDataRequest.getRequestInfo(), defaultDataRequest.getTargetTenantId());
				defaultDataRequest.getSchemaCodes().remove(TENANT_BOUNDARY_SCHEMA);
			}
			DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.schemaCodes(defaultDataRequest.getSchemaCodes())
					.onlySchemas(defaultDataRequest.getOnlySchemas())
					.build();
			mdmsV2Util.createDefaultMdmsData(defaultMdmsDataRequest);
		}

		if (defaultDataRequest.getLocale() != null && defaultDataRequest.getModules() != null) {
			DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.locale(defaultDataRequest.getLocale())
					.modules(defaultDataRequest.getModules())
					.build();
			localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
		}
	}

	private void createTenantBoundarydata(RequestInfo requestInfo, String targetTenantId) {
		List<String> schemaCodes = new ArrayList<>(Collections.singletonList(TENANT_BOUNDARY_SCHEMA));

		SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder()
				.tenantId(serviceConfig.getDefaultTenantId())
				.codes(schemaCodes)
				.build();
		SchemaDefSearchRequest schemaDefSearchRequest = SchemaDefSearchRequest.builder()
				.requestInfo(requestInfo)
				.schemaDefCriteria(schemaDefCriteria)
				.build();
		SchemaDefinitionResponse schemaSearchResponse = mdmsV2Util.searchMdmsSchema(schemaDefSearchRequest);
		List<SchemaDefinition> schemaDefinitionList = schemaSearchResponse.getSchemaDefinitions();
		for (SchemaDefinition schemaDefinition : schemaDefinitionList) {
			schemaDefinition.setTenantId(targetTenantId);
			SchemaDefinitionRequest schemaDefinitionRequest = SchemaDefinitionRequest.builder()
					.requestInfo(requestInfo)
					.schemaDefinition(schemaDefinition)
					.build();
			mdmsV2Util.createMdmsSchema(schemaDefinitionRequest);
			//Search data for the schema code in default tenetId
			List<Mdms> mdmsList = getAllMdmsResults(serviceConfig.getDefaultTenantId(), schemaDefinition.getCode(), requestInfo);
			//Create schema data in the given tenantId
			for (Mdms mdms : mdmsList) {
				mdms.setTenantId(targetTenantId);

				JsonNode dataNode = mdms.getData();
				if (dataNode.has("boundary")) {
					// Cast the 'boundary' node to ObjectNode so that we can modify it
					ObjectNode boundaryNode = (ObjectNode) dataNode.get("boundary");

					// Modify the 'code' field within the 'boundary' node
					boundaryNode.put("code", targetTenantId);

					// Set the modified 'data' back to the Mdms object (optional, since it's mutable)
					((ObjectNode) dataNode).set("boundary", boundaryNode);
					mdms.setData(dataNode);
				} else {
					log.info("Boundary node does not exist in the data.");
				}
				MdmsRequest mdmsRequest = MdmsRequest.builder().requestInfo(requestInfo).mdms(mdms).build();
				log.info("{} : {}", mdms.getSchemaCode(), mdms.getUniqueIdentifier());
				mdmsV2Util.createMdmsData(mdmsRequest);
			}
		}
	}

	// Method to get all search results with pagination
	public List<Mdms> getAllMdmsResults(String tenantId, String schemaCode, RequestInfo requestInfo) {
		List<Mdms> allMdmsResults = new ArrayList<>();
		int limit = 100; // Default limit
		int offset = 0; // Default offset

		while (true) {
			// Create MdmsCriteriaV2 with current offset and limit
			MdmsCriteriaV2 mdmsCriteria = MdmsCriteriaV2.builder()
					.tenantId(tenantId)
					.schemaCode(schemaCode)
					.offset(offset)
					.limit(limit)
					.build();

			MdmsCriteriaReqV2 mdmsCriteriaReq = MdmsCriteriaReqV2.builder()
					.requestInfo(requestInfo)
					.mdmsCriteria(mdmsCriteria)
					.build();

			// Fetch results from the repository
			MdmsResponseV2 dataSearchResponse = mdmsV2Util.searchMdmsData(mdmsCriteriaReq);
			List<Mdms> mdmsList = dataSearchResponse.getMdms();

			// Add the current batch of results to the list
			allMdmsResults.addAll(mdmsList);

			// Check if there are fewer results than the limit; if so, this is the last page
			if (mdmsList.size() < limit) {
				break;
			}

			// Update offset for the next batch
			offset += limit;
		}
		return allMdmsResults;
	}

	public void createTenantConfig(TenantRequest tenantRequest) {
		TenantConfigResponse tenantConfigSearchResponse = tenantManagementUtil.searchTenantConfig(serviceConfig.getDefaultTenantId(), tenantRequest.getRequestInfo());
		List<TenantConfig> tenantConfigList = tenantConfigSearchResponse.getTenantConfigs();

		for (TenantConfig tenantConfig : tenantConfigList) {
			// Set code and name according to target tenant
			tenantConfig.setCode(tenantRequest.getTenant().getCode());
			tenantConfig.setName(tenantRequest.getTenant().getName());

			TenantConfigRequest tenantConfigRequest = TenantConfigRequest.builder()
					.requestInfo(tenantRequest.getRequestInfo())
					.tenantConfig(tenantConfig)
					.build();

			tenantManagementUtil.createTenantConfig(tenantConfigRequest);
		}
	}

	public DefaultDataRequest setupDefaultData(DataSetupRequest dataSetupRequest) {
		if (Objects.equals(dataSetupRequest.getModule(), "PGR")) {
			createPgrWorkflowConfig(dataSetupRequest.getTargetTenantId());
		}

		Set<String> uniqueIdentifiers = new HashSet<>();
		uniqueIdentifiers.add(dataSetupRequest.getModule());

		MdmsCriteriaV2 mdmsCriteriaV2 = MdmsCriteriaV2.builder()
				.tenantId(serviceConfig.getDefaultTenantId())
				.schemaCode(serviceConfig.getModuleMasterConfig())
				.uniqueIdentifiers(uniqueIdentifiers)
				.build();
		MdmsCriteriaReqV2 mdmsCriteriaReqV2 = MdmsCriteriaReqV2.builder()
				.requestInfo(dataSetupRequest.getRequestInfo())
				.mdmsCriteria(mdmsCriteriaV2)
				.build();

		MdmsResponseV2 mdmsResponseV2 = mdmsV2Util.searchMdmsData(mdmsCriteriaReqV2);
		List<Mdms> mdmsList = mdmsResponseV2.getMdms();

		List<String> schemaCodes = new ArrayList<>();
		List<String> modules = new ArrayList<>();

		for (Mdms mdms : mdmsList) {
			try {
				MdmsData mdmsData = objectMapper.treeToValue(mdms.getData(), MdmsData.class);
				processMasterList(mdmsData.getMasterList(), schemaCodes, modules);
			} catch (IOException e) {
				log.error("Failed to parse MDMS data :{}", mdms.getData(), e);
				throw new CustomException("MDMS_DATA_PARSE_FAILED", "Failed to parse mdms data ");
			}
		}

		DefaultDataRequest defaultDataRequest = DefaultDataRequest.builder()
				.requestInfo(dataSetupRequest.getRequestInfo())
				.targetTenantId(dataSetupRequest.getTargetTenantId())
				.schemaCodes(schemaCodes)
				.onlySchemas(dataSetupRequest.getOnlySchemas())
				.build();

		try {
			createDefaultData(defaultDataRequest);
		} catch (Exception e) {
			log.error("Failed to create default data for : {}", dataSetupRequest.getTargetTenantId(), e);
			throw new CustomException("DEFAULT_DATA_CREATE_FAILED", "Failed to create default data ");
		}
		return defaultDataRequest;
	}

	private void createPgrWorkflowConfig(String targetTenantId) {
		// Load the JSON file
		Resource resource = resourceLoader.getResource("classpath:PgrWorkflowConfig.json");
		try (InputStream inputStream = resource.getInputStream()) {
			BusinessServiceRequest businessServiceRequest = objectMapper.readValue(inputStream, BusinessServiceRequest.class);
			businessServiceRequest.getBusinessServices().forEach(service -> service.setTenantId(targetTenantId));
			workflowUtil.createWfConfig(businessServiceRequest);
		} catch (IOException e) {
			throw new CustomException("IO_EXCEPTION", "Error reading or mapping JSON file: " + e.getMessage());
		}
	}

	private void processMasterList(List<Master> masterList, List<String> schemaCodes, List<String> modules) {
		for (Master master : masterList) {
			if ("module".equals(master.getType()) ||
					"common".equals(master.getType()) ||
					"config".equals(master.getType())) {
				schemaCodes.add(master.getCode());
			} else if ("localisation".equals(master.getType())) {
				modules.add(master.getCode());
			}
		}
	}
}
