package org.egov.handler.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.util.MdmsV2Util;
import org.egov.handler.web.models.DefaultMasterDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataHandlerService {

	private final MdmsV2Util mdmsV2Util;

	private final ServiceConfiguration serviceConfig;

	@Autowired
	public DataHandlerService(MdmsV2Util mdmsV2Util, ServiceConfiguration serviceConfig) {
		this.mdmsV2Util = mdmsV2Util;
		this.serviceConfig = serviceConfig;
	}

	public void createDefaultData(RequestInfo requestInfo, String code) {
		DefaultMasterDataRequest defaultMasterDataRequest = DefaultMasterDataRequest.builder()
				.requestInfo(requestInfo)
				.targetTenantId(code)
				.schemaCodes(serviceConfig.getDefaultMdmsSchemaList())
				.build();
		mdmsV2Util.createMdmsData(defaultMasterDataRequest);
	}
}
