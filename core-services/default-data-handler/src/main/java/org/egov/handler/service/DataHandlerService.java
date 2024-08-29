package org.egov.handler.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.web.models.DefaultLocalizationDataRequest;
import org.egov.handler.web.models.DefaultMdmsDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final LocalizationUtil localizationUtil;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil, ServiceConfiguration serviceConfig) {
		this.mdmsV2Util = mdmsV2Util;
		this.localizationUtil = localizationUtil;
		this.serviceConfig = serviceConfig;
	}

	public void createDefaultData(RequestInfo requestInfo, String code) {
		DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
				.requestInfo(requestInfo)
				.targetTenantId(code)
				.schemaCodes(serviceConfig.getDefaultMdmsSchemaList())
				.build();
		mdmsV2Util.createMdmsData(defaultMdmsDataRequest);

		DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder()
				.requestInfo(requestInfo)
				.targetTenantId(code)
				.locale(serviceConfig.getDefaultLocalizationLocale())
				.modules(serviceConfig.getDefaultLocalizationModuleList())
				.build();

		localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
	}
}
