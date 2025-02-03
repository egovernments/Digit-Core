package digit.web.controllers;


import digit.web.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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
@RequestMapping("/user")
public class UserApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public UserApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @RequestMapping(value = "/users/_createnovalidate", method = RequestMethod.POST)
    public ResponseEntity<Void> userCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody User body) {


        String accept = request.getHeader("Accept");

        // Replace with your Keycloak server details
        String serverUrl = "http://localhost:8081";
        String realm = "demo";
        String clientId = "realm-management";
        String username = "demo";
        String password = "demo";
        String clientSecret="H5RfwajW4YC958JakzzfR9A9DdxBlvkC";
        // Initialize Keycloak client
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .username(username)
                .clientSecret(clientSecret)
                .password(password)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(body.getUsername());
        user.setFirstName("New");
        user.setLastName("User");
        user.setEmail("newuser@example.com");
        user.setEnabled(true);

        RealmResource realmResource = keycloak.realm("demo");
        UsersResource usersResource = realmResource.users();
        usersResource.create(user);


        System.out.println(usersResource.search(body.getUsername()));
        String userId = usersResource.search(body.getUsername()).get(0).getId();
        CredentialRepresentation Password = new CredentialRepresentation();
        Password.setTemporary(false);
        Password.setType(CredentialRepresentation.PASSWORD);
        Password.setValue(body.getPassword());

        usersResource.get(userId).resetPassword(Password);


        System.out.println("User created with ID: " + userId);

        System.out.println(keycloak.serverInfo().getInfo().getProviders());

        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/_search", method = RequestMethod.GET)
    public ResponseEntity<List<User>> userSearchGet(@Parameter(in = ParameterIn.QUERY, description = "Tenant ID to search for users.", schema = @Schema()) @Valid @RequestParam(value = "tenantId", required = false) String tenantId, @Parameter(in = ParameterIn.QUERY, description = "Username to search for users.", schema = @Schema()) @Valid @RequestParam(value = "username", required = false) String username) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<User>>(objectMapper.readValue("[ {  \"password\" : \"password\",  \"loginType\" : \"loginType\",  \"roles\" : [ {    \"role\" : \"role\",    \"subTenant\" : \"subTenant\"  }, {    \"role\" : \"role\",    \"subTenant\" : \"subTenant\"  } ],  \"tenantId\" : \"tenantId\",  \"username\" : \"username\"}, {  \"password\" : \"password\",  \"loginType\" : \"loginType\",  \"roles\" : [ {    \"role\" : \"role\",    \"subTenant\" : \"subTenant\"  }, {    \"role\" : \"role\",    \"subTenant\" : \"subTenant\"  } ],  \"tenantId\" : \"tenantId\",  \"username\" : \"username\"} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<List<User>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<User>>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/user/_update", method = RequestMethod.PUT)
    public ResponseEntity<Void> userUpdatePut(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody User body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}
