package digit.web.controllers;


import digit.service.SubTenantService;
import digit.service.TenantService;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class SubTenantApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final SubTenantService subTenantService;

    @Autowired
    public SubTenantApiController(ObjectMapper objectMapper, HttpServletRequest request, SubTenantService subTenantService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.subTenantService = subTenantService;
    }

    @RequestMapping(value = "/subTenant/_create", method = RequestMethod.POST)
    public ResponseEntity<SubTenantResponse> subTenantCreatePost(@Valid @RequestBody SubTenantRequest body) {
        SubTenantResponse tenantResponse = subTenantService.create(body);
        return new ResponseEntity<SubTenantResponse>(tenantResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/subTenant/_search", method = RequestMethod.POST)
    public ResponseEntity<SubTenantResponse> subTenantSearchPost(@Valid @ModelAttribute SubTenantDataSearchCriteria searchCriteria, RequestInfo requestInfo) {
        SubTenantResponse subTenantResponse = subTenantService.search(searchCriteria,requestInfo);
        return new ResponseEntity<SubTenantResponse>(subTenantResponse,HttpStatus.OK);
    }

    @RequestMapping(value = "/subTenant/_update", method = RequestMethod.POST)
    public ResponseEntity<SubTenantResponse> subTenantUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody SubTenantRequest body) {
        SubTenantResponse subTenantResponse = subTenantService.update(body);
        return new ResponseEntity<SubTenantResponse>(subTenantResponse,HttpStatus.ACCEPTED);
    }

}
