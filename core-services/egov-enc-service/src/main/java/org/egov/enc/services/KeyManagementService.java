package org.egov.enc.services;

import lombok.extern.slf4j.Slf4j;
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

import static org.egov.enc.utils.Constants.TENANTID_MDC_STRING;

@Slf4j
@Service
public class KeyManagementService implements ApplicationRunner {

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;

    @Value(("${egov.state.level.tenant.id}"))
    private String stateLevelTenantId;

    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private KeyGenerator keyGenerator;
    @Autowired
    private KeyStore keyStore;
    @Autowired
    private KeyIdGenerator keyIdGenerator;


    //Initialize active tenant id list and Check for any new tenants
    private void init() throws Exception {
        // Adding in MDC so that tracer can add it in header
        MDC.put(TENANTID_MDC_STRING, stateLevelTenantId);
        generateKeyForNewTenants();
    }

    //Check if a given tenantId exists
    public boolean checkIfTenantExists(String tenant) throws Exception {
        if(keyStore.getTenantIds().contains(tenant)) {
            return true;
        }
        generateKeyForNewTenants();
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
    private int generateKeyForNewTenants() throws Exception {
        keyStore.refreshKeys();
        keyIdGenerator.refreshKeyIds();

        Collection<String> tenantIdsFromMdms = makeComprehensiveListOfTenantIds();
        tenantIdsFromMdms.removeAll(keyStore.getTenantIds());

        if(tenantIdsFromMdms.size() != 0) {
            ArrayList<String> tenantIdList = new ArrayList<>(tenantIdsFromMdms);
            generateKeys(tenantIdList);

            keyStore.refreshKeys();
            keyIdGenerator.refreshKeyIds();
        }
        return tenantIdsFromMdms.size();
    }

    private Set<String> makeComprehensiveListOfTenantIds() {
        ArrayList<String> tenantIds = getTenantIds();
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
    public RotateKeyResponse rotateAllKeys() throws Exception {
        deactivateOldKeys();
        generateKeyForNewTenants();
        return new RotateKeyResponse(true);
    }

    /**
     * Idempotently provision a symmetric + asymmetric key for a single tenantId.
     *
     * The default key-generation path (init() + checkIfTenantExists) only fires
     * for tenants reachable via MDMS search under STATE_LEVEL_TENANT_ID — brand
     * new state roots (which are not yet under any existing root's
     * tenant.tenants list) get a "Tenant Id not found" 500 on first encrypt
     * because no key exists for them.
     *
     * Callers that provision new tenants (e.g. MCP tenant_bootstrap) hit this
     * BEFORE the first encrypt request for the new tenant. Re-issuing for an
     * existing tenant is a no-op — the existing keyId is returned, no rotation.
     *
     * Synchronized to prevent two concurrent generates for the same fresh
     * tenant from both inserting (the underlying generateKeys does not have
     * an INSERT ... ON CONFLICT — duplicate rows would violate the keyId PK).
     */
    public synchronized org.egov.enc.web.models.GenerateKeyResponse generateKeyForTenant(String tenantId)
            throws Exception {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new CustomException("INVALID_TENANT_ID", "tenantId must be non-empty");
        }
        final String normalized = tenantId.trim();

        // Idempotency: if the tenant already has an active key in the store,
        // return its keyId — do NOT generate a duplicate. This is the no-op
        // path that lets callers issue this freely without worrying about state.
        keyStore.refreshKeys();
        if (keyStore.getTenantIds().contains(normalized)) {
            org.egov.enc.models.SymmetricKey existing = keyStore.getSymmetricKey(normalized);
            return org.egov.enc.web.models.GenerateKeyResponse.builder()
                    .tenantId(normalized)
                    .created(false)
                    .keyId(existing != null ? existing.getId() : null)
                    .build();
        }

        // Generate the key pair and persist. Reuses the same private path
        // that init() and rotateAll() use — symmetric + asymmetric inserts
        // in one shot; failure halfway throws and the caller can retry.
        ArrayList<String> tenants = new ArrayList<>();
        tenants.add(normalized);
        generateKeys(tenants);

        // Refresh in-memory caches so the next encrypt for this tenant
        // resolves directly without going through the MDMS-discovery fallback.
        keyStore.refreshKeys();
        keyIdGenerator.refreshKeyIds();

        org.egov.enc.models.SymmetricKey created = keyStore.getSymmetricKey(normalized);
        log.info("Generated keys for tenantId={} (keyId={})", normalized,
                created != null ? created.getId() : "?");
        return org.egov.enc.web.models.GenerateKeyResponse.builder()
                .tenantId(normalized)
                .created(true)
                .keyId(created != null ? created.getId() : null)
                .build();
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

        generateKeyForNewTenants();

        return new RotateKeyResponse(true);
    }



    private ArrayList<String> getTenantIds() throws JSONException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TENANTID_MDC_STRING,stateLevelTenantId);

        String requestJson = "{\"RequestInfo\":{},\"MdmsCriteria\":{\"tenantId\":\"" + stateLevelTenantId + "\"," +
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

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        init();
    }
}
