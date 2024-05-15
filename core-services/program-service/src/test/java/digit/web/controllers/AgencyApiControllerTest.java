package digit.web.controllers;

import digit.web.models.AgencyRequest;
import digit.web.models.AgencyResponse;
import digit.web.models.AgencySearchRequest;
import digit.web.models.AgencySearchResponse;
import digit.web.models.ErrorRes;
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
* API tests for AgencyApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(AgencyApiController.class)
@Import(TestConfiguration.class)
public class AgencyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void agencyCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/agency/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void agencyCreatePostFailure() throws Exception {
        mockMvc.perform(post("/agency/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void agencySearchPostSuccess() throws Exception {
        mockMvc.perform(post("/agency/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void agencySearchPostFailure() throws Exception {
        mockMvc.perform(post("/agency/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void agencyUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/agency/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void agencyUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/agency/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
