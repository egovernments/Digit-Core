package org.egov.infra.mdms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.mdms.model.ModuleDetail;
import org.egov.mdms.service.MdmsClient;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "egov.mdms.mode", havingValue = "local")
public class LocalMdmsClient implements MdmsClient {

    @Autowired
    private MDMSService mdmsService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    void logActivation() {
        log.info("LocalMdmsClient active (egov.mdms.mode=local). In-process MDMS calls; no HTTP.");
    }

    @Override
    public MdmsResponse getMaster(RequestInfo requestInfo, String tenantId,
                                  Map<String, List<MasterDetail>> masterDetails) {
        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setRequestInfo(requestInfo);
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        for (Map.Entry<String, List<MasterDetail>> entry : masterDetails.entrySet()) {
            ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(entry.getKey())
                    .masterDetails(entry.getValue()).build();
            moduleDetails.add(moduleDetail);
        }
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        return getMaster(mdmsCriteriaReq);
    }

    @Override
    public MdmsResponse getMaster(MdmsCriteriaReq mdmsCriteriaReq) {
        log.debug("LocalMdmsClient invoked for tenant={}, modules={}",
                mdmsCriteriaReq.getMdmsCriteria().getTenantId(),
                mdmsCriteriaReq.getMdmsCriteria().getModuleDetails().stream()
                        .map(ModuleDetail::getModuleName).toList());
        Map<String, Map<String, JSONArray>> result = mdmsService.searchMaster(mdmsCriteriaReq);
        MdmsResponse response = new MdmsResponse();
        response.setMdmsRes(result);
        return isolate(response);
    }

    /**
     * Jackson round-trip deep-copy at the in-process boundary.
     *
     * Under HTTP transport this isolation was accidental — Jackson serialized the
     * response into JSON on the wire and the client deserialized a fresh object
     * graph, so callers could freely mutate the result. Direct in-process calls
     * would instead alias MDMSService's static TenantMap cache — a caller-side
     * mutation on any returned JSONArray or Map would corrupt master data
     * process-wide. Doing the copy here (not inside MDMSService) confines the
     * cost to in-process callers only; HTTP callers keep paying for the wire.
     */
    private MdmsResponse isolate(MdmsResponse response) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsBytes(response), MdmsResponse.class);
        } catch (Exception e) {
            throw new CustomException("MDMS_LOCAL_ISOLATION_FAILED",
                    "Failed to isolate MDMS response from in-process cache: " + e.getMessage());
        }
    }
}
