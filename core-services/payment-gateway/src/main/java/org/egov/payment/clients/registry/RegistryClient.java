package org.egov.payment.clients.registry;

import org.egov.payment.clients.registry.models.DataSearchRequest;
import org.egov.payment.clients.registry.models.RegistryData;

import java.util.List;

public interface RegistryClient {

    List<RegistryData> search(String tenantId, String clientId, String schemaCode, DataSearchRequest request);
}
