package org.egov.infra.mdms.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.config.ApplicationConfig;
import org.egov.infra.mdms.model.AuditLog;
import org.egov.infra.mdms.model.AuditLogResponse;
import org.egov.infra.mdms.repository.AuditLogRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.infra.mdms.errors.ErrorCodes.HISTORY_AUDIT_SERVICE_ERR;
import static org.egov.infra.mdms.errors.ErrorCodes.HISTORY_AUDIT_SERVICE_ERR_MSG;

@Repository
@Slf4j
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private RestTemplate restTemplate;

    private ApplicationConfig applicationConfig;

    @Autowired
    public AuditLogRepositoryImpl(RestTemplate restTemplate, ApplicationConfig applicationConfig) {
        this.restTemplate = restTemplate;
        this.applicationConfig = applicationConfig;
    }

    /**
     * This method fetches one page of audit logs of a master data record from audit-service.
     * audit-service binds the search criteria from query parameters and RequestInfo from the body.
     * @param requestInfo
     * @param tenantId
     * @param objectId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<AuditLog> search(RequestInfo requestInfo, String tenantId, String objectId, int offset, int limit) {
        URI uri = UriComponentsBuilder.fromUriString(applicationConfig.getAuditHost() + applicationConfig.getAuditSearchEndpoint())
                .queryParam("tenantId", tenantId)
                .queryParam("objectId", objectId)
                .queryParam("module", applicationConfig.getAuditMdmsModule())
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .encode()
                .build()
                .toUri();

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);

        try {
            AuditLogResponse response = restTemplate.postForObject(uri, body, AuditLogResponse.class);
            if (response == null || response.getAuditLogs() == null)
                return Collections.emptyList();
            return response.getAuditLogs();
        } catch (RestClientException e) {
            log.error("Audit log search failed for objectId {}", objectId, e);
            throw new CustomException(HISTORY_AUDIT_SERVICE_ERR, HISTORY_AUDIT_SERVICE_ERR_MSG);
        }
    }
}
