package org.egov.sunbirdrc.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RevokeCredentialService {
    private final String url="http://localhost:3000/credentials/";
    public String revokeCredential(String credentialId){
        RestTemplate restTemplate= new RestTemplate();
        String requestUrl=url+credentialId;
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


