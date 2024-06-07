package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static digit.config.ServiceConstants.*;

@Slf4j
@Component
public class MdmsUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration configs;




    public Map<String, Map<String, JSONArray>> fetchMdmsData(RequestInfo requestInfo, String tenantId, String moduleName,
                                                                                List<String> masterNameList) {
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequest(requestInfo, tenantId, moduleName, masterNameList);
        Object response = new HashMap<>();
        Integer rate = 0;
        MdmsResponse mdmsResponse = new MdmsResponse();
        try {
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            mdmsResponse = mapper.convertValue(response, MdmsResponse.class);
        }catch(Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_MDMS,e);
        }

        return mdmsResponse.getMdmsRes();
        //log.info(ulbToCategoryListMap.toString());
    }

    private MdmsCriteriaReq getMdmsRequest(RequestInfo requestInfo, String tenantId,
                                           String moduleName, List<String> masterNameList) {
        List<MasterDetail> masterDetailList = new ArrayList<>();
        for(String masterName: masterNameList) {
            MasterDetail masterDetail = new MasterDetail();
            masterDetail.setName(masterName);
            masterDetailList.add(masterDetail);
        }

        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(masterDetailList);
        moduleDetail.setModuleName(moduleName);
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(tenantId.split("\\.")[0]);
        mdmsCriteria.setModuleDetails(moduleDetailList);

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(requestInfo);

        return mdmsCriteriaReq;
    }
}