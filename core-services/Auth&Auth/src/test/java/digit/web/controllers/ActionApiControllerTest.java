package digit.web.controllers;

import digit.web.models.Action;
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
* API tests for ActionApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(ActionApiController.class)
@Import(TestConfiguration.class)
public class ActionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void actionCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/action/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void actionCreatePostFailure() throws Exception {
        mockMvc.perform(post("/api/action/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void actionSearchGetSuccess() throws Exception {
        mockMvc.perform(post("/api/action/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void actionSearchGetFailure() throws Exception {
        mockMvc.perform(post("/api/action/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void actionUpdatePutSuccess() throws Exception {
        mockMvc.perform(post("/api/action/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void actionUpdatePutFailure() throws Exception {
        mockMvc.perform(post("/api/action/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
