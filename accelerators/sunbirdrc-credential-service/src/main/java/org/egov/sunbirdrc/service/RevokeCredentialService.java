package org.egov.sunbirdrc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RevokeCredentialService {

    @Value("${sunbird.credential.host}")
    private String revokeCredentialHost;

    @Value("${sunbird.revoke.path}")
    private String revokeCredentialPath;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private RestTemplate restTemplate;
    public String revokeCredential(String credentialId){
        StringBuilder requestUrl= new StringBuilder();
        requestUrl.append(revokeCredentialHost).append("/credentials/");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String revokeRequestUrl= requestUrl.toString()+credentialId;
        ResponseEntity<String> responseEntity=restTemplate.exchange(
                revokeRequestUrl,
                HttpMethod.DELETE,
                null,
                String.class);
        return responseEntity.getBody();
    }
    }


