package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.EstimateRequest;
import digit.web.models.EstimateResponse;
import digit.web.models.EstimateSearchRequest;
import digit.web.models.EstimateSearchResponse;
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
public class EstimateApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public EstimateApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/estimate/_create", method = RequestMethod.POST)
	public ResponseEntity<EstimateResponse> estimateCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new estimate + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody EstimateRequest body) {
		try {
			return new ResponseEntity<EstimateResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Estimate\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 1000,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", EstimateResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<EstimateResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/estimate/_search", method = RequestMethod.POST)
	public ResponseEntity<EstimateSearchResponse> estimateSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get esimate(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody EstimateSearchRequest body) {
		try {
			return new ResponseEntity<EstimateSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Estimates\" : [ {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 1000,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 1000,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", EstimateSearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<EstimateSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/estimate/_update", method = RequestMethod.POST)
	public ResponseEntity<EstimateResponse> estimateUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new estimate + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody EstimateRequest body) {
		try {
			return new ResponseEntity<EstimateResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Estimate\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"netAmount\" : 1000,    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", EstimateResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<EstimateResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
