package org.egov.user.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.user.TestConfiguration;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.MobileNumberValidator;
import org.egov.user.domain.service.TokenService;
import org.egov.user.domain.service.UserService;
import org.egov.user.security.CustomAuthenticationKeyGenerator;
import org.egov.user.web.contract.factory.ResponseInfoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(BulkUserController.class)
@Import(TestConfiguration.class)
public class BulkUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkUserService bulkUserService;

    @MockBean
    private ResponseInfoFactory responseInfoFactory;

    // Beans required by EgovUserApplication autowiring — mocked out so the
    // WebMvcTest slice can build without wiring the full context.
    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private MobileNumberValidator mobileNumberValidator;

    @MockBean
    private CustomAuthenticationKeyGenerator authenticationKeyGenerator;

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(), anyBoolean()))
                .thenReturn(ResponseInfo.builder().status("successful").build());
    }

    @Test
    @WithMockUser
    public void empty_users_list_returns_200_with_empty_response() throws Exception {
        String body = "{\"RequestInfo\":{\"apiId\":\"t\"},\"users\":[]}";

        mockMvc.perform(post("/users/v2/_create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()", is(0)));
    }

    @Test
    @WithMockUser
    public void bulk_create_returns_all_users_with_ids() throws Exception {
        List<User> saved = Arrays.asList(
                User.builder().id(1L).username("emp_1").type(UserType.EMPLOYEE).build(),
                User.builder().id(2L).username("emp_2").type(UserType.EMPLOYEE).build());
        when(bulkUserService.createUsersBulk(anyList(), any()))
                .thenReturn(new org.egov.user.v2.BulkUserService.Result(saved, java.util.Collections.emptyList()));

        String body = mapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("RequestInfo", new java.util.HashMap<String, Object>() {{ put("apiId", "t"); }});
            put("users", Arrays.asList(
                    new java.util.HashMap<String, Object>() {{
                        put("username", "emp_1");
                        put("type", "EMPLOYEE");
                        put("tenantId", "os.osun");
                    }},
                    new java.util.HashMap<String, Object>() {{
                        put("username", "emp_2");
                        put("type", "EMPLOYEE");
                        put("tenantId", "os.osun");
                    }}));
        }});

        mockMvc.perform(post("/users/v2/_create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()", is(2)))
                .andExpect(jsonPath("$.users[0].id", is(1)))
                .andExpect(jsonPath("$.users[1].id", is(2)));
    }

    @Test
    @WithMockUser
    public void bulk_create_with_duplicate_returns_id_null_for_failed_user() throws Exception {
        List<User> saved = Arrays.asList(
                User.builder().id(1L).username("emp_1").type(UserType.EMPLOYEE).build(),
                User.builder().id(null).username("emp_dup").type(UserType.EMPLOYEE).build());
        java.util.Map<String, Object> err = new java.util.HashMap<>();
        err.put("username", "emp_dup");
        err.put("code", "EGOV_USER_V2_BULK_USERNAME_ALREADY_EXISTS_IN_DB");
        err.put("message", "A user with this username, type and tenantId already exists in eg_user.");
        when(bulkUserService.createUsersBulk(anyList(), any()))
                .thenReturn(new org.egov.user.v2.BulkUserService.Result(saved, java.util.Collections.singletonList(err)));

        String body = "{\"RequestInfo\":{\"apiId\":\"t\"},\"users\":[" +
                "{\"username\":\"emp_1\",\"type\":\"EMPLOYEE\",\"tenantId\":\"os.osun\"}," +
                "{\"username\":\"emp_dup\",\"type\":\"EMPLOYEE\",\"tenantId\":\"os.osun\"}]}";

        mockMvc.perform(post("/users/v2/_create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()", is(2)))
                .andExpect(jsonPath("$.users[0].id", is(1)))
                .andExpect(jsonPath("$.users[1].id").doesNotExist());
    }

    @Test
    @WithMockUser
    public void missing_users_field_returns_empty_response() throws Exception {
        String body = "{\"RequestInfo\":{\"apiId\":\"t\"}}";

        mockMvc.perform(post("/users/v2/_create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()", is(0)));
    }
}
