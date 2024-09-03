package org.egov.infra.mdms.service;

import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.DefaultMasterDataRequest;
import org.egov.infra.mdms.repository.impl.DefaultDataRepository;
import org.egov.infra.mdms.repository.impl.DefaultSchemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultMasterDataService {

	private final DefaultSchemaRepository schemaRepository;

	private final DefaultDataRepository dataRepository;

	private final ApplicationConfig config;

	public DefaultMasterDataService(DefaultSchemaRepository schemaRepository, DefaultDataRepository dataRepository, ApplicationConfig config) {
		this.schemaRepository = schemaRepository;
		this.dataRepository = dataRepository;
		this.config = config;
	}

	public void create(DefaultMasterDataRequest defaultMasterDataRequest) {
		String targetTenantId = defaultMasterDataRequest.getTargetTenantId();
		List<String> schemaCodes = defaultMasterDataRequest.getSchemaCodes();

		schemaRepository.copySchemaDefinitions(config.getDefaultTenantId(), targetTenantId, schemaCodes);

		if (Boolean.FALSE.equals(defaultMasterDataRequest.getOnlySchemas())) {
			dataRepository.copySchemaData(config.getDefaultTenantId(), targetTenantId, schemaCodes);
		}
	}

}
