package org.egov.handler.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.TenantConfigRequest;
import org.egov.handler.web.models.TenantConfigResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class TenantManagementUtil {

	private final RestTemplate restTemplate;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public TenantManagementUtil(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
		this.restTemplate = restTemplate;
		this.serviceConfig = serviceConfig;
	}

	public TenantConfigResponse searchTenantConfig(String code, RequestInfo requestInfo) {
		// Building URI with query parameters
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getTenantConfigSearchURI());

		if (!code.isEmpty()) {
			uri.append("?code=");
			uri.append(code);
		}

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder()
				.requestInfo(requestInfo)
				.build();

		try {
			return restTemplate.postForObject(uri.toString(), requestInfoWrapper, TenantConfigResponse.class);
		} catch (Exception e) {
			log.error("Error searching tenant config for {}", code);
			throw new CustomException("TENANT_CONFIG_SEARCH_FAILED", "Failed to search the tenant config for " + code);
		}
	}

	public TenantConfigResponse createTenantConfig(TenantConfigRequest tenantConfigRequest) {
		StringBuilder uri = new StringBuilder();
		uri.append(serviceConfig.getTenantConfigCreateURI());
		try {
			return restTemplate.postForObject(uri.toString(), tenantConfigRequest, TenantConfigResponse.class);

		} catch (Exception e) {
			log.error("Error creating default tenant config for {}", tenantConfigRequest.getTenantConfig().getCode());
			throw new CustomException("TENANT_CONFIG_CREATE_FAILED", "Failed to create the tenant config for " + tenantConfigRequest.getTenantConfig().getCode());
		}
	}
}
