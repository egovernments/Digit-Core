package digit.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.Boundary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoundaryEntityRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private BoundaryEntityRowMapper rowMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldMapAdditionalDetailsAndGeometryAsNull_whenBothAreNullInDb() throws SQLException, JsonProcessingException {
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getString("id")).thenReturn("b1");
        when(resultSet.getString("code")).thenReturn("BND1");
        when(resultSet.getString("geometry")).thenReturn(null);
        when(resultSet.getString("additionaldetails")).thenReturn(null);
        when(resultSet.getString("tenantid")).thenReturn("pg.citya");
        when(resultSet.getString("createdby")).thenReturn("system");
        when(resultSet.getLong("createdtime")).thenReturn(123456789L);
        when(resultSet.getString("lastmodifiedby")).thenReturn("system");
        when(resultSet.getLong("lastmodifiedtime")).thenReturn(123456789L);

        List<Boundary> result = rowMapper.extractData(resultSet);

        assertEquals(1, result.size());
        assertNull(result.get(0).getGeometry());
        assertNull(result.get(0).getAdditionalDetails());
        assertEquals("b1", result.get(0).getId());

        // Confirms readTree was never called on null input — mapper wasn't even touched
        verify(mapper, never()).readTree(anyString());
    }


    @Test
    void shouldParseAdditionalDetails_whenPresentInDb() throws Exception {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("id")).thenReturn("b2");
        when(resultSet.getString("code")).thenReturn("BND2");
        when(resultSet.getString("geometry")).thenReturn(null);
        when(resultSet.getString("additionaldetails")).thenReturn("{\"key\":\"value\"}");
        when(resultSet.getString("tenantid")).thenReturn("pg.citya");
        when(resultSet.getString("createdby")).thenReturn("system");
        when(resultSet.getLong("createdtime")).thenReturn(123456789L);
        when(resultSet.getString("lastmodifiedby")).thenReturn("system");
        when(resultSet.getLong("lastmodifiedtime")).thenReturn(123456789L);

        JsonNode fakeNode = new ObjectMapper().readTree("{\"key\":\"value\"}");
        when(mapper.readTree("{\"key\":\"value\"}")).thenReturn(fakeNode);

        List<Boundary> result = rowMapper.extractData(resultSet);

        assertNotNull(result.get(0).getAdditionalDetails());
        assertEquals("value", result.get(0).getAdditionalDetails().get("key").asText());
    }


    @Test
    void shouldParseGeometry_whenPresentInDb() throws Exception {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("id")).thenReturn("b3");
        when(resultSet.getString("code")).thenReturn("BND3");
        when(resultSet.getString("geometry")).thenReturn("{\"type\":\"Point\",\"coordinates\":[10,20]}");
        when(resultSet.getString("additionaldetails")).thenReturn(null);
        when(resultSet.getString("tenantid")).thenReturn("pg.citya");
        when(resultSet.getString("createdby")).thenReturn("system");
        when(resultSet.getLong("createdtime")).thenReturn(123456789L);
        when(resultSet.getString("lastmodifiedby")).thenReturn("system");
        when(resultSet.getLong("lastmodifiedtime")).thenReturn(123456789L);

        JsonNode fakeGeometryNode = new ObjectMapper().readTree("{\"type\":\"Point\",\"coordinates\":[10,20]}");
        when(mapper.readTree("{\"type\":\"Point\",\"coordinates\":[10,20]}")).thenReturn(fakeGeometryNode);

        List<Boundary> result = rowMapper.extractData(resultSet);

        assertNotNull(result.get(0).getGeometry());
        assertEquals("Point", result.get(0).getGeometry().get("type").asText());
    }

}
