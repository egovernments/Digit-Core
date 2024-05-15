package digit.web.controllers;

import digit.web.models.AllocationRequest;
import digit.web.models.AllocationResponse;
import digit.web.models.AllocationSearchRequest;
import digit.web.models.AllocationSearchResponse;
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
* API tests for AllocationApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(AllocationApiController.class)
@Import(TestConfiguration.class)
public class AllocationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void allocationCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/allocation/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void allocationCreatePostFailure() throws Exception {
        mockMvc.perform(post("/allocation/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void allocationSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/allocation/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void allocationSearchPostFailure() throws Exception {
        mockMvc.perform(post("/allocation/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void allocationUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/allocation/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void allocationUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/allocation/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
