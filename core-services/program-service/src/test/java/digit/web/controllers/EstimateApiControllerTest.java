package digit.web.controllers;

import digit.web.models.ErrorRes;
import digit.web.models.EstimateRequest;
import digit.web.models.EstimateResponse;
import digit.web.models.EstimateSearchRequest;
import digit.web.models.EstimateSearchResponse;
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
* API tests for EstimateApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(EstimateApiController.class)
@Import(TestConfiguration.class)
public class EstimateApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void estimateCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/estimate/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void estimateCreatePostFailure() throws Exception {
        mockMvc.perform(post("/estimate/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void estimateSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/estimate/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void estimateSearchPostFailure() throws Exception {
        mockMvc.perform(post("/estimate/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void estimateUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/estimate/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void estimateUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/estimate/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
