package org.egov.wf.web.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

import org.egov.common.contract.request.User;
import org.junit.jupiter.api.Test;

class ProcessInstanceTest {

    /**
     * Mimics Spring Boot's default ObjectMapper, which has
     * FAIL_ON_UNKNOWN_PROPERTIES disabled. Before the @JsonAlias fix this
     * caused the correctly spelled "assignees" key to be dropped silently,
     * so the transition was accepted with 200 but no eg_wf_assignee_v2 rows
     * were ever persisted.
     */
    private ObjectMapper springBootLikeMapper() {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Test
    void deserializesMisspelledContractKeyAssignes() throws Exception {
        ProcessInstance processInstance = springBootLikeMapper()
                .readValue("{\"assignes\":[{\"uuid\":\"u1\"}]}", ProcessInstance.class);

        assertNotNull(processInstance.getAssignes());
        assertEquals(1, processInstance.getAssignes().size());
        assertEquals("u1", processInstance.getAssignes().get(0).getUuid());
    }

    @Test
    void deserializesCorrectlySpelledAliasAssignees() throws Exception {
        ProcessInstance processInstance = springBootLikeMapper()
                .readValue("{\"assignees\":[{\"uuid\":\"u1\"}]}", ProcessInstance.class);

        assertNotNull(processInstance.getAssignes());
        assertEquals(1, processInstance.getAssignes().size());
        assertEquals("u1", processInstance.getAssignes().get(0).getUuid());
    }

    @Test
    void deserializesAliasInsideTransitionRequestPayload() throws Exception {
        String payload = "{\"ProcessInstances\":[{\"tenantId\":\"pg.citya\","
                + "\"businessService\":\"PGR\",\"businessId\":\"PG-PGR-2026-000001\","
                + "\"action\":\"ASSIGN\",\"moduleName\":\"RAINMAKER-PGR\","
                + "\"assignees\":[{\"uuid\":\"u1\"}]}]}";

        ProcessInstanceRequest request = springBootLikeMapper()
                .readValue(payload, ProcessInstanceRequest.class);

        ProcessInstance processInstance = request.getProcessInstances().get(0);
        assertNotNull(processInstance.getAssignes());
        assertEquals("u1", processInstance.getAssignes().get(0).getUuid());
    }

    @Test
    void serializationStillEmitsAssignesForPersisterJsonPaths() throws Exception {
        ProcessInstance processInstance = new ProcessInstance();
        User user = new User();
        user.setUuid("u1");
        processInstance.setAssignes(Collections.singletonList(user));

        String json = springBootLikeMapper().writeValueAsString(processInstance);

        // The persister yml extracts ProcessInstances.*.assignes.* — the
        // serialized key must remain "assignes" for existing consumers.
        assertTrue(json.contains("\"assignes\""));
        assertFalse(json.contains("\"assignees\""));
    }
}
