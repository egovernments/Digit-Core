package org.egov.mdms.service;

import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;

public interface MdmsClient {

    MdmsResponse getMaster(RequestInfo requestInfo, String tenantId,
                           Map<String, List<MasterDetail>> masterDetails);

    MdmsResponse getMaster(MdmsCriteriaReq mdmsCriteriaReq);
}
