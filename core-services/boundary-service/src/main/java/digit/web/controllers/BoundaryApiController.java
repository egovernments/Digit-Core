package digit.web.controllers;


import digit.service.BoundaryService;
import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipResponse;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundaryResponse;
import digit.web.models.BoundarySearchResponse;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchyResponse;
import digit.web.models.BoundaryTypeHierarchySearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class BoundaryApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    private final BoundaryService boundaryService;

    @Autowired
    public BoundaryApiController(ObjectMapper objectMapper, HttpServletRequest request, BoundaryService boundaryService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.boundaryService = boundaryService;
    }

    @RequestMapping(value = "/boundary/boundary-relationships/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> boundaryBoundaryRelationshipsCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", schema = @Schema()) @Valid @RequestBody BoundaryRelationshipRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryRelationshipResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"TenantBoundary\" : [ {    \"parent\" : \"parent\",    \"code\" : \"code\",    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\",    \"boundaryType\" : \"boundaryType\",    \"id\" : \"id\"  }, {    \"parent\" : \"parent\",    \"code\" : \"code\",    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\",    \"boundaryType\" : \"boundaryType\",    \"id\" : \"id\"  } ]}", BoundaryRelationshipResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryRelationshipResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryRelationshipResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/boundary-relationships/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundarySearchResponse> boundaryBoundaryRelationshipsSearchPost(@NotNull @Size(max = 256) @Parameter(in = ParameterIn.QUERY, description = "unique id for a tenant.", required = true, schema = @Schema()) @Valid @RequestParam(value = "tenantId", required = true) String tenantId, @Size(max = 64) @Parameter(in = ParameterIn.QUERY, description = "boundary type within the tenant boundary structure.", schema = @Schema()) @Valid @RequestParam(value = "boundaryType", required = false) String boundaryType, @Size(max = 128) @Parameter(in = ParameterIn.QUERY, description = "Type Of the BoundaryType Like REVENUE, ADMIN", schema = @Schema()) @Valid @RequestParam(value = "hierarchyType", required = false) String hierarchyType, @Parameter(in = ParameterIn.QUERY, description = "boolean flag to inform the service if children need to be part of search.", schema = @Schema()) @Valid @RequestParam(value = "includeChildren", required = false) Boolean includeChildren, @Parameter(in = ParameterIn.QUERY, description = "boolean flag to inform the service if parents need to be part of search.", schema = @Schema()) @Valid @RequestParam(value = "includeParents", required = false) Boolean includeParents, @Parameter(in = ParameterIn.QUERY, description = "unique List of boundary codes.", schema = @Schema()) @Valid @RequestParam(value = "codes", required = false) List<String> codes) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundarySearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"TenantBoundary\" : [ {    \"boundary\" : {      \"code\" : \"code\",      \"children\" : [ null, null ],      \"tenantId\" : \"tenantId\",      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    },    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\"  }, {    \"boundary\" : {      \"code\" : \"code\",      \"children\" : [ null, null ],      \"tenantId\" : \"tenantId\",      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    },    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\"  } ]}", BoundarySearchResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundarySearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundarySearchResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/boundary-relationships/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> boundaryBoundaryRelationshipsUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", schema = @Schema()) @Valid @RequestBody BoundaryRelationshipRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryRelationshipResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"TenantBoundary\" : [ {    \"parent\" : \"parent\",    \"code\" : \"code\",    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\",    \"boundaryType\" : \"boundaryType\",    \"id\" : \"id\"  }, {    \"parent\" : \"parent\",    \"code\" : \"code\",    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\",    \"boundaryType\" : \"boundaryType\",    \"id\" : \"id\"  } ]}", BoundaryRelationshipResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryRelationshipResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryRelationshipResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> boundaryCreatePost(@Valid @RequestBody BoundaryRequest body) {
        BoundaryResponse boundaryResponse = boundaryService.createBoundary(body);
        return new ResponseEntity<BoundaryResponse>(boundaryResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/boundary/hierarchy-definition/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> boundaryHierarchyDefinitionCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", schema = @Schema()) @Valid @RequestBody BoundaryTypeHierarchyRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryTypeHierarchyResponse>(objectMapper.readValue("{  \"BoundaryHierarchy\" : {    \"boundaryHierarchy\" : [ {      \"parentBoundaryType\" : \"parentBoundaryType\",      \"active\" : true,      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    }, {      \"parentBoundaryType\" : \"parentBoundaryType\",      \"active\" : true,      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    } ],    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\"  },  \"RequestInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  }}", BoundaryTypeHierarchyResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryTypeHierarchyResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryTypeHierarchyResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/hierarchy-definition/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryTypeHierarchyResponse> boundaryHierarchyDefinitionSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "", schema = @Schema()) @Valid @RequestBody BoundaryTypeHierarchySearchRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryTypeHierarchyResponse>(objectMapper.readValue("{  \"BoundaryHierarchy\" : {    \"boundaryHierarchy\" : [ {      \"parentBoundaryType\" : \"parentBoundaryType\",      \"active\" : true,      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    }, {      \"parentBoundaryType\" : \"parentBoundaryType\",      \"active\" : true,      \"boundaryType\" : \"boundaryType\",      \"id\" : \"id\"    } ],    \"hierarchyType\" : \"hierarchyType\",    \"tenantId\" : \"tenantId\"  },  \"RequestInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  }}", BoundaryTypeHierarchyResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryTypeHierarchyResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryTypeHierarchyResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> boundarySearchPost(@NotNull @Parameter(in = ParameterIn.QUERY, description = "unique list of boundary codes.", required = true, schema = @Schema()) @Valid @RequestParam(value = "codes", required = true) List<String> codes) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Boundary\" : [ {    \"code\" : \"code\",    \"tenantId\" : \"tenantId\",    \"geometry\" : {      \"coordinates\" : [ [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ], [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ] ],      \"type\" : \"Polygon\"    },    \"id\" : \"id\"  }, {    \"code\" : \"code\",    \"tenantId\" : \"tenantId\",    \"geometry\" : {      \"coordinates\" : [ [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ], [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ] ],      \"type\" : \"Polygon\"    },    \"id\" : \"id\"  } ]}", BoundaryResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/boundary/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryResponse> boundaryUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", schema = @Schema()) @Valid @RequestBody BoundaryRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<BoundaryResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Boundary\" : [ {    \"code\" : \"code\",    \"tenantId\" : \"tenantId\",    \"geometry\" : {      \"coordinates\" : [ [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ], [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ] ],      \"type\" : \"Polygon\"    },    \"id\" : \"id\"  }, {    \"code\" : \"code\",    \"tenantId\" : \"tenantId\",    \"geometry\" : {      \"coordinates\" : [ [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ], [ [ 6.027456183070403, 6.027456183070403 ], [ 6.027456183070403, 6.027456183070403 ] ] ],      \"type\" : \"Polygon\"    },    \"id\" : \"id\"  } ]}", BoundaryResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<BoundaryResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BoundaryResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

}
