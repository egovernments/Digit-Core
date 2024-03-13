package digit.web.controllers;

import digit.web.models.BirthApplicationSearchCriteria;
import digit.web.models.BirthRegistrationRequest;
import digit.web.models.BirthRegistrationResponse;
import digit.web.models.ErrorResponse;
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
* API tests for BirthApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(BirthApiController.class)
@Import(TestConfiguration.class)
public class BirthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void birthRegistrationV1CreatePostSuccess() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void birthRegistrationV1CreatePostFailure() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void birthRegistrationV1SearchPostSuccess() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void birthRegistrationV1SearchPostFailure() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void birthRegistrationV1UpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void birthRegistrationV1UpdatePostFailure() throws Exception {
        mockMvc.perform(post("/birth/registration/v1/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
