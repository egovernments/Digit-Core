package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.AllocationRequest;
import digit.web.models.AllocationResponse;
import digit.web.models.AllocationSearchRequest;
import digit.web.models.AllocationSearchResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-02T12:10:17.717685845+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class AllocationApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public AllocationApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/allocation/_create", method = RequestMethod.POST)
	public ResponseEntity<AllocationResponse> allocationCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AllocationRequest body) {
		try {
			return new ResponseEntity<AllocationResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Allocation\" : {    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"allocationType\" : \"ALLOCATION\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", AllocationResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AllocationResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/allocation/_search", method = RequestMethod.POST)
	public ResponseEntity<AllocationSearchResponse> allocationSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get allocation(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody AllocationSearchRequest body) {
		try {
			return new ResponseEntity<AllocationSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Allocations\" : [ {    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"allocationType\" : \"ALLOCATION\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"allocationType\" : \"ALLOCATION\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", AllocationSearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AllocationSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/allocation/_update", method = RequestMethod.POST)
	public ResponseEntity<AllocationResponse> allocationUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AllocationRequest body) {
		try {
			return new ResponseEntity<AllocationResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Allocation\" : {    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"allocationType\" : \"ALLOCATION\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", AllocationResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AllocationResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
