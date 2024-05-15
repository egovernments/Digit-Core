package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.SanctionRequest;
import digit.web.models.SanctionResponse;
import digit.web.models.SanctionSearchRequest;
import digit.web.models.SanctionSearchResponse;
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
public class SanctionApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public SanctionApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/sanction/_create", method = RequestMethod.POST)
	public ResponseEntity<SanctionResponse> sanctionCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new sanction + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody SanctionRequest body) {
		try {
			return new ResponseEntity<SanctionResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Sanction\" : {    \"availableAmount\" : 1.4658129805029452,    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", SanctionResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<SanctionResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sanction/_search", method = RequestMethod.POST)
	public ResponseEntity<SanctionSearchResponse> sanctionSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get sanction(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody SanctionSearchRequest body) {
		try {
			return new ResponseEntity<SanctionSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Sanctions\" : [ {    \"availableAmount\" : 1.4658129805029452,    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"availableAmount\" : 1.4658129805029452,    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", SanctionSearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<SanctionSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sanction/_update", method = RequestMethod.POST)
	public ResponseEntity<SanctionResponse> sanctionUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated sanction + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody SanctionRequest body) {
		try {
			return new ResponseEntity<SanctionResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Sanction\" : {    \"availableAmount\" : 1.4658129805029452,    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 0.8008281904610115,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 6.027456183070403,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", SanctionResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<SanctionResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
