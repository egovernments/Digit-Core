package digit.web.controllers;


import digit.service.TenantService;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api")
public class TenantApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private TenantService tenantService;

    @Autowired
    public TenantApiController(ObjectMapper objectMapper, HttpServletRequest request, TenantService tenantService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.tenantService = tenantService;
    }

    @RequestMapping(value = "/tenant/config/_create", method = RequestMethod.POST)
    public ResponseEntity<Void> tenantConfigCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody Tenant body) {

        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/tenant/config/_search", method = RequestMethod.POST)
    public ResponseEntity<List<Tenant>> tenantConfigSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody Tenant body, @Parameter(in = ParameterIn.QUERY, description = "Tenant code to search for.", schema = @Schema()) @Valid @RequestParam(value = "tenantId", required = false) String tenantId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Tenant>>(objectMapper.readValue("[ {  \"code\" : \"code\",  \"auditDetails\" : {    \"lastModifiedTime\" : 3,    \"createdBy\" : \"createdBy\",    \"lastModifiedBy\" : \"lastModifiedBy\",    \"createdTime\" : 9  },  \"name\" : \"name\",  \"id\" : \"id\",  \"isActive\" : true,  \"email\" : \"\",  \"additionalAttributes\" : { }}, {  \"code\" : \"code\",  \"auditDetails\" : {    \"lastModifiedTime\" : 3,    \"createdBy\" : \"createdBy\",    \"lastModifiedBy\" : \"lastModifiedBy\",    \"createdTime\" : 9  },  \"name\" : \"name\",  \"id\" : \"id\",  \"isActive\" : true,  \"email\" : \"\",  \"additionalAttributes\" : { }} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<List<Tenant>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Tenant>>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/tenant/config/_update", method = RequestMethod.POST)
    public ResponseEntity<Void> tenantConfigUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody Tenant body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/tenant/_create", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody TenantRequest body) {
        TenantResponse tenantResponse = tenantService.create(body);
        return new ResponseEntity<TenantResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/tenant/_search", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantSearchPost(@Valid @RequestBody RequestInfo body, @ModelAttribute TenantDataSearchCriteria searchCriteria) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<TenantResponse>(objectMapper.readValue("{  \"Tenants\" : [ {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"id\",    \"isActive\" : true,    \"email\" : \"\",    \"additionalAttributes\" : { }  }, {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"id\",    \"isActive\" : true,    \"email\" : \"\",    \"additionalAttributes\" : { }  } ],  \"responseInfo\" : {    \"userInfo\" : {      \"pwdExpiryDate\" : 7,      \"correspondenceCity\" : \"correspondenceCity\",      \"accountLockedDate\" : 1,      \"gender\" : \"gender\",      \"signature\" : \"signature\",      \"mobileNumber\" : \"mobileNumber\",      \"roles\" : [ {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      }, {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      } ],      \"correspondencePincode\" : \"correspondencePincode\",      \"emailId\" : \"emailId\",      \"locale\" : \"locale\",      \"type\" : \"type\",      \"uuid\" : \"uuid\",      \"correspondenceAddress\" : \"correspondenceAddress\",      \"bloodGroup\" : \"bloodGroup\",      \"password\" : \"password\",      \"alternateMobileNumber\" : \"alternateMobileNumber\",      \"id\" : 6,      \"permanentAddress\" : \"permanentAddress\",      \"pan\" : \"pan\",      \"relationship\" : \"relationship\",      \"accountLocked\" : true,      \"altContactNumber\" : \"altContactNumber\",      \"identificationMark\" : \"identificationMark\",      \"lastModifiedDate\" : \"2000-01-23\",      \"lastModifiedBy\" : 5,      \"fatherOrHusbandName\" : \"fatherOrHusbandName\",      \"active\" : true,      \"photo\" : \"photo\",      \"userName\" : \"userName\",      \"aadhaarNumber\" : \"aadhaarNumber\",      \"createdDate\" : \"2000-01-23\",      \"createdBy\" : 5,      \"otpReference\" : \"otpReference\",      \"dob\" : 2,      \"tenantId\" : \"tenantId\",      \"name\" : \"name\",      \"salutation\" : \"salutation\",      \"permanentCity\" : \"permanentCity\",      \"permanentPincode\" : \"permanentPincode\"    },    \"ver\" : \"ver\",    \"requesterId\" : \"requesterId\",    \"authToken\" : \"authToken\",    \"action\" : \"action\",    \"msgId\" : \"msgId\",    \"correlationId\" : \"correlationId\",    \"apiId\" : \"apiId\",    \"did\" : \"did\",    \"key\" : \"key\",    \"ts\" : 0  }}", TenantResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<TenantResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<TenantResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/tenant/_update", method = RequestMethod.POST)
    public ResponseEntity<TenantResponse> tenantUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody TenantRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<TenantResponse>(objectMapper.readValue("{  \"Tenants\" : [ {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"id\",    \"isActive\" : true,    \"email\" : \"\",    \"additionalAttributes\" : { }  }, {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"id\",    \"isActive\" : true,    \"email\" : \"\",    \"additionalAttributes\" : { }  } ],  \"responseInfo\" : {    \"userInfo\" : {      \"pwdExpiryDate\" : 7,      \"correspondenceCity\" : \"correspondenceCity\",      \"accountLockedDate\" : 1,      \"gender\" : \"gender\",      \"signature\" : \"signature\",      \"mobileNumber\" : \"mobileNumber\",      \"roles\" : [ {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      }, {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      } ],      \"correspondencePincode\" : \"correspondencePincode\",      \"emailId\" : \"emailId\",      \"locale\" : \"locale\",      \"type\" : \"type\",      \"uuid\" : \"uuid\",      \"correspondenceAddress\" : \"correspondenceAddress\",      \"bloodGroup\" : \"bloodGroup\",      \"password\" : \"password\",      \"alternateMobileNumber\" : \"alternateMobileNumber\",      \"id\" : 6,      \"permanentAddress\" : \"permanentAddress\",      \"pan\" : \"pan\",      \"relationship\" : \"relationship\",      \"accountLocked\" : true,      \"altContactNumber\" : \"altContactNumber\",      \"identificationMark\" : \"identificationMark\",      \"lastModifiedDate\" : \"2000-01-23\",      \"lastModifiedBy\" : 5,      \"fatherOrHusbandName\" : \"fatherOrHusbandName\",      \"active\" : true,      \"photo\" : \"photo\",      \"userName\" : \"userName\",      \"aadhaarNumber\" : \"aadhaarNumber\",      \"createdDate\" : \"2000-01-23\",      \"createdBy\" : 5,      \"otpReference\" : \"otpReference\",      \"dob\" : 2,      \"tenantId\" : \"tenantId\",      \"name\" : \"name\",      \"salutation\" : \"salutation\",      \"permanentCity\" : \"permanentCity\",      \"permanentPincode\" : \"permanentPincode\"    },    \"ver\" : \"ver\",    \"requesterId\" : \"requesterId\",    \"authToken\" : \"authToken\",    \"action\" : \"action\",    \"msgId\" : \"msgId\",    \"correlationId\" : \"correlationId\",    \"apiId\" : \"apiId\",    \"did\" : \"did\",    \"key\" : \"key\",    \"ts\" : 0  }}", TenantResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<TenantResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<TenantResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

}
