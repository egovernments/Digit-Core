package digit.web.controllers;

import digit.web.models.RoleAction;
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
* API tests for RoleActionApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(RoleActionApiController.class)
@Import(TestConfiguration.class)
public class RoleActionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void roleActionCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/roleAction/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleActionCreatePostFailure() throws Exception {
        mockMvc.perform(post("/api/roleAction/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void roleActionSearchGetSuccess() throws Exception {
        mockMvc.perform(post("/api/roleAction/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleActionSearchGetFailure() throws Exception {
        mockMvc.perform(post("/api/roleAction/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void roleActionUpdatePutSuccess() throws Exception {
        mockMvc.perform(post("/api/roleAction/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void roleActionUpdatePutFailure() throws Exception {
        mockMvc.perform(post("/api/roleAction/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
