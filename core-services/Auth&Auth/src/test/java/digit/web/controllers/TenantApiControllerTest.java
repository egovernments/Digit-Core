package digit.web.controllers;

import digit.web.models.Tenant;
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
* API tests for TenantApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(TenantApiController.class)
@Import(TestConfiguration.class)
public class TenantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void tenantCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/api/tenant/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void tenantCreatePostFailure() throws Exception {
        mockMvc.perform(post("/api/tenant/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void tenantSearchGetSuccess() throws Exception {
        mockMvc.perform(post("/api/tenant/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void tenantSearchGetFailure() throws Exception {
        mockMvc.perform(post("/api/tenant/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void tenantUpdatePutSuccess() throws Exception {
        mockMvc.perform(post("/api/tenant/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void tenantUpdatePutFailure() throws Exception {
        mockMvc.perform(post("/api/tenant/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
