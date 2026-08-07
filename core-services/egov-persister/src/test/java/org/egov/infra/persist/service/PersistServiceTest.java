package org.egov.infra.persist.service;

import com.github.zafarkhaja.semver.Version;
import com.jayway.jsonpath.PathNotFoundException;
import org.egov.infra.persist.repository.PersistRepository;
import org.egov.infra.persist.utils.Utils;
import org.egov.infra.persist.web.contract.TopicMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PersistService.class})
@ExtendWith(SpringExtension.class)
class PersistServiceTest {
    @MockBean
    private PersistRepository persistRepository;

    @Autowired
    private PersistService persistService;

    @MockBean
    private TopicMap topicMap;

    @MockBean
    private Utils utils;

    @Test
    void testPersist() throws Exception  {
        when(this.utils.getSemVer((String) any())).thenReturn(Version.forIntegers(1));
        when(this.topicMap.getTopicMap()).thenReturn(new HashMap<>());
    }


    @Test
    void testPersistPathNotFound() {
        when(this.utils.getSemVer((String) any())).thenReturn(Version.forIntegers(1));
        when(this.topicMap.getTopicMap()).thenThrow(new PathNotFoundException("An error occurred"));
        assertThrows(PathNotFoundException.class, () -> this.persistService.persist("Topic", "Json"));
        verify(this.topicMap).getTopicMap();
    }

    @Test
    void testPersistWithTopicOnly() {
        when(this.topicMap.getTopicMap()).thenReturn(new HashMap<>());
        this.persistService.persist("Topic", new ArrayList<>());
        verify(this.topicMap).getTopicMap();
    }


    @Test
    void testPersistError() {
        when(this.utils.getSemVer((String) any())).thenThrow(new PathNotFoundException("An error occurred"));
        when(this.topicMap.getTopicMap()).thenReturn(new HashMap<>());

        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("foo");
        assertThrows(PathNotFoundException.class, () -> this.persistService.persist("Topic", stringList));
        verify(this.utils).getSemVer((String) any());
        verify(this.topicMap).getTopicMap();
    }

    @Test
    void batchExtractionFailureIsPropagatedForRecordIsolation() {
        org.egov.infra.persist.web.contract.Mapping mapping = mock(org.egov.infra.persist.web.contract.Mapping.class);
        org.egov.infra.persist.web.contract.QueryMap queryMap = mock(org.egov.infra.persist.web.contract.QueryMap.class);
        java.util.List<org.egov.infra.persist.web.contract.JsonMap> jsonMaps = java.util.List.of();

        when(topicMap.getTopicMap()).thenReturn(java.util.Map.of("topic", java.util.List.of(mapping)));
        when(utils.getSemVer(anyString())).thenReturn(Version.forIntegers(1));
        when(mapping.getName()).thenReturn("mapping");
        when(mapping.getVersion()).thenReturn(">=1.0.0");
        when(mapping.getQueryMaps()).thenReturn(java.util.List.of(queryMap));
        when(queryMap.getQuery()).thenReturn("INSERT INTO example(id) VALUES (?)");
        when(queryMap.getBasePath()).thenReturn("$.items.*");
        when(queryMap.getJsonMaps()).thenReturn(jsonMaps);
        doThrow(new IllegalArgumentException("bad extraction"))
                .when(persistRepository).getRows(eq(jsonMaps), any(), eq("$.items.*"));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> persistService.persist("topic", java.util.List.of("{}")));

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("$.items.*"));
        verify(persistRepository, never())
                .persist(eq("INSERT INTO example(id) VALUES (?)"), anyList());
    }
}

