package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.egov.sunbirdrc.repository.DidSchemaIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CredentialService {


    @Autowired
    private DidSchemaIdRepository didSchemaIdRepository;

    @Autowired
    private FetchDidSchemaIdService fetchDidSchemaIdService;

    @Autowired
    private DidSchemaId didSchemaId;

    public ResponseEntity<String> getCredentialId(String uuid){
        Map<String, DidSchemaId> data= didSchemaIdRepository.getDataMap();
        System.out.println("data is "+ data);
        System.out.println("data is "+ data.get(uuid));

        return fetchDidSchemaIdService.getVcCredentialsId(data.get(uuid));
    }

}
