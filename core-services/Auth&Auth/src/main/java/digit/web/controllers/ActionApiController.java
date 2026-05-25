package digit.web.controllers;


import digit.web.models.Action;
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
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-07-19T14:07:58.669830457+05:30[Asia/Kolkata]")
@Controller
    @RequestMapping("/api")
    public class ActionApiController{

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        @Autowired
        public ActionApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
        }

                @RequestMapping(value="/action/_create", method = RequestMethod.POST)
                public ResponseEntity<Void> actionCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody Action body) {
                        String accept = request.getHeader("Accept");
                        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/action/_search", method = RequestMethod.GET)
                public ResponseEntity<List<Action>> actionSearchGet(@Parameter(in = ParameterIn.QUERY, description = "Action ID to search for." ,schema=@Schema()) @Valid @RequestParam(value = "id", required = false) String id,@Parameter(in = ParameterIn.QUERY, description = "URI of the action to search for." ,schema=@Schema()) @Valid @RequestParam(value = "uri", required = false) String uri) {
                        String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("application/json")) {
                            try {
                            return new ResponseEntity<List<Action>>(objectMapper.readValue("[ {  \"accessType\" : [ \"OPEN\", \"OPEN\" ],  \"description\" : \"description\",  \"id\" : \"id\",  \"tag\" : [ \"tag\", \"tag\" ],  \"isActive\" : true,  \"uri\" : \"uri\"}, {  \"accessType\" : [ \"OPEN\", \"OPEN\" ],  \"description\" : \"description\",  \"id\" : \"id\",  \"tag\" : [ \"tag\", \"tag\" ],  \"isActive\" : true,  \"uri\" : \"uri\"} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                            } catch (IOException e) {
                            return new ResponseEntity<List<Action>>(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            }

                        return new ResponseEntity<List<Action>>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/action/_update", method = RequestMethod.PUT)
                public ResponseEntity<Void> actionUpdatePut(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody Action body) {
                        String accept = request.getHeader("Accept");
                        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                }

        }
