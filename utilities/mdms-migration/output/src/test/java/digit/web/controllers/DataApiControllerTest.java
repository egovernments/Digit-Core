package digit.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import digit.TestConfiguration;

/**
* API tests for DataApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(DataApiController.class)
@Import(TestConfiguration.class)
public class DataApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void migrateMasterDataSuccess() throws Exception {
        mockMvc.perform(post("/data/v1/_migrate").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void migrateMasterDataFailure() throws Exception {
        mockMvc.perform(post("/data/v1/_migrate").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
