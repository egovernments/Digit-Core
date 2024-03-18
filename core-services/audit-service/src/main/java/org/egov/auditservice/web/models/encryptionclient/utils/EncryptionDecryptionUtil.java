//package org.egov.auditservice.web.models.encryptionclient.utils;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import lombok.extern.slf4j.Slf4j;
//import org.egov.encryption.EncryptionService;
//import org.egov.tracer.model.CustomException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.HttpServerErrorException;
//import org.springframework.web.client.ResourceAccessException;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//public class EncryptionDecryptionUtil {
//
//    private EncryptionService encryptionService;
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    public EncryptionDecryptionUtil(EncryptionService encryptionService) {
//        this.encryptionService = encryptionService;
//    }
//
//    public <T> T encryptObject(Object objectToEncrypt, String key, String tenantId, Class<T> classType) {
//        try {
//            if (objectToEncrypt == null) {
//                return null;
//            }
//            T encryptedObject = encryptionService.encryptJson(objectToEncrypt, key, tenantId, classType);
//            if (encryptedObject == null) {
//                throw new CustomException("ENCRYPTION_NULL_ERROR", "Null object found on performing encryption");
//            }
//            return encryptedObject;
//        } catch (IOException | HttpClientErrorException | HttpServerErrorException | ResourceAccessException e) {
//            log.error("Error occurred while encrypting", e);
//            throw new CustomException("ENCRYPTION_ERROR", "Error occurred in encryption process");
//        } catch (Exception e) {
//            log.error("Unknown Error occurred while encrypting", e);
//            throw new CustomException("UNKNOWN_ERROR", "Unknown error occurred in encryption process");
//        }
//    }
//
//}
