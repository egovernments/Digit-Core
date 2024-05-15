package digit.web.controllers;

import digit.web.models.ErrorRes;
import digit.web.models.ProjectRequest;
import digit.web.models.ProjectResponse;
import digit.web.models.ProjectSearchRequest;
import digit.web.models.ProjectSearchResponse;
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
* API tests for ProjectApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(ProjectApiController.class)
@Import(TestConfiguration.class)
public class ProjectApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void projectCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/project/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void projectCreatePostFailure() throws Exception {
        mockMvc.perform(post("/project/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void projectSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/project/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void projectSearchPostFailure() throws Exception {
        mockMvc.perform(post("/project/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void projectUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/project/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void projectUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/project/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
