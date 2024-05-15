package digit.web.controllers;

import digit.web.models.ErrorRes;
import digit.web.models.ProgramRequest;
import digit.web.models.ProgramResponse;
import digit.web.models.ProgramSearchRequest;
import digit.web.models.ProgramSearchResponse;
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
* API tests for ProgramApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(ProgramApiController.class)
@Import(TestConfiguration.class)
public class ProgramApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void programCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/program/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void programCreatePostFailure() throws Exception {
        mockMvc.perform(post("/program/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void programSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/program/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void programSearchPostFailure() throws Exception {
        mockMvc.perform(post("/program/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void programUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/program/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void programUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/program/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
