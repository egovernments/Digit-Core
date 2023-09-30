package org.egov.enc.services;

import lombok.extern.slf4j.Slf4j;
import org.egov.enc.config.AppProperties;
import org.egov.enc.keymanagement.KeyGenerator;
import org.egov.enc.keymanagement.KeyIdGenerator;
import org.egov.enc.keymanagement.KeyStore;
import org.egov.enc.masterdata.MasterDataProvider;
import org.egov.enc.models.AsymmetricKey;
import org.egov.enc.models.SymmetricKey;
import org.egov.enc.repository.KeyRepository;
import org.egov.enc.web.models.RotateKeyRequest;
import org.egov.enc.web.models.RotateKeyResponse;
import org.egov.tracer.model.CustomException;
import org.json.JSONException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.egov.enc.utils.Constants.TENANTID_MDC_STRING;

@Slf4j
@Service
public class KeyManagementService implements ApplicationRunner {

    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private KeyGenerator keyGenerator;
    @Autowired
    private KeyStore keyStore;
    @Autowired
    private KeyIdGenerator keyIdGenerator;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private MasterDataProvider masterDataProvider;


    //Initialize active tenant id list and Check for any new tenants
    private void init() throws Exception {
        // Adding in MDC so that tracer can add it in header
        MDC.put(TENANTID_MDC_STRING, appProperties.getStateLevelTenantId());
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
        return masterDataProvider.getTenantIds();
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        init();
    }
}
