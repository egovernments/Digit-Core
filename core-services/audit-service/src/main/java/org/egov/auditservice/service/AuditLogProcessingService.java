package org.egov.auditservice.service;

import org.egov.auditservice.producer.Producer;
import org.egov.auditservice.repository.AuditServiceRepository;
import org.egov.auditservice.repository.ServiceRequestRepository;
import org.egov.auditservice.validator.AuditServiceValidator;
import org.egov.auditservice.web.models.AuditLog;
import org.egov.auditservice.web.models.AuditLogRequest;
import org.egov.auditservice.web.models.AuditLogSearchCriteria;
import org.egov.auditservice.web.models.ObjectIdWrapper;
import org.egov.auditservice.web.models.encryptionclient.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class AuditLogProcessingService {

    @Value("${persister.audit.kafka.topic}")
    private String auditTopic;

    @Value("${egov.enc.host}")
    private String encHost;

    @Value("${egov.enc.encrypt.endpoint}")
    private String encEncryptEndpoint;

    @Value("${egov.enc.decrypt.endpoint}")
    private String encDecryptEndpoint;


    @Autowired
    private Producer producer;

    @Autowired
    private ChooseSignerAndVerifier chooseSignerAndVerifier;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private AuditServiceRepository repository;

    @Autowired
    private AuditServiceValidator validator;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;



    public List<AuditLog> process(AuditLogRequest request) {

        // Validate audit logs size
        validator.validateAuditRequestSize(request.getAuditLogs());

        // Validate operation types present in audit log request
        validator.validateOperationType(request.getAuditLogs());

        // Validate keyValuePair has db fields
        validator.validateKeyValueMap(request.getAuditLogs());

        // Enrich audit logs
        enrichmentService.enrichAuditLogs(request);

        // Encrypting keyValueMap before persisting
        List<EncReqObject> encRequestList = new ArrayList<>();
        request.getAuditLogs().forEach(auditLog -> {
            EncReqObject encReqObject = EncReqObject.builder().tenantId(auditLog.getTenantId()).type("Imp").value(auditLog.getKeyValueMap()).build();
            encRequestList.add(encReqObject);
        });

        EncryptionRequest encRequests = EncryptionRequest.builder().encryptionRequests(encRequestList).build();
        String encUri = getEncUri();
        LinkedList<Object> response = serviceRequestRepository.fetchEncResult(encUri, encRequests);

        // Iterate over both lists simultaneously
        for (int i = 0; i < response.size(); i++) {
            Map<String, Object> keyValueMap = (Map) response.get(i);
            request.getAuditLogs().get(i).setKeyValueMap(keyValueMap);
        }

        // Sign incoming data before persisting
        List<AuditLog> signedAuditLogs = chooseSignerAndVerifier.selectImplementationAndSign(request);


        // Persister will handle persisting audit records
        producer.push(auditTopic, request);

        return signedAuditLogs;
    }


    public List<AuditLog> getAuditLogs(RequestInfo requestInfo, AuditLogSearchCriteria criteria) {

        validator.validateAuditLogSearch(criteria);

        List<AuditLog> auditLogs = repository.getAuditLogsFromDb(criteria);

        if(CollectionUtils.isEmpty(auditLogs))
            return new ArrayList<>();

        //Decrypting keyValueMap
        List<Object> decRequestList = new ArrayList<>();
        auditLogs.forEach(auditLog -> {
            decRequestList.add(auditLog.getKeyValueMap());
        });


        String decUri = getDecUri();
        LinkedList<Object> response = serviceRequestRepository.fetchEncResult(decUri, decRequestList);

        // Iterate over both lists simultaneously
        for (int i = 0; i < response.size(); i++) {
            Map<String, Object> keyValueMap = (Map) response.get(i);
            auditLogs.get(i).setKeyValueMap(keyValueMap);
        }

        return auditLogs;
    }

    public void verifyDbEntity(String objectId, Map<String, Object> keyValuePairs){
        chooseSignerAndVerifier.selectImplementationAndVerify(ObjectIdWrapper.builder().objectId(objectId).keyValuePairs(keyValuePairs).build());
    }

    private String getEncUri() {
        StringBuilder signUri = new StringBuilder(encHost);
        signUri.append(encEncryptEndpoint);
        return signUri.toString();
    }

    private String getDecUri() {
        StringBuilder signUri = new StringBuilder(encHost);
        signUri.append(encDecryptEndpoint);
        return signUri.toString();
    }

}
