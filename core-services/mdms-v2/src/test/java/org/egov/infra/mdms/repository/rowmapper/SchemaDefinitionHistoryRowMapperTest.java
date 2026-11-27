package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.infra.mdms.model.HistoryOperation;
import org.egov.infra.mdms.model.SchemaDefinitionHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchemaDefinitionHistoryRowMapperTest {

    private SchemaDefinitionHistoryRowMapper rowMapper;

    @BeforeEach
    void setUp() {
        rowMapper = new SchemaDefinitionHistoryRowMapper();
        ReflectionTestUtils.setField(rowMapper, "objectMapper", new ObjectMapper());
    }

    @Test
    void rowIsMappedToSchemaAndChangeDetails() throws Exception {
        PGobject definition = new PGobject();
        definition.setType("jsonb");
        definition.setValue("{\"type\":\"object\"}");

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("historyid")).thenReturn(11L);
        when(rs.getInt("version")).thenReturn(1);
        when(rs.getString("operation")).thenReturn("MIGRATION");
        when(rs.getString("id")).thenReturn("s1");
        when(rs.getString("tenantid")).thenReturn("pg");
        when(rs.getString("code")).thenReturn("common.Dept");
        when(rs.getString("description")).thenReturn("Departments");
        when(rs.getObject("definition")).thenReturn(definition);
        when(rs.getBoolean("isactive")).thenReturn(true);

        List<SchemaDefinitionHistory> result = rowMapper.extractData(rs);

        assertEquals(1, result.size());
        SchemaDefinitionHistory history = result.get(0);
        assertEquals(HistoryOperation.MIGRATION, history.getChangeDetails().getOperation());
        assertEquals(11L, history.getChangeDetails().getHistoryId());
        assertEquals("common.Dept", history.getSchemaDefinition().getCode());
        assertEquals("object", history.getSchemaDefinition().getDefinition().get("type").asText());
    }
}
