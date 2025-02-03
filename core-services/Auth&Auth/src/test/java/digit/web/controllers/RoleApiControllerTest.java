package digit.web.controllers;

import digit.web.models.Role;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import digit.TestConfiguration;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for RoleApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(RoleApiController.class)
@Import(TestConfiguration.class)
public class RoleApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void roleCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/role/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleCreatePostFailure() throws Exception {
        mockMvc.perform(post("/api/role/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void roleSearchGetSuccess() throws Exception {
        mockMvc.perform(post("/api/role/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleSearchGetFailure() throws Exception {
        mockMvc.perform(post("/api/role/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void roleUpdatePutSuccess() throws Exception {
        mockMvc.perform(post("/api/role/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleUpdatePutFailure() throws Exception {
        mockMvc.perform(post("/api/role/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
