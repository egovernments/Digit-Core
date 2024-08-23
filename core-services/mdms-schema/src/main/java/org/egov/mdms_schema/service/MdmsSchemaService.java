package org.egov.mdms_schema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms_schema.config.MdmsSchemaConfig;
import org.egov.mdms_schema.repository.ServiceRequestRepository;
import org.egov.mdms_schema.web.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MdmsSchemaService {

	private final ServiceRequestRepository repository;

	private final ObjectMapper mapper;

	private final MdmsSchemaConfig config;

	public MdmsSchemaService(ServiceRequestRepository repository, ObjectMapper mapper, MdmsSchemaConfig config) {
		this.repository = repository;
		this.mapper = mapper;
		this.config = config;
	}


	public void create(@Valid RequestInfo requestInfo, String tenantId) {
		//Load schema codes
		List<String> schemaCodes = Arrays.asList(config.getSchemaCodeList().split(","));
		if (schemaCodes.isEmpty()) {
			schemaCodes.add(config.getSchemaCodeList());
		}

		//Search for schemas codes in default tenantId
		SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder().tenantId(config.getDefaultTenantId()).codes(schemaCodes).build();
		SchemaDefSearchRequest schemaDefSearchRequest = SchemaDefSearchRequest.builder().requestInfo(requestInfo).schemaDefCriteria(schemaDefCriteria).build();

		Object mdmsV2SchemaSearchResponse = repository.fetchResult(getMdmsV2SchemaSearchUrl(), schemaDefSearchRequest);
		SchemaDefinitionResponse schemaSearchResponse = mapper.convertValue(mdmsV2SchemaSearchResponse, SchemaDefinitionResponse.class);
		List<SchemaDefinition> schemaDefinitionList = schemaSearchResponse.getSchemaDefinitions();

		Map<String, SchemaDefinition> schemaDefinitionMap = new HashMap<>();
		for (SchemaDefinition schemaDefinition : schemaDefinitionList) {
			String schemaCode = schemaDefinition.getCode();
			schemaDefinitionMap.put(schemaCode, schemaDefinition);
		}

		//Create schemas and their data in the given tenantId
		for(String schemaCode : schemaCodes) {
			SchemaDefinition schemaDefinition = schemaDefinitionMap.get(schemaCode);
			//Create schemas in the given tenantId
			schemaDefinition.setTenantId(tenantId);
			SchemaDefinitionRequest schemaDefinitionRequest = SchemaDefinitionRequest.builder().requestInfo(requestInfo).schemaDefinition(schemaDefinition).build();
			Object mdmsV2SchemaCreateResponse = repository.fetchResult(getMdmsV2SchemaCreateUrl(), schemaDefinitionRequest);
			SchemaDefinitionResponse schemaCreateResponse = mapper.convertValue(mdmsV2SchemaCreateResponse, SchemaDefinitionResponse.class);

			//Search data for the schema code in default tenetId
			List<Mdms> mdmsList = getAllMdmsResults(config.getDefaultTenantId(), schemaCode, requestInfo);

			//Create schema data in the given tenantId
			for(Mdms mdms : mdmsList) {
				mdms.setTenantId(tenantId);
				MdmsRequest mdmsRequest = MdmsRequest.builder().requestInfo(requestInfo).mdms(mdms).build();
				log.info(mdms.getSchemaCode() + " : " + mdms.getUniqueIdentifier());
				Object mdmsV2DataCreateResponse = repository.fetchResult(getMdmsV2DataCreateUrl(schemaDefinition.getCode()), mdmsRequest);
				MdmsResponseV2 dataCreateResponse = mapper.convertValue(mdmsV2DataCreateResponse, MdmsResponseV2.class);
			}
		}
	}

	public StringBuilder getMdmsV2SchemaCreateUrl() {
		return new StringBuilder().append(config.getMdmsV2LocalHost()).append(config.getMdmsV2SchemaCreate());
	}

	public StringBuilder getMdmsV2SchemaSearchUrl() {
		return new StringBuilder().append(config.getMdmsV2Host()).append(config.getMdmsV2SchemaSearch());
	}

	public StringBuilder getMdmsV2DataCreateUrl(String schemaCode) {
		return new StringBuilder().append(config.getMdmsV2LocalHost()).append(config.getMdmsV2DataCreate());
	}

	public StringBuilder getMdmsV2DataSearchUrl() {
		return new StringBuilder().append(config.getMdmsV2Host()).append(config.getMdmsV2DataSearch());
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
			Object mdmsV2DataSearchResponse = repository.fetchResult(getMdmsV2DataSearchUrl(), mdmsCriteriaReq);
			MdmsResponseV2 dataSearchResponse = mapper.convertValue(mdmsV2DataSearchResponse, MdmsResponseV2.class);
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
}
