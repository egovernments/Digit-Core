package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.AgencyRequest;
import digit.web.models.AgencyResponse;
import digit.web.models.AgencySearchRequest;
import digit.web.models.AgencySearchResponse;
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
public class AgencyApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public AgencyApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/agency/_create", method = RequestMethod.POST)
	public ResponseEntity<AgencyResponse> agencyCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new agency + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AgencyRequest body) {
		try {
			return new ResponseEntity<AgencyResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Agency\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pg.citya\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"orgNumber\" : \"orgNumber\",    \"agencyType\" : \"Funding Agency\"  }}", AgencyResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AgencyResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/agency/_search", method = RequestMethod.POST)
	public ResponseEntity<AgencySearchResponse> agencySearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get agency(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody AgencySearchRequest body) {
		try {
			return new ResponseEntity<AgencySearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Agencies\" : [ {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pg.citya\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"orgNumber\" : \"orgNumber\",    \"agencyType\" : \"Funding Agency\"  }, {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pg.citya\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"orgNumber\" : \"orgNumber\",    \"agencyType\" : \"Funding Agency\"  } ]}", AgencySearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AgencySearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/agency/_update", method = RequestMethod.POST)
	public ResponseEntity<AgencyResponse> agencyUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated agency + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody AgencyRequest body) {
		try {
			return new ResponseEntity<AgencyResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Agency\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pg.citya\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"orgNumber\" : \"orgNumber\",    \"agencyType\" : \"Funding Agency\"  }}", AgencyResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<AgencyResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
