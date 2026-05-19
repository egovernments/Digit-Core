package org.egov.handler.util;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.Tenant;
import org.egov.handler.web.models.TenantConfigRequest;
import org.egov.handler.web.models.TenantConfigResponse;
import org.egov.handler.web.models.TenantSearchResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

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

	public List<String> fetchTenantCodesPage(RequestInfo requestInfo, int offset, int limit) {
		String uri = serviceConfig.getTenantSearchURI() + "?offset=" + offset + "&limit=" + limit;
		try {
			TenantSearchResponse response = restTemplate.postForObject(uri, requestInfo, TenantSearchResponse.class);
			if (response == null || response.getTenants() == null) {
				return new ArrayList<>();
			}
			List<String> codes = new ArrayList<>();
			for (Tenant tenant : response.getTenants()) {
				if (tenant.getCode() != null) {
					codes.add(tenant.getCode());
				}
			}
			return codes;
		} catch (Exception e) {
			log.error("Error fetching tenants at offset={}: {}", offset, e.getMessage());
			throw new CustomException("TENANT_SEARCH_FAILED", "Failed to fetch tenants at offset " + offset);
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
