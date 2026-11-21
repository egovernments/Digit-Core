package org.egov.enc.masterdata.provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.enc.config.AppProperties;
import org.egov.enc.masterdata.MasterDataProvider;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
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
    public ArrayList<String> getTenantIds() throws CustomException{
        LOGGER.info("Inside WebServiceMasterDataProvider");
        try{
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
        }catch(JSONException | RestClientException e){
            LOGGER.error("Unable to get data from WebService", ExceptionUtils.getStackTrace(e));
            throw new CustomException("Unable to fetch data from MDMS WebService", "WebService Exception");
        }

    }
}
