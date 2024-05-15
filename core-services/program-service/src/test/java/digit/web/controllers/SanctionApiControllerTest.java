package digit.web.controllers;

import digit.web.models.ErrorRes;
import digit.web.models.SanctionRequest;
import digit.web.models.SanctionResponse;
import digit.web.models.SanctionSearchRequest;
import digit.web.models.SanctionSearchResponse;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for SanctionApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(SanctionApiController.class)
@Import(TestConfiguration.class)
public class SanctionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void sanctionCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/sanction/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void sanctionCreatePostFailure() throws Exception {
        mockMvc.perform(post("/sanction/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void sanctionSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/sanction/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void sanctionSearchPostFailure() throws Exception {
        mockMvc.perform(post("/sanction/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void sanctionUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/sanction/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void sanctionUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/sanction/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
