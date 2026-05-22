package org.egov.pg.clients.registry;

import org.egov.pg.clients.registry.models.DataSearchRequest;
import org.egov.pg.clients.registry.models.RegistryData;

import java.util.List;

public interface RegistryClient {

	List<RegistryData> search(String tenantId, String userId, String schemaCode, DataSearchRequest request);
}
