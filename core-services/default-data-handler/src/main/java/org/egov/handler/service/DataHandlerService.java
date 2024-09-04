package org.egov.handler.service;

import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.util.TenantManagementUtil;
import org.egov.handler.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final LocalizationUtil localizationUtil;

	private final TenantManagementUtil tenantManagementUtil;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil, TenantManagementUtil tenantManagementUtil, ServiceConfiguration serviceConfig) {
		this.mdmsV2Util = mdmsV2Util;
		this.localizationUtil = localizationUtil;
		this.tenantManagementUtil = tenantManagementUtil;
		this.serviceConfig = serviceConfig;
	}

	public void createDefaultData(DefaultDataRequest defaultDataRequest) {
		if (defaultDataRequest.getSchemaCodes() != null) {
			DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.schemaCodes(defaultDataRequest.getSchemaCodes())
					.onlySchemas(defaultDataRequest.getOnlySchemas())
					.build();
			mdmsV2Util.createMdmsData(defaultMdmsDataRequest);
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
}
