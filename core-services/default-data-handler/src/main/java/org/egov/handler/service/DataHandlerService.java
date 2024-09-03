package org.egov.handler.service;

import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.web.models.DefaultDataRequest;
import org.egov.handler.web.models.DefaultLocalizationDataRequest;
import org.egov.handler.web.models.DefaultMdmsDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final LocalizationUtil localizationUtil;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util, LocalizationUtil localizationUtil) {
		this.mdmsV2Util = mdmsV2Util;
		this.localizationUtil = localizationUtil;
	}

	public void createDefaultData(DefaultDataRequest defaultDataRequest) {
		if (defaultDataRequest.getSchemaCodes() != null) {
			DefaultMdmsDataRequest defaultMdmsDataRequest = DefaultMdmsDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.schemaCodes(defaultDataRequest.getSchemaCodes())
					.build();
			mdmsV2Util.createMdmsData(defaultMdmsDataRequest);
		}

		if(defaultDataRequest.getLocale() != null && defaultDataRequest.getModules() != null) {
			DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder()
					.requestInfo(defaultDataRequest.getRequestInfo())
					.targetTenantId(defaultDataRequest.getTargetTenantId())
					.locale(defaultDataRequest.getLocale())
					.modules(defaultDataRequest.getModules())
					.build();
			localizationUtil.createLocalizationData(defaultLocalizationDataRequest);
		}
	}
}
