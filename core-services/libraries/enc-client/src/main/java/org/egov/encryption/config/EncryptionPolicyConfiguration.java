package org.egov.encryption.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.encryption.models.Attribute;
import org.egov.encryption.models.SecurityPolicy;
import org.egov.encryption.util.MdmsFetcher;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EncryptionPolicyConfiguration {

    @Autowired
    private MdmsFetcher mdmsFetcher;
    @Autowired
    private ObjectMapper objectMapper;

    // Per state-tenant cache of model -> attributes, loaded lazily on first request for that tenant.
    private final Map<String, Map<String, List<Attribute>>> tenantEncryptionPolicyAttributesMap = new ConcurrentHashMap<>();

    private Map<String, List<Attribute>> loadEncryptionPolicyAttributesMap(String tenantId) {
        try {
            JSONArray attributesDetailsJSON = mdmsFetcher.getSecurityMdmsForFilter(null, tenantId);
            ObjectReader reader = objectMapper.readerFor(objectMapper.getTypeFactory().constructCollectionType(List.class,
                    SecurityPolicy.class));
            List<SecurityPolicy> securityPolicies = reader.readValue(attributesDetailsJSON.toString());
            return securityPolicies.stream()
                    .collect(Collectors.toMap(SecurityPolicy::getModel, SecurityPolicy::getAttributes));
        } catch (IOException e) {
            log.error(ErrorConstants.SECURITY_POLICY_READING_ERROR_MESSAGE, e);
            throw new CustomException(ErrorConstants.SECURITY_POLICY_READING_ERROR, ErrorConstants.SECURITY_POLICY_READING_ERROR_MESSAGE);
        }
    }

    public List<Attribute> getAttributeDetailsForModel(String modelName) {
        return getAttributeDetailsForModel(modelName, null);
    }

    public List<Attribute> getAttributeDetailsForModel(String modelName, String tenantId) {
        try {
            String cacheKey = tenantId == null ? "" : tenantId;
            Map<String, List<Attribute>> attributesMap =
                    tenantEncryptionPolicyAttributesMap.computeIfAbsent(cacheKey, k -> loadEncryptionPolicyAttributesMap(tenantId));
            return attributesMap.get(modelName);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("DECRYPTION_ERROR", "Error in retrieving MDMS data");
        }
    }

}