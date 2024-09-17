package org.egov.infra.mdms.service;

import org.egov.infra.mdms.model.DefaultMasterDataRequest;
import org.egov.infra.mdms.repository.impl.DefaultDataRepository;
import org.egov.infra.mdms.repository.impl.DefaultSchemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultMasterDataService {

	private final DefaultSchemaRepository schemaRepository;

	private final DefaultDataRepository dataRepository;

	public DefaultMasterDataService(DefaultSchemaRepository schemaRepository, DefaultDataRepository dataRepository) {
		this.schemaRepository = schemaRepository;
		this.dataRepository = dataRepository;
	}

	public void create(DefaultMasterDataRequest defaultMasterDataRequest) {
		String targetTenantId = defaultMasterDataRequest.getTargetTenantId();
		List<String> schemaCodes = defaultMasterDataRequest.getSchemaCodes();
		String defaultTenantId = defaultMasterDataRequest.getDefaultTenantId();

		schemaRepository.copySchemaDefinitions(defaultTenantId, targetTenantId, schemaCodes);

		if (Boolean.FALSE.equals(defaultMasterDataRequest.getOnlySchemas())) {
			dataRepository.copySchemaData(defaultTenantId, targetTenantId, schemaCodes);
		}
	}

}
