package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.ProgramRequest;
import digit.web.models.ProgramResponse;
import digit.web.models.ProgramSearchRequest;
import digit.web.models.ProgramSearchResponse;
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
public class ProgramApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public ProgramApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/program/_create", method = RequestMethod.POST)
	public ResponseEntity<ProgramResponse> programCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of the new program + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProgramRequest body) {
		try {
			return new ResponseEntity<ProgramResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Program\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"endDate\" : 1704067200,    \"criteria\" : [ \"State will pay 45% of total amount\", \"State will pay 45% of total amount\" ],    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"additionalDetails\" : { },    \"startDate\" : 1672531200,    \"objective\" : [ \"55L of water to every household\", \"55L of water to every household\" ],    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", ProgramResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProgramResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/program/_search", method = RequestMethod.POST)
	public ResponseEntity<ProgramSearchResponse> programSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get programs based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody ProgramSearchRequest body) {
		try {
			return new ResponseEntity<ProgramSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Programs\" : [ {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"endDate\" : 1704067200,    \"criteria\" : [ \"State will pay 45% of total amount\", \"State will pay 45% of total amount\" ],    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"additionalDetails\" : { },    \"startDate\" : 1672531200,    \"objective\" : [ \"55L of water to every household\", \"55L of water to every household\" ],    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"endDate\" : 1704067200,    \"criteria\" : [ \"State will pay 45% of total amount\", \"State will pay 45% of total amount\" ],    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"additionalDetails\" : { },    \"startDate\" : 1672531200,    \"objective\" : [ \"55L of water to every household\", \"55L of water to every household\" ],    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", ProgramSearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProgramSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/program/_update", method = RequestMethod.POST)
	public ResponseEntity<ProgramResponse> programUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of the updated program + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProgramRequest body) {
		try {
			return new ResponseEntity<ProgramResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Program\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"endDate\" : 1704067200,    \"criteria\" : [ \"State will pay 45% of total amount\", \"State will pay 45% of total amount\" ],    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"additionalDetails\" : { },    \"startDate\" : 1672531200,    \"objective\" : [ \"55L of water to every household\", \"55L of water to every household\" ],    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", ProgramResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProgramResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
