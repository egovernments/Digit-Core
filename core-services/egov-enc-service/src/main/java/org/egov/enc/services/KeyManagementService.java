package org.egov.enc.services;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.enc.keymanagement.KeyGenerator;
import org.egov.enc.keymanagement.KeyIdGenerator;
import org.egov.enc.keymanagement.KeyStore;
import org.egov.enc.models.AsymmetricKey;
import org.egov.enc.models.SymmetricKey;
import org.egov.enc.repository.KeyRepository;
import org.egov.enc.web.models.RotateKeyRequest;
import org.egov.enc.web.models.RotateKeyResponse;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.egov.enc.utils.Constants;

import static org.egov.enc.utils.Constants.TENANTID_MDC_STRING;

@Slf4j
@Service
public class KeyManagementService implements ApplicationRunner {

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;

    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private KeyGenerator keyGenerator;
    @Autowired
    private KeyStore keyStore;
    @Autowired
    private KeyIdGenerator keyIdGenerator;
    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;


    //Load existing keys into memory at startup. Keys are provisioned lazily per request tenant
    //(the state anchor is always derived from the request tenantId), so no MDMS crawl is done here.
    private void init() throws Exception {
        keyStore.refreshKeys();
        keyIdGenerator.refreshKeyIds();
    }

    //Check if a given tenantId exists; provisions its key on demand using the state anchor
    //derived from the request tenantId
    public boolean checkIfTenantExists(String tenant) throws Exception {
        if(keyStore.getTenantIds().contains(tenant)) {
            return true;
        }
        String stateAnchor = multiStateInstanceUtil.getStateLevelTenant(tenant);
        generateKeyForNewTenants(stateAnchor);
        return keyStore.getTenantIds().contains(tenant);
    }

    //Generate Symmetric and Asymmetric Keys for each of the TenantId in the given input list
    private void generateKeys(ArrayList<String> tenantIds) throws Exception {

        int status;
        ArrayList<SymmetricKey> symmetricKeys = keyGenerator.generateSymmetricKeys(tenantIds);
        for(SymmetricKey symmetricKey : symmetricKeys) {
            status = keyRepository.insertSymmetricKey(symmetricKey);
            if(status != 1) {
                throw new CustomException("DB Insert Exception", "DB Insert Exception");
            }
        }

        ArrayList<AsymmetricKey> asymmetricKeys = keyGenerator.generateAsymmetricKeys(tenantIds);
        for(AsymmetricKey asymmetricKey : asymmetricKeys) {
            status = keyRepository.insertAsymmetricKey(asymmetricKey);
            if(status != 1) {
                throw new CustomException("DB Insert Exception", "DB Insert Exception");
            }
        }
    }

    //Generate keys if there are any new tenants
    //Returns the number of tenants for which the keys have been generated
    private int generateKeyForNewTenants(String stateAnchor) throws Exception {
        keyStore.refreshKeys();
        keyIdGenerator.refreshKeyIds();

        Collection<String> tenantIdsFromMdms = makeComprehensiveListOfTenantIds(stateAnchor);
        tenantIdsFromMdms.removeAll(keyStore.getTenantIds());

        if(tenantIdsFromMdms.size() != 0) {
            ArrayList<String> tenantIdList = new ArrayList<>(tenantIdsFromMdms);
            generateKeys(tenantIdList);

            keyStore.refreshKeys();
            keyIdGenerator.refreshKeyIds();
        }
        return tenantIdsFromMdms.size();
    }

    private Set<String> makeComprehensiveListOfTenantIds(String stateAnchor) {
        ArrayList<String> tenantIds = getTenantIds(stateAnchor);
        Set<String> comprehensiveTenantIdsSet = new HashSet<>(tenantIds);

        for (String tenantId: tenantIds) {
            int index = tenantId.indexOf(".");
            while(index > 0) {
                comprehensiveTenantIdsSet.add(tenantId.substring(0, index));
                index = tenantId.indexOf(".", index + 1);
            }
        }

        return comprehensiveTenantIdsSet;
    }

    //Used to deactivate old keys at the time of key rotation
    private void deactivateOldKeys() {
        keyRepository.deactivateSymmetricKeys();
        keyRepository.deactivateAsymmetricKeys();
    }

    //Deactivate old keys and generate new keys for every tenantId
    public RotateKeyResponse rotateAllKeys(RotateKeyRequest rotateKeyRequest) throws Exception {
        deactivateOldKeys();
        String stateAnchor = multiStateInstanceUtil.getStateLevelTenant(rotateKeyRequest.getTenantId());
        generateKeyForNewTenants(stateAnchor);
        return new RotateKeyResponse(true);
    }

    public RotateKeyResponse rotateKey(RotateKeyRequest rotateKeyRequest) throws Exception {
        int status;
        status = keyRepository.deactivateSymmetricKeyForGivenTenant(rotateKeyRequest.getTenantId());
        log.info("Key Rotate SYM Return Status: " + status);
        if(status != 1) {
            throw new CustomException("DB Exception", "DB Exception");
        }
        status = keyRepository.deactivateAsymmetricKeyForGivenTenant(rotateKeyRequest.getTenantId());
        log.info("Key Rotate ASY Return Status: " + status);
        if(status != 1) {
            throw new CustomException("DB Exception", "DB Exception");
        }

        String stateAnchor = multiStateInstanceUtil.getStateLevelTenant(rotateKeyRequest.getTenantId());
        generateKeyForNewTenants(stateAnchor);

        return new RotateKeyResponse(true);
    }



    private ArrayList<String> getTenantIds(String stateAnchor) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TENANTID_MDC_STRING, stateAnchor);

        String requestJson = "{\"RequestInfo\":{},\"MdmsCriteria\":{\"tenantId\":\"" + stateAnchor + "\"," +
                "\"moduleDetails\":[{\"moduleName\":\"tenant\",\"masterDetails\":[{\"name\":\"tenants\"," +
                "\"filter\":\"$.*.code\"}]}]}}";

        String url = mdmsHost + mdmsEndpoint;

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Null-safe parsing: if the tenant/tenants master is not present in MDMS for this
        // tenant, surface a clear, tenant-specific error instead of a raw NPE/JSONException.
        JSONObject mdmsRes = response.getBody() != null
                ? new JSONObject(response.getBody()).optJSONObject("MdmsRes") : null;
        JSONObject tenantModule = mdmsRes != null ? mdmsRes.optJSONObject("tenant") : null;
        JSONArray jsonArray = tenantModule != null ? tenantModule.optJSONArray("tenants") : null;

        if (jsonArray == null || jsonArray.length() == 0) {
            String message = String.format(Constants.MDMS_TENANTS_NOT_FOUND_MESSAGE, stateAnchor);
            log.error(message);
            throw new CustomException(Constants.MDMS_DATA_NOT_FOUND_CODE, message);
        }

        ArrayList<String> tenantIds = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            tenantIds.add(jsonArray.getString(i));
        }

        return tenantIds;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        init();
    }
}
