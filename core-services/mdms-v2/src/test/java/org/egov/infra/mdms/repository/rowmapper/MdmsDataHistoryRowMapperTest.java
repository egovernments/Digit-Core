package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.infra.mdms.model.HistoryOperation;
import org.egov.infra.mdms.model.MdmsHistory;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MdmsDataHistoryRowMapperTest {

    private MdmsDataHistoryRowMapper rowMapper;

    @BeforeEach
    void setUp() {
        rowMapper = new MdmsDataHistoryRowMapper();
        ReflectionTestUtils.setField(rowMapper, "objectMapper", new ObjectMapper());
    }

    private ResultSet mockRow(Object data) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("historyid")).thenReturn(7L);
        when(rs.getInt("version")).thenReturn(3);
        when(rs.getString("operation")).thenReturn("UPDATE");
        when(rs.getLong("changedtime")).thenReturn(1000L);
        when(rs.getString("dbuser")).thenReturn("egov");
        when(rs.getString("clientaddress")).thenReturn("10.0.0.1");
        when(rs.getString("applicationname")).thenReturn("seed-23");
        when(rs.getLong("transactionid")).thenReturn(99L);
        when(rs.getString("id")).thenReturn("id1");
        when(rs.getString("tenantid")).thenReturn("pg");
        when(rs.getString("schemacode")).thenReturn("common.Dept");
        when(rs.getString("uniqueidentifier")).thenReturn("D1");
        when(rs.getBoolean("isactive")).thenReturn(true);
        when(rs.getString("createdby")).thenReturn("u1");
        when(rs.getString("lastmodifiedby")).thenReturn("u2");
        when(rs.getLong("createdtime")).thenReturn(1L);
        when(rs.getLong("lastmodifiedtime")).thenReturn(2L);
        when(rs.getObject("data")).thenReturn(data);
        return rs;
    }

    @Test
    void rowIsMappedToRecordAndChangeDetails() throws Exception {
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue("{\"code\":\"D1\"}");

        List<MdmsHistory> result = rowMapper.extractData(mockRow(json));

        assertEquals(1, result.size());
        MdmsHistory history = result.get(0);
        assertEquals(7L, history.getChangeDetails().getHistoryId());
        assertEquals(3, history.getChangeDetails().getVersion());
        assertEquals(HistoryOperation.UPDATE, history.getChangeDetails().getOperation());
        assertEquals("seed-23", history.getChangeDetails().getApplicationName());
        assertEquals("D1", history.getMdms().getData().get("code").asText());
        assertEquals("common.Dept", history.getMdms().getSchemaCode());
        assertEquals("u2", history.getMdms().getAuditDetails().getLastModifiedBy());
    }

    @Test
    void nullDataIsMappedToNull() throws Exception {
        List<MdmsHistory> result = rowMapper.extractData(mockRow(null));
        assertNull(result.get(0).getMdms().getData());
    }

    @Test
    void invalidJsonRaisesCustomException() throws Exception {
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue("{not-json");
        ResultSet rs = mockRow(json);
        assertThrows(CustomException.class, () -> rowMapper.extractData(rs));
    }
}
