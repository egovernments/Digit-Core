package org.egov.handler.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class HrmsUtil {

    private final RestTemplate restTemplate;

    private final ServiceConfiguration serviceConfig;

    @Autowired
    public HrmsUtil(RestTemplate restTemplate, ServiceConfiguration serviceConfig) {
        this.restTemplate = restTemplate;
        this.serviceConfig = serviceConfig;
    }

    public void createHrmsEmployee(String HrmsRequest, String tenantId) {

        StringBuilder uri = new StringBuilder();
        uri.append(serviceConfig.getHrmsHost()).append(serviceConfig.getHrmsCreatePath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(HrmsRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri.toString(), request, String.class);
            System.out.println(response);
//			restTemplate.postForObject(uri.toString(), defaultLocalizationDataRequest, ResponseInfo.class);
        } catch (Exception e) {
            log.error("Error creating default HRMS employee data for {} : {}", tenantId, e.getMessage());
            throw new CustomException("LOCALIZATION_DEFAULT_DATA_CREATE_FAILED", "Failed to create localization data for " + tenantId + " : " + e.getMessage());
        }
    }

    public void upsertLocalization(TenantRequest tenantRequest) {

        List<Message> messageList = generateDynamicMessage(tenantRequest);
        tenantRequest.getRequestInfo().getUserInfo().setId(128L);
        CreateMessagesRequest createMessagesRequest = CreateMessagesRequest.builder()
                .requestInfo(tenantRequest.getRequestInfo())
                .tenantId(tenantRequest.getTenant().getCode())
                .messages(messageList)
                .build();

        StringBuilder uri = new StringBuilder();
        uri.append(serviceConfig.getUpsertLocalizationURI());
        try {
            restTemplate.postForObject(uri.toString(), createMessagesRequest, ResponseInfo.class);
        } catch (Exception e) {
            log.error("Error creating Tenant localization data for {} : {}", tenantRequest.getTenant().getCode(), e.getMessage());
            throw new CustomException("TENANT", "Failed to create localization data for " + tenantRequest.getTenant().getCode() + " : " + e.getMessage());
        }
    }

    public List<Message> generateDynamicMessage(TenantRequest tenantRequest) {

        Tenant tenant = tenantRequest.getTenant();

        String tenantCode = tenant != null && tenant.getCode() != null
                ? tenant.getCode().toUpperCase().replace(".", "_")
                : null;

        String ulbKey = tenantCode != null
                ? "TENANT_TENANTS_" + tenantCode
                : null;

        Message message = Message.builder()
                .code(ulbKey)
                .module(serviceConfig.getTenantLocalizationModule())
                .message(tenant.getName())
                .locale("en_IN")
                .build();
        return Collections.singletonList(message);
    }
}
