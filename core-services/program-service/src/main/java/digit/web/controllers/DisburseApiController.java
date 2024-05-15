package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.DisburseRequest;
import digit.web.models.DisburseResponse;
import digit.web.models.DisburseSearchRequest;
import digit.web.models.DisburseSearchResponse;
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
public class DisburseApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public DisburseApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/disburse/_create", method = RequestMethod.POST)
	public ResponseEntity<DisburseResponse> disburseCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody DisburseRequest body) {
		try {
			return new ResponseEntity<DisburseResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Disbursement\" : {    \"accountCode\" : \"1234567890@SBIN0003491\",    \"targetId\" : \"EP/0/2023-24/08/14/000267, 251c51eb-e970-4e01-a99a-70136c47a934\",    \"individual\" : {      \"address\" : \"address\",      \"pin\" : \"pin\",      \"phone\" : \"phone\",      \"name\" : \"name\",      \"email\" : \"email\"    },    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"netAmount\" : 1000,    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"transactionId\" : \"PI/2023-24/00001,BENF/2023-24/00001\",    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", DisburseResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<DisburseResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/disburse/_search", method = RequestMethod.POST)
	public ResponseEntity<DisburseSearchResponse> disburseSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get disbursement(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody DisburseSearchRequest body) {
			try {
				return new ResponseEntity<DisburseSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Disbursements\" : [ {    \"accountCode\" : \"1234567890@SBIN0003491\",    \"targetId\" : \"EP/0/2023-24/08/14/000267, 251c51eb-e970-4e01-a99a-70136c47a934\",    \"individual\" : {      \"address\" : \"address\",      \"pin\" : \"pin\",      \"phone\" : \"phone\",      \"name\" : \"name\",      \"email\" : \"email\"    },    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"netAmount\" : 1000,    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"transactionId\" : \"PI/2023-24/00001,BENF/2023-24/00001\",    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"accountCode\" : \"1234567890@SBIN0003491\",    \"targetId\" : \"EP/0/2023-24/08/14/000267, 251c51eb-e970-4e01-a99a-70136c47a934\",    \"individual\" : {      \"address\" : \"address\",      \"pin\" : \"pin\",      \"phone\" : \"phone\",      \"name\" : \"name\",      \"email\" : \"email\"    },    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"netAmount\" : 1000,    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"transactionId\" : \"PI/2023-24/00001,BENF/2023-24/00001\",    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", DisburseSearchResponse.class), HttpStatus.OK);
			} catch (IOException e) {
				return new ResponseEntity<DisburseSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}

	@RequestMapping(value = "/disburse/_update", method = RequestMethod.POST)
	public ResponseEntity<DisburseResponse> disburseUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated allocation + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody DisburseRequest body) {
			try {
				return new ResponseEntity<DisburseResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Disbursement\" : {    \"accountCode\" : \"1234567890@SBIN0003491\",    \"targetId\" : \"EP/0/2023-24/08/14/000267, 251c51eb-e970-4e01-a99a-70136c47a934\",    \"individual\" : {      \"address\" : \"address\",      \"pin\" : \"pin\",      \"phone\" : \"phone\",      \"name\" : \"name\",      \"email\" : \"email\"    },    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"netAmount\" : 1000,    \"grossAmount\" : 1000,    \"additionalDetails\" : { },    \"transactionId\" : \"PI/2023-24/00001,BENF/2023-24/00001\",    \"sanctionId\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", DisburseResponse.class), HttpStatus.OK);
			} catch (IOException e) {
				return new ResponseEntity<DisburseResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}

}
