package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.ProjectRequest;
import digit.web.models.ProjectResponse;
import digit.web.models.ProjectSearchRequest;
import digit.web.models.ProjectSearchResponse;
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
public class ProjectApiController {

	private final ObjectMapper objectMapper;

	private final HttpServletRequest request;

	@Autowired
	public ProjectApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@RequestMapping(value = "/project/_create", method = RequestMethod.POST)
	public ResponseEntity<ProjectResponse> projectCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of new project + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProjectRequest body) {
		try {
			return new ResponseEntity<ProjectResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Project\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", ProjectResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProjectResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/project/_search", method = RequestMethod.POST)
	public ResponseEntity<ProjectSearchResponse> projectSearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Search and get project(s) based on defined search criteria", required = true, schema = @Schema()) @Valid @RequestBody ProjectSearchRequest body) {
		try {
			return new ResponseEntity<ProjectSearchResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Projects\" : [ {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }, {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  } ]}", ProjectSearchResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProjectSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/project/_update", method = RequestMethod.POST)
	public ResponseEntity<ProjectResponse> projectUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Details of updated project + RequestInfo metadata", required = true, schema = @Schema()) @Valid @RequestBody ProjectRequest body) {
		try {
			return new ResponseEntity<ProjectResponse>(objectMapper.readValue("{  \"ResponseInfo\" : {    \"ver\" : \"ver\",    \"resMsgId\" : \"resMsgId\",    \"msgId\" : \"msgId\",    \"apiId\" : \"apiId\",    \"ts\" : 0,    \"status\" : \"SUCCESSFUL\"  },  \"Project\" : {    \"programCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"projectCode\" : \"PORJ/2023-24/00001\",    \"auditDetails\" : {      \"lastModifiedTime\" : 1,      \"createdBy\" : \"createdBy\",      \"lastModifiedBy\" : \"lastModifiedBy\",      \"createdTime\" : 6    },    \"tenantId\" : \"pb.jalandhar,dwss\",    \"name\" : \"Community Development Initiative\",    \"description\" : \"Empowering local communities through sustainable development projects.\",    \"id\" : \"251c51eb-e970-4e01-a99a-70136c47a934\",    \"agencyCode\" : \"PORG/2023-24/PG.CITYA/00001\",    \"additionalDetails\" : { },    \"status\" : {      \"statusMessage\" : \"statusMessage\",      \"statusCode\" : \"SUCCESSFUL\"    }  }}", ProjectResponse.class), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<ProjectResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
