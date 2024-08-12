package digit.web.controllers;


import digit.web.models.ErrorRes;
import digit.web.models.RequestInfo;
import digit.web.models.SubTenantRequest;
import digit.web.models.SubTenantResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.util.*;

    import jakarta.validation.constraints.*;
    import jakarta.validation.Valid;
    import jakarta.servlet.http.HttpServletRequest;
        import java.util.Optional;
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-08-12T11:40:14.091712534+05:30[Asia/Kolkata]")
@Controller
    @RequestMapping("/api")
    public class SubTenantApiController{

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        @Autowired
        public SubTenantApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
        }

                @RequestMapping(value="/subTenant/_create", method = RequestMethod.POST)
                public ResponseEntity<SubTenantResponse> subTenantCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody SubTenantRequest body) {
                        String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("application/json")) {
                            try {
                            return new ResponseEntity<SubTenantResponse>(objectMapper.readValue("{  \"Tenants\" : [ {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  }, {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  } ],  \"responseInfo\" : {    \"userInfo\" : {      \"pwdExpiryDate\" : 7,      \"correspondenceCity\" : \"correspondenceCity\",      \"accountLockedDate\" : 1,      \"gender\" : \"gender\",      \"signature\" : \"signature\",      \"mobileNumber\" : \"mobileNumber\",      \"roles\" : [ {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      }, {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      } ],      \"correspondencePincode\" : \"correspondencePincode\",      \"emailId\" : \"emailId\",      \"locale\" : \"locale\",      \"type\" : \"type\",      \"uuid\" : \"uuid\",      \"correspondenceAddress\" : \"correspondenceAddress\",      \"bloodGroup\" : \"bloodGroup\",      \"password\" : \"password\",      \"alternateMobileNumber\" : \"alternateMobileNumber\",      \"id\" : 6,      \"permanentAddress\" : \"permanentAddress\",      \"pan\" : \"pan\",      \"relationship\" : \"relationship\",      \"accountLocked\" : true,      \"altContactNumber\" : \"altContactNumber\",      \"identificationMark\" : \"identificationMark\",      \"lastModifiedDate\" : \"2000-01-23\",      \"lastModifiedBy\" : 5,      \"fatherOrHusbandName\" : \"fatherOrHusbandName\",      \"active\" : true,      \"photo\" : \"photo\",      \"userName\" : \"userName\",      \"aadhaarNumber\" : \"aadhaarNumber\",      \"createdDate\" : \"2000-01-23\",      \"createdBy\" : 5,      \"otpReference\" : \"otpReference\",      \"dob\" : 2,      \"tenantId\" : \"tenantId\",      \"name\" : \"name\",      \"salutation\" : \"salutation\",      \"permanentCity\" : \"permanentCity\",      \"permanentPincode\" : \"permanentPincode\"    },    \"ver\" : \"ver\",    \"requesterId\" : \"requesterId\",    \"authToken\" : \"authToken\",    \"action\" : \"action\",    \"msgId\" : \"msgId\",    \"correlationId\" : \"correlationId\",    \"apiId\" : \"apiId\",    \"did\" : \"did\",    \"key\" : \"key\",    \"ts\" : 0  }}", SubTenantResponse.class), HttpStatus.NOT_IMPLEMENTED);
                            } catch (IOException e) {
                            return new ResponseEntity<SubTenantResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            }

                        return new ResponseEntity<SubTenantResponse>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/subTenant/_search", method = RequestMethod.POST)
                public ResponseEntity<SubTenantResponse> subTenantSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody RequestInfo body,@Parameter(in = ParameterIn.QUERY, description = "SubTenant code to search for." ,schema=@Schema()) @Valid @RequestParam(value = "code", required = false) String code,@Parameter(in = ParameterIn.QUERY, description = "SubTenant name to search for." ,schema=@Schema()) @Valid @RequestParam(value = "name", required = false) String name,@Parameter(in = ParameterIn.QUERY, description = "Search based on parentId" ,schema=@Schema()) @Valid @RequestParam(value = "parentId", required = false) String parentId) {
                        String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("application/json")) {
                            try {
                            return new ResponseEntity<SubTenantResponse>(objectMapper.readValue("{  \"Tenants\" : [ {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  }, {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  } ],  \"responseInfo\" : {    \"userInfo\" : {      \"pwdExpiryDate\" : 7,      \"correspondenceCity\" : \"correspondenceCity\",      \"accountLockedDate\" : 1,      \"gender\" : \"gender\",      \"signature\" : \"signature\",      \"mobileNumber\" : \"mobileNumber\",      \"roles\" : [ {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      }, {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      } ],      \"correspondencePincode\" : \"correspondencePincode\",      \"emailId\" : \"emailId\",      \"locale\" : \"locale\",      \"type\" : \"type\",      \"uuid\" : \"uuid\",      \"correspondenceAddress\" : \"correspondenceAddress\",      \"bloodGroup\" : \"bloodGroup\",      \"password\" : \"password\",      \"alternateMobileNumber\" : \"alternateMobileNumber\",      \"id\" : 6,      \"permanentAddress\" : \"permanentAddress\",      \"pan\" : \"pan\",      \"relationship\" : \"relationship\",      \"accountLocked\" : true,      \"altContactNumber\" : \"altContactNumber\",      \"identificationMark\" : \"identificationMark\",      \"lastModifiedDate\" : \"2000-01-23\",      \"lastModifiedBy\" : 5,      \"fatherOrHusbandName\" : \"fatherOrHusbandName\",      \"active\" : true,      \"photo\" : \"photo\",      \"userName\" : \"userName\",      \"aadhaarNumber\" : \"aadhaarNumber\",      \"createdDate\" : \"2000-01-23\",      \"createdBy\" : 5,      \"otpReference\" : \"otpReference\",      \"dob\" : 2,      \"tenantId\" : \"tenantId\",      \"name\" : \"name\",      \"salutation\" : \"salutation\",      \"permanentCity\" : \"permanentCity\",      \"permanentPincode\" : \"permanentPincode\"    },    \"ver\" : \"ver\",    \"requesterId\" : \"requesterId\",    \"authToken\" : \"authToken\",    \"action\" : \"action\",    \"msgId\" : \"msgId\",    \"correlationId\" : \"correlationId\",    \"apiId\" : \"apiId\",    \"did\" : \"did\",    \"key\" : \"key\",    \"ts\" : 0  }}", SubTenantResponse.class), HttpStatus.NOT_IMPLEMENTED);
                            } catch (IOException e) {
                            return new ResponseEntity<SubTenantResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            }

                        return new ResponseEntity<SubTenantResponse>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/subTenant/_update", method = RequestMethod.POST)
                public ResponseEntity<SubTenantResponse> subTenantUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody SubTenantRequest body) {
                        String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("application/json")) {
                            try {
                            return new ResponseEntity<SubTenantResponse>(objectMapper.readValue("{  \"Tenants\" : [ {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  }, {    \"code\" : \"code\",    \"auditDetails\" : {      \"lastModifiedTime\" : 3,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 9    },    \"name\" : \"name\",    \"id\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\",    \"isActive\" : true,    \"parentId\" : \"parentId\",    \"additionalAttributes\" : { }  } ],  \"responseInfo\" : {    \"userInfo\" : {      \"pwdExpiryDate\" : 7,      \"correspondenceCity\" : \"correspondenceCity\",      \"accountLockedDate\" : 1,      \"gender\" : \"gender\",      \"signature\" : \"signature\",      \"mobileNumber\" : \"mobileNumber\",      \"roles\" : [ {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      }, {        \"tenantId\" : \"tenantId\",        \"name\" : \"name\",        \"description\" : \"description\",        \"id\" : \"id\"      } ],      \"correspondencePincode\" : \"correspondencePincode\",      \"emailId\" : \"emailId\",      \"locale\" : \"locale\",      \"type\" : \"type\",      \"uuid\" : \"uuid\",      \"correspondenceAddress\" : \"correspondenceAddress\",      \"bloodGroup\" : \"bloodGroup\",      \"password\" : \"password\",      \"alternateMobileNumber\" : \"alternateMobileNumber\",      \"id\" : 6,      \"permanentAddress\" : \"permanentAddress\",      \"pan\" : \"pan\",      \"relationship\" : \"relationship\",      \"accountLocked\" : true,      \"altContactNumber\" : \"altContactNumber\",      \"identificationMark\" : \"identificationMark\",      \"lastModifiedDate\" : \"2000-01-23\",      \"lastModifiedBy\" : 5,      \"fatherOrHusbandName\" : \"fatherOrHusbandName\",      \"active\" : true,      \"photo\" : \"photo\",      \"userName\" : \"userName\",      \"aadhaarNumber\" : \"aadhaarNumber\",      \"createdDate\" : \"2000-01-23\",      \"createdBy\" : 5,      \"otpReference\" : \"otpReference\",      \"dob\" : 2,      \"tenantId\" : \"tenantId\",      \"name\" : \"name\",      \"salutation\" : \"salutation\",      \"permanentCity\" : \"permanentCity\",      \"permanentPincode\" : \"permanentPincode\"    },    \"ver\" : \"ver\",    \"requesterId\" : \"requesterId\",    \"authToken\" : \"authToken\",    \"action\" : \"action\",    \"msgId\" : \"msgId\",    \"correlationId\" : \"correlationId\",    \"apiId\" : \"apiId\",    \"did\" : \"did\",    \"key\" : \"key\",    \"ts\" : 0  }}", SubTenantResponse.class), HttpStatus.NOT_IMPLEMENTED);
                            } catch (IOException e) {
                            return new ResponseEntity<SubTenantResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            }

                        return new ResponseEntity<SubTenantResponse>(HttpStatus.NOT_IMPLEMENTED);
                }

        }
