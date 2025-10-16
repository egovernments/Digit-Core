package digit.web.controllers;

import digit.web.models.BoundaryRelationshipRequest;
import digit.web.models.BoundaryRelationshipResponse;
import digit.web.models.BoundaryRequest;
import digit.web.models.BoundaryResponse;
import digit.web.models.BoundarySearchResponse;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchyResponse;
import digit.web.models.BoundaryTypeHierarchySearchRequest;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* API tests for BoundaryApiController
*/
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(BoundaryApiController.class)
@Import(TestConfiguration.class)
public class BoundaryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void boundaryBoundaryRelationshipsCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryBoundaryRelationshipsCreatePostFailure() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryBoundaryRelationshipsSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryBoundaryRelationshipsSearchPostFailure() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryBoundaryRelationshipsUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryBoundaryRelationshipsUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/boundary/boundary-relationships/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryCreatePostFailure() throws Exception {
        mockMvc.perform(post("/boundary/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryHierarchyDefinitionCreatePostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/hierarchy-definition/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryHierarchyDefinitionCreatePostFailure() throws Exception {
        mockMvc.perform(post("/boundary/hierarchy-definition/_create").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryHierarchyDefinitionSearchPostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/hierarchy-definition/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryHierarchyDefinitionSearchPostFailure() throws Exception {
        mockMvc.perform(post("/boundary/hierarchy-definition/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundarySearchPostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundarySearchPostFailure() throws Exception {
        mockMvc.perform(post("/boundary/_search").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void boundaryUpdatePostSuccess() throws Exception {
        mockMvc.perform(post("/boundary/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    }

    @Test
    public void boundaryUpdatePostFailure() throws Exception {
        mockMvc.perform(post("/boundary/_update").contentType(MediaType
        .APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    }

}
