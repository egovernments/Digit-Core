package org.egov.handler.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MdmsV2Util {

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public MdmsV2Util(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
		this.restTemplate = restTemplate;
		this.serviceConfig = serviceConfig;
	}

	public void createDefaultMdmsData(DefaultMdmsDataRequest defaultMdmsDataRequest) {

		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDefaultDataCreateURI());
		try {
			restTemplate.postForObject(uri.toString(), defaultMdmsDataRequest, ResponseInfo.class);
		} catch (Exception e) {
			log.error("Error creating default MDMS data for {} : {}", defaultMdmsDataRequest.getTargetTenantId(), e.getMessage());
			throw new CustomException("MDMS_DEFAULT_DATA_CREATE_FAILED", "Failed to create default mdms data for " + defaultMdmsDataRequest.getTargetTenantId() + " : " + e.getMessage());
		}
	}

	public void createMdmsSchema(SchemaDefinitionRequest schemaDefinitionRequest) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsSchemaCreateURI());

		try {
			restTemplate.postForObject(uri.toString(), schemaDefinitionRequest, SchemaDefinitionResponse.class);
		} catch (Exception e) {
			log.error("Error creating MDMS schema for {} : {}", schemaDefinitionRequest.getSchemaDefinition().getTenantId(), e.getMessage());
			throw new CustomException("MDMS_SCHEMA_CREATE_FAILED", "Failed to create mdms schema for " + schemaDefinitionRequest.getSchemaDefinition().getTenantId() + " : " + e.getMessage());
		}
	}

	public SchemaDefinitionResponse searchMdmsSchema(SchemaDefSearchRequest schemaDefSearchRequest) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsSchemaSearchURI());

		try {
			return restTemplate.postForObject(uri.toString(), schemaDefSearchRequest, SchemaDefinitionResponse.class);
		} catch (Exception e) {
			log.error("Error searching MDMS schema for {} : {}", schemaDefSearchRequest.getSchemaDefCriteria().getTenantId(), e.getMessage());
			throw new CustomException("MDMS_SCHEMA_SEARCH_FAILED", "Failed to search mdms schema for " + schemaDefSearchRequest.getSchemaDefCriteria().getTenantId() + " : " + e.getMessage());
		}
	}

	public void createMdmsData(MdmsRequest mdmsRequest) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDataCreateURI());
		uri.append("/");
		uri.append(mdmsRequest.getMdms().getSchemaCode());

		try {
			restTemplate.postForObject(uri.toString(), mdmsRequest, MdmsResponseV2.class);
		} catch (Exception e) {
			if (e instanceof HttpClientErrorException httpException && (httpException.getStatusCode().value() == 400)) {
					String responseBody = httpException.getResponseBodyAsString();
					JsonNode errorNode = parseErrorResponse(responseBody);
					if (errorNode != null && isDuplicateRecordError(errorNode)) {
						log.error("Duplicate record error for schema {}: {}", mdmsRequest.getMdms().getSchemaCode(), httpException.getMessage());
						return; // No need to throw further if it's a duplicate record
					}
			}
			log.error("Error creating MDMS data for {} : {}", mdmsRequest.getMdms().getTenantId(), e.getMessage());
			throw new CustomException("MDMS_DATA_CREATE_FAILED", "Failed to create mdms data for " + mdmsRequest.getMdms().getTenantId() + " : " + e.getMessage());
		}
	}

	public MdmsResponseV2 searchMdmsData(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getMdmsDataSearchURI());

		try {
			return restTemplate.postForObject(uri.toString(), mdmsCriteriaReqV2, MdmsResponseV2.class);
		} catch (Exception e) {
			log.error("Error searching MDMS data for {} : {}", mdmsCriteriaReqV2.getMdmsCriteria().getTenantId(), e.getMessage());
			throw new CustomException("MDMS_DATA_SEARCH_FAILED", "Failed to search mdms data for " + mdmsCriteriaReqV2.getMdmsCriteria().getTenantId() + " : " + e.getMessage());
		}
	}

	private JsonNode parseErrorResponse(String responseBody) {
		try {
			return new ObjectMapper().readTree(responseBody);
		} catch (Exception e) {
			log.error("Failed to parse error response body: {}", responseBody);
			return null;
		}
	}

	private boolean isDuplicateRecordError(JsonNode errorNode) {
		JsonNode errors = errorNode.path("Errors");
		if (errors.isArray()) {
			for (JsonNode error : errors) {
				if ("DUPLICATE_RECORD".equals(error.path("code").asText())) {
					return true;
				}
			}
		}
		return false;
	}
}
