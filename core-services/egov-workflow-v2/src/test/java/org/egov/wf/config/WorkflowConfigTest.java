package org.egov.wf.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

class WorkflowConfigTest {

    @Test
    void testInitialize() {

        WorkflowConfig workflowConfig = new WorkflowConfig("UTC", 1, 1, 3, "Save Transition Topic",
                "Save Business Service Topic", "2020-03-01", "localhost", "https://config.us-east-2.amazonaws.com",
                "localhost", "https://config.us-east-2.amazonaws.com", true, "MD", 3, 3, true);
        workflowConfig.initialize();
        assertTrue(workflowConfig.getAssignedOnly());
        assertEquals("https://config.us-east-2.amazonaws.com", workflowConfig.getUserSearchEndpoint());
        assertEquals("localhost", workflowConfig.getUserHost());
        assertEquals("2020-03-01", workflowConfig.getUpdateBusinessServiceTopic());
        assertEquals("UTC", workflowConfig.getTimeZone());
        assertEquals(3, workflowConfig.getStateLevelTenantIdLength().intValue());
        assertEquals("MD", workflowConfig.getStateLevelTenantId());
        assertEquals("Save Transition Topic", workflowConfig.getSaveTransitionTopic());
        assertEquals("Save Business Service Topic", workflowConfig.getSaveBusinessServiceTopic());
        assertEquals("localhost", workflowConfig.getMdmsHost());
        assertEquals("https://config.us-east-2.amazonaws.com", workflowConfig.getMdmsEndPoint());
        assertEquals(3, workflowConfig.getMaxSearchLimit().intValue());
        assertTrue(workflowConfig.getIsEnvironmentCentralInstance());
        assertEquals(3, workflowConfig.getEscalationBatchSize().intValue());
        assertEquals(1, workflowConfig.getDefaultOffset().intValue());
        assertEquals(1, workflowConfig.getDefaultLimit().intValue());
    }

    @Test
    void testJacksonConverter() {

        WorkflowConfig workflowConfig = new WorkflowConfig();
        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter actualJacksonConverterResult = workflowConfig.jacksonConverter(objectMapper);
        assertEquals(2, actualJacksonConverterResult.getSupportedMediaTypes().size());
        assertSame(objectMapper, actualJacksonConverterResult.getObjectMapper());
    }

    @Test
    void testJacksonConverter3() {

        assertEquals(2,
                (new WorkflowConfig()).jacksonConverter(mock(ObjectMapper.class)).getSupportedMediaTypes().size());
    }
}

