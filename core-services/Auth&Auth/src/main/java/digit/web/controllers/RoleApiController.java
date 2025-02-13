package digit.web.controllers;


import digit.web.models.Role;
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
    public class RoleApiController{

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        @Autowired
        public RoleApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
        }

                @RequestMapping(value="/role/_create", method = RequestMethod.POST)
                public ResponseEntity<Void> roleCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody Role body) {
                        String accept = request.getHeader("Accept");
                        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/role/_search", method = RequestMethod.GET)
                public ResponseEntity<List<Role>> roleSearchGet(@Parameter(in = ParameterIn.QUERY, description = "Tenant ID to search for roles." ,schema=@Schema()) @Valid @RequestParam(value = "tenantId", required = false) String tenantId,@Parameter(in = ParameterIn.QUERY, description = "Role code to search for." ,schema=@Schema()) @Valid @RequestParam(value = "code", required = false) String code) {
                        String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("application/json")) {
                            try {
                            return new ResponseEntity<List<Role>>(objectMapper.readValue("[ {  \"code\" : \"code\",  \"tenantId\" : \"tenantId\",  \"isActive\" : true}, {  \"code\" : \"code\",  \"tenantId\" : \"tenantId\",  \"isActive\" : true} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
                            } catch (IOException e) {
                            return new ResponseEntity<List<Role>>(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            }

                        return new ResponseEntity<List<Role>>(HttpStatus.NOT_IMPLEMENTED);
                }

                @RequestMapping(value="/role/_update", method = RequestMethod.PUT)
                public ResponseEntity<Void> roleUpdatePut(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody Role body) {
                        String accept = request.getHeader("Accept");
                        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
                }

        }
