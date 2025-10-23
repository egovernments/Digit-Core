package org.egov.encryption.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.encryption.models.*;
import org.egov.encryption.util.MdmsFetcher;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DecryptionPolicyConfiguration {

    @Autowired
    private EncProperties encProperties;

    @Autowired
    private MdmsFetcher mdmsFetcher;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, List<Attribute>> modelAttributeAccessMap;

    private Map<String, Map<String, List<AttributeAccess>>> modelRoleBasedDecryptionPolicyMap;

    private Map<String, UniqueIdentifier> uniqueIdentifierMap;



    void initializeModelAttributeAccessMap(String tenantId, List<SecurityPolicy> modelRoleAttributeAccessList) {
        modelAttributeAccessMap.putAll(modelRoleAttributeAccessList.stream()
                .collect(Collectors.toMap(securityPolicy -> tenantModelKey(tenantId, securityPolicy.getModel()),
                        SecurityPolicy::getAttributes)));
    }

    void initializeRoleBasedDecryptionPolicyMap(String tenantId, List<SecurityPolicy> modelRoleAttributeAccessList) {
        for (SecurityPolicy securityPolicy : modelRoleAttributeAccessList) {
            modelRoleBasedDecryptionPolicyMap.put(tenantModelKey(tenantId, securityPolicy.getModel()),
                    makeRoleAttributeAccessMapping(securityPolicy.getRoleBasedDecryptionPolicy()));
        }
    }

    void initializeUniqueIdentifierMap(String tenantId, List<SecurityPolicy> modelRoleAttributeAccessList) {
        uniqueIdentifierMap.putAll(modelRoleAttributeAccessList.stream()
                .collect(Collectors.toMap(securityPolicy -> tenantModelKey(tenantId, securityPolicy.getModel()),
                        SecurityPolicy::getUniqueIdentifier)));
    }

    @PostConstruct
    void initializeModelAttributeAccessMapFromMdms() {
        modelAttributeAccessMap = new HashMap<>();
        modelRoleBasedDecryptionPolicyMap = new HashMap<>();
        uniqueIdentifierMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(encProperties.getStateLevelTenantIds())) {
            for(String tenantId : encProperties.getStateLevelTenantIds()) {
                List<SecurityPolicy> securityPolicyList = null;
                try {
                    JSONArray securityPolicyJson = mdmsFetcher.getSecurityMdmsForFilter(tenantId, null);
                    ObjectReader reader = objectMapper.readerFor(objectMapper.getTypeFactory().constructCollectionType(List.class,
                            SecurityPolicy.class));
                    securityPolicyList = reader.readValue(securityPolicyJson.toString());
                } catch (IOException e) {
                    log.error(ErrorConstants.SECURITY_POLICY_READING_ERROR_MESSAGE, e);
                    throw new CustomException(ErrorConstants.SECURITY_POLICY_READING_ERROR, ErrorConstants.SECURITY_POLICY_READING_ERROR_MESSAGE);
                }

                initializeModelAttributeAccessMap(tenantId, securityPolicyList);
                initializeRoleBasedDecryptionPolicyMap(tenantId, securityPolicyList);
                initializeUniqueIdentifierMap(tenantId, securityPolicyList);
            }
        }
    }

    public UniqueIdentifier getUniqueIdentifierForModel(String tenantId, String model) {
        return uniqueIdentifierMap.get(tenantModelKey(tenantId, model));
    }

    public Map<Attribute, Visibility> getRoleAttributeAccessListForModel(RequestInfo requestInfo, String tenantId, String model, List<String> roles) {
        Map<Attribute, Visibility> mapping = new HashMap<>();
        String modelKey = tenantModelKey(tenantId, model);
        try {
            List<Attribute> attributesList = modelAttributeAccessMap.get(modelKey);
            Map<String, List<AttributeAccess>> roleAttributeAccessMap =
                    modelRoleBasedDecryptionPolicyMap.get(modelKey);

            boolean isAttributeListEmpty = CollectionUtils.isEmpty(attributesList);
            boolean isRoleAttributeAccessMapEmpty = CollectionUtils.isEmpty(roleAttributeAccessMap);

            if (isAttributeListEmpty) {
                throw new CustomException("DECRYPTION_NULL_ERROR", "Attribute list is empty");
            }


            if (!isAttributeListEmpty && !isRoleAttributeAccessMapEmpty) {
                Map<String, Attribute> attributesMap = makeAttributeMap(attributesList);

                List<String> secondLevelVisibility = new ArrayList<>();

                for (String role : roles) {
                    if (!roleAttributeAccessMap.containsKey(role))
                        continue;

                    List<AttributeAccess> attributeList = roleAttributeAccessMap.get(role);

                    for (AttributeAccess attributeAccess : attributeList) {
                        String attributeName = attributeAccess.getAttribute();
                        Attribute attribute = attributesMap.get(attributeName);
                        if (requestInfo.getPlainAccessRequest() != null && !CollectionUtils.isEmpty(requestInfo.getPlainAccessRequest().getPlainRequestFields())
                                && requestInfo.getPlainAccessRequest().getPlainRequestFields().contains(attributeName)
                                && attributeAccess.getSecondLevelVisibility() != null && !secondLevelVisibility.contains(attributeName)) {
                            secondLevelVisibility.add(attributeName);
                        }
                        String firstLevelVisibility = attributeAccess.getFirstLevelVisibility() != null ?
                                String.valueOf(attributeAccess.getFirstLevelVisibility()) : String.valueOf(attribute.getDefaultVisibility());
                        Visibility visibility = Visibility.valueOf(firstLevelVisibility);
                        if (mapping.containsKey(attribute)) {
                            if (mapping.get(attribute).ordinal() > visibility.ordinal()) {
                                mapping.remove(attribute);
                                mapping.put(attribute, visibility);
                            }
                        } else {
                            mapping.put(attribute, visibility);
                        }
                    }
                }

                if (requestInfo.getPlainAccessRequest() != null)
                    requestInfo.getPlainAccessRequest().setPlainRequestFields(secondLevelVisibility);
            }

            List<Attribute> mappingAttributesList = new ArrayList<>(mapping.keySet());
            List<String> attributesToAvoidlist = new ArrayList<>();
            for (Attribute attribute : mappingAttributesList)
                attributesToAvoidlist.add(attribute.getName());


            if (!isAttributeListEmpty)
                getDefaultVisibilityMapping(attributesList, mapping, attributesToAvoidlist);


            return mapping;
        } catch (Exception e) {
            throw new CustomException("DECRYPTION_NULL_ERROR", "Error in decryption process");
        }
    }

    private Map<String, List<AttributeAccess>> makeRoleAttributeAccessMapping(List<RoleBasedDecryptionPolicy> roleBasedDecryptionPolicyList) {
        Map<String, List<AttributeAccess>> roleAttributeAccessMap = new HashMap<>();
        for (RoleBasedDecryptionPolicy roleBasedDecryptionPolicy : roleBasedDecryptionPolicyList) {
            List<String> roles = roleBasedDecryptionPolicy.getRoles();
            List<AttributeAccess> attributeAccessList = roleBasedDecryptionPolicy.getAttributeAccessList();
            for (String role : roles) {
                roleAttributeAccessMap.put(role, attributeAccessList);
            }
        }
        return roleAttributeAccessMap;
    }

    private Map<String, Attribute> makeAttributeMap(List<Attribute> attributesList) {
        Map<String, Attribute> atrributesMap = new HashMap<>();

        for (Attribute attribute : attributesList) {
            String filedName = attribute.getName();
            atrributesMap.put(filedName, attribute);
        }
        return atrributesMap;
    }

    public UniqueIdentifier getSecurityPolicyUniqueIdentifier(String tenantId, String model) {
        return uniqueIdentifierMap.get(tenantModelKey(tenantId, model));
    }

    private void getDefaultVisibilityMapping(List<Attribute> attributesList, Map<Attribute, Visibility> mapping, List<String> attributesToAvoidlist) {

        for (Attribute attribute : attributesList) {
            String defaultVisibility = String.valueOf(attribute.getDefaultVisibility());
            Visibility visibility = Visibility.valueOf(defaultVisibility);
            if (!attributesToAvoidlist.contains(attribute.getName())) {
                if (mapping.containsKey(attribute)) {
                    if (mapping.get(attribute).ordinal() > visibility.ordinal()) {
                        mapping.remove(attribute);
                        mapping.put(attribute, visibility);
                    }
                } else {
                    mapping.put(attribute, visibility);
                }
            }
        }
    }

    private String tenantModelKey(String tenantId, String model) {
        return tenantId + "_" + model;
    }

}
