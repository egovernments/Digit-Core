package org.egov.enc.masterdata.provider;

import org.egov.enc.config.AppProperties;
import org.egov.enc.masterdata.MasterDataProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.egov.enc.utils.Constants.TENANTID_MDC_STRING;

public class WebServiceMasterDataProvider implements MasterDataProvider {

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;

    @Autowired
    private AppProperties appProperties;

    public static final Logger LOGGER = LoggerFactory.getLogger(WebServiceMasterDataProvider.class);

    @Override
    public ArrayList<String> getTenantIds() {
        LOGGER.info("Inside WebServiceMasterDataProvider");
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TENANTID_MDC_STRING,appProperties.getStateLevelTenantId());

        String requestJson = "{\"RequestInfo\":{},\"MdmsCriteria\":{\"tenantId\":\"" + appProperties.getStateLevelTenantId() + "\"," +
                "\"moduleDetails\":[{\"moduleName\":\"tenant\",\"masterDetails\":[{\"name\":\"tenants\"," +
                "\"filter\":\"$.*.code\"}]}]}}";

        String url = mdmsHost + mdmsEndpoint;

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray jsonArray = jsonObject.getJSONObject("MdmsRes").getJSONObject("tenant").getJSONArray("tenants");

        ArrayList<String> tenantIds = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            tenantIds.add(jsonArray.getString(i));
        }

        return tenantIds;
    }
}
