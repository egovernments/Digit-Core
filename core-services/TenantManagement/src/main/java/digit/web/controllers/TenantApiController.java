package digit.web.controllers;


import digit.config.ApplicationConfig;
import digit.kafka.Producer;
import digit.service.TenantConfigService;
import digit.service.TenantService;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.email.EmailRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.egov.common.contract.request.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class TenantApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private TenantService tenantService;

    private TenantConfigService tenantConfigService;

    private Producer producer;

    private ApplicationConfig applicationConfig;

    @Autowired
    public TenantApiController(ObjectMapper objectMapper, HttpServletRequest request, TenantService tenantService, TenantConfigService tenantConfigService, Producer producer, ApplicationConfig applicationConfig) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.tenantService = tenantService;
        this.tenantConfigService = tenantConfigService;
        this.producer = producer;
        this.applicationConfig = applicationConfig;
    }

    @RequestMapping(value = "/tenant/config/_create", method = RequestMethod.POST)
    public ResponseEntity<TenantConfigResponse> tenantConfigCreatePost(@Valid @RequestBody TenantConfigRequest tenantConfigRequest) {

        TenantConfigResponse tenantConfigResponse = tenantConfigService.create(tenantConfigRequest);

        return new ResponseEntity<>(tenantConfigResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/tenant/config/_search", method = RequestMethod.POST)
    public ResponseEntity<TenantConfigResponse> tenantConfigSearchPost(@Valid @RequestBody RequestInfo requestInfo, @ModelAttribute TenantConfigSearchCriteria searchCriteria) {
        TenantConfigResponse tenantConfigResponse =  tenantConfigService.search(searchCriteria,requestInfo);
        return new ResponseEntity<>(tenantConfigResponse,HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/tenant/config/_update", method = RequestMethod.POST)
    public ResponseEntity<TenantConfigResponse> tenantConfigUpdatePost(@Valid @RequestBody TenantConfigRequest tenantConfigRequest) {
        TenantConfigResponse tenantConfigResponse = tenantConfigService.update(tenantConfigRequest);
        return new ResponseEntity<TenantConfigResponse>(tenantConfigResponse,HttpStatus.ACCEPTED);
    }

    // TODO: Create a switch to disable this
    @RequestMapping(value = "/tenant/_create", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantCreatePost(@Valid @RequestBody TenantRequest body) {
        TenantResponse tenantResponse = tenantService.create(body);
        return new ResponseEntity<TenantResponse>(tenantResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/tenant/_search", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantSearchPost(@Valid @RequestBody RequestInfo body, @ModelAttribute TenantDataSearchCriteria searchCriteria) {

        TenantResponse tenantResponse = tenantService.search(searchCriteria, body);
        return new ResponseEntity<TenantResponse>(tenantResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/tenant/_update", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantUpdatePost(@Valid @RequestBody TenantRequest body) {
        TenantResponse tenantResponse = tenantService.update(body);
        return new ResponseEntity<TenantResponse>(tenantResponse, HttpStatus.ACCEPTED);
    }

}
