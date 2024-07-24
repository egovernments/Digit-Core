package org.egov.sunbirdrc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.CredentialIdResponse;
import org.egov.sunbirdrc.models.QrCodeRequest;
import org.egov.sunbirdrc.models.QrTemplateClient;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class QrImageService {

    @Value("${sunbird.credential.schema.host}")
    private String qrCodeHost;

    @Value("${sunbird.credential.host}")
    private String credentialHost;

    @Value("${sunbird.credential.path}")
    private String credentialPath;

    @Value("${sunbird.template.path}")
    private String qrTemplatePath;

    @Value("${sunbird.qr.path}")
    private String qrImagePath;


    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String qrCodeHtmlTemplate= "<html lang='en'><head><meta charset='UTF-8' /><meta http-equiv='X-UA-Compatible' content='IE=edge' /><meta name='viewport' content='width=device-width, initial-scale=1.0' /><script src='https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js'></script><link rel='stylesheet' href='style.css' /><img src='{{qr}}' alt='Base64 Imag'></body></html>";
    private final String qrCodeClientObjectType="handlebar";
    @Autowired
    private CredentialService credentialService;
    public String getQrImage(QrCodeRequest qrCodeRequest) throws JsonProcessingException {
        CredentialIdResponse credentialIdObject=credentialService.getCredential(qrCodeRequest);
        QrTemplateClient qrTemplateClient= QrTemplateClient.builder()
                .schemaId(credentialIdObject.getSchemaId())
                .schemaVersion("1.0.0")
                .template(qrCodeHtmlTemplate)
                .type(qrCodeClientObjectType)
                .build();

        StringBuilder uri = new StringBuilder();
        uri.append(qrCodeHost).append(qrTemplatePath);
        //uri.append("https://unified-dev.digit.org/credential-schema-service/template");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<QrTemplateClient> requestEntity = new HttpEntity<>(qrTemplateClient, headers);
        log.info("Constructed URI: {}", uri.toString());
        Object qrTemplateResponse = serviceRequestRepository.fetchResult(uri, requestEntity);
        String templateId=null;
        try{
            templateId = JsonPath.read(qrTemplateResponse, "$.template.templateId");
        }
        catch(Exception e){
            throw new CustomException("ID_NOT_FOUND", "id not found in the schema");
        }
        log.info("template id received from the request"+ templateId);
        String qrImageResponse= getQrImageFromTemplate(credentialIdObject, templateId);
        return qrImageResponse;
    }

    public String getQrImageFromTemplate(CredentialIdResponse credentialIdObject, String templateId){
        //using template id make get request for qr code by appending the template id as header
        StringBuilder qrRequestUri = new StringBuilder();
        qrRequestUri.append(credentialHost).append(qrImagePath);
        //qrRequestUri.append("https://unified-dev.digit.org/credentials-service/credentials/");
        String getQrRequestUri= qrRequestUri.toString()+ credentialIdObject.getCredentialId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("templateid", templateId);
        headers.set("Accept", "text/html");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("Entity Details:"+entity);
        ResponseEntity<String> response=null;
        try{
            response = restTemplate.exchange(getQrRequestUri, HttpMethod.GET, entity, String.class);

        }
        catch (Exception e){
            throw new CustomException("QRIMAGE_RESPONSE_ERR","failed to fetch qr ");
        }
        return response.getBody();
    }
}
