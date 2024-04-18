package org.egov.sunbirdrc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RevokeCredentialService {

    @Value("${sunbird.revoke.url}")
    private String revokeCredentialUrl;

    @Autowired
    private RestTemplate restTemplate;
    public String revokeCredential(String credentialId){
        String requestUrl=revokeCredentialUrl+credentialId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity=restTemplate.exchange(
                requestUrl,
                HttpMethod.DELETE,
                null,
                String.class);
        return responseEntity.getBody();
    }
    }


