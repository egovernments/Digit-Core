package org.egov.auditservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ObjectMapper objectMapper;

    public List<AuditLog> process(AuditLogRequest request) {

        // Validate audit logs size
        validator.validateAuditRequestSize(request.getAuditLogs());

        // Validate operation types present in audit log request
        validator.validateOperationType(request.getAuditLogs());

        // Validate keyValuePair has db fields
        validator.validateKeyValueMap(request.getAuditLogs());

        // Enrich audit logs
        enrichmentService.enrichAuditLogs(request);

        // Sign incoming data before persisting
        List<AuditLog> signedAuditLogs = chooseSignerAndVerifier.selectImplementationAndSign(request);

        // Encrypting keyValueMap before persisting
        List<EncReqObject> encRequestList = new ArrayList<>();
        request.getAuditLogs().forEach(auditLog -> {
            EncReqObject encRequest = EncReqObject.builder().tenantId(auditLog.getTenantId()).type("Imp").value(auditLog.getKeyValueMap()).build();
            encRequestList.add(encRequest);
        });

        EncryptionRequest encRequests = EncryptionRequest.builder().encryptionRequests(encRequestList).build();
        String encUri = getEncUri();
        Object response = serviceRequestRepository.fetchResult(encUri, encRequests);
        EncryptionResponse encResponse = objectMapper.convertValue(response, EncryptionResponse.class);
        List<Map<String, Object>> encResponseList = encResponse.getEncResponseList();

//        EncryptionResponse encResponse = encryptionDecryptionUtil.encryptObject(encRequests,"EncryptionResponse", request.getAuditLogs().get(0).getTenantId(), EncryptionResponse.class );
//        List<Map<String, Object>> encResponseList = encResponse.getEncResponseList();


        // Iterate over both lists simultaneously
        for (int i = 0; i < encResponseList.size(); i++) {
            Map<String, Object> keyValueMap = encResponseList.get(i);
            request.getAuditLogs().get(i).setKeyValueMap(keyValueMap);
        }

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
        List<Map<String, Object>> decRequestList = new ArrayList<>();
        auditLogs.forEach(auditLog -> {
            decRequestList.add(auditLog.getKeyValueMap());
        });
        EncryptionResponse decRequest = EncryptionResponse.builder().encResponseList(decRequestList).build();

        String decUri = getDecUri();
        Object response = serviceRequestRepository.fetchResult(decUri, decRequest);
        EncryptionResponse decResponse = objectMapper.convertValue(response, EncryptionResponse.class);
        List<Map<String, Object>> decResponseList = decResponse.getEncResponseList();

        // Iterate over both lists simultaneously
        for (int i = 0; i < decResponseList.size(); i++) {
            Map<String, Object> keyValueMap = decResponseList.get(i);
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

    public Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        // Get all fields of the object using reflection
        Field[] fields = obj.getClass().getDeclaredFields();
        // Iterate over the fields
        for (Field field : fields) {
            try {
                // Make the field accessible (in case it's private)
                field.setAccessible(true);
                // Get the value of the field from the object
                Object value = field.get(obj);
                // Put the field name and value into the map
                map.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                // Handle IllegalAccessException
                e.printStackTrace();
            }
        }
        return map;
    }

}
