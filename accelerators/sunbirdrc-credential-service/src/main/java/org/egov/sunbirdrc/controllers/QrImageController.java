package org.egov.sunbirdrc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.egov.sunbirdrc.models.QrCodeRequest;
import org.egov.sunbirdrc.service.QrImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/qrcode")
@Controller
@Slf4j
public class QrImageController {

    @Autowired
    private QrImageService qrImageService;
    @PostMapping("/_get")
    public ResponseEntity<String> getCredential(@RequestBody QrCodeRequest qrCodeRequest) throws JsonProcessingException {
        String response= qrImageService.getQrImage(qrCodeRequest);
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

}
