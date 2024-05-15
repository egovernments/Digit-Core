package digit.web.controllers;

import digit.web.models.DisburseRequest;
import digit.web.models.DisburseResponse;
import digit.web.models.DisburseSearchRequest;
import digit.web.models.DisburseSearchResponse;
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
* API tests for DisburseApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(DisburseApiController.class)
@Import(TestConfiguration.class)
public class DisburseApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void disburseCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/disburse/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void disburseCreatePostFailure() throws Exception {
        mockMvc.perform(post("/disburse/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void disburseSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/disburse/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void disburseSearchPostFailure() throws Exception {
        mockMvc.perform(post("/disburse/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void disburseUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/disburse/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void disburseUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/disburse/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
