package digit.web.controllers;

import digit.web.models.ErrorRes;
import digit.web.models.RequestInfo;
import digit.web.models.SubTenantRequest;
import digit.web.models.SubTenantResponse;
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
* API tests for SubTenantApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(SubTenantApiController.class)
@Import(TestConfiguration.class)
public class SubTenantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void subTenantCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/subTenant/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void subTenantCreatePostFailure() throws Exception {
        mockMvc.perform(post("/api/subTenant/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void subTenantSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/api/subTenant/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void subTenantSearchPostFailure() throws Exception {
        mockMvc.perform(post("/api/subTenant/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void subTenantUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/subTenant/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void subTenantUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/api/subTenant/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
