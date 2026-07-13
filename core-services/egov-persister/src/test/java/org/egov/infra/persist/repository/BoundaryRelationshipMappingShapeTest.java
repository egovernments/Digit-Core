package org.egov.infra.persist.repository;

import com.jayway.jsonpath.Configuration;
import org.egov.infra.persist.web.contract.JsonMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves the boundary-relationship "dedicated bulk topic" fix at the persister layer, using the REAL
 * {@link PersistRepository#getRows} extraction (documents parsed exactly as
 * {@code PersistService} does: {@code Configuration.defaultConfiguration().jsonProvider().parse(json)}).
 *
 * <p>Two message shapes exist on two different topics:
 * <ul>
 *   <li>single-create publishes {@code {"BoundaryRelationship": {..one object..}}} to
 *       {@code save-boundary-relationship} — mapped with base path {@code $.BoundaryRelationship} (no wildcard).</li>
 *   <li>bulk-create publishes {@code {"BoundaryRelationship": [..array..]}} to
 *       {@code boundary-relationship-bulk-create-job} — mapped with base path {@code $.BoundaryRelationship.*}.</li>
 * </ul>
 * The positive tests prove each shape+mapping pairing extracts the correct number of rows with correct
 * field values. The negative tests prove WHY the topics must stay separate: crossing a shape with the
 * wrong base path fails (the original single-topic blocker).</p>
 */
class BoundaryRelationshipMappingShapeTest {

    // Column order of the INSERT in boundary-persister.yml
    private static final String[] FIELDS = {
            "id", "tenantId", "code", "hierarchyType", "boundaryType", "parent",
            "ancestralMaterializedPath",
            "auditDetails.createdTime", "auditDetails.createdBy",
            "auditDetails.lastModifiedTime", "auditDetails.lastModifiedBy"
    };

    private List<JsonMap> jsonMaps(String prefix) {
        List<JsonMap> maps = new ArrayList<>();
        for (String f : FIELDS) {
            JsonMap m = new JsonMap();
            m.setJsonPath(prefix + f);   // e.g. "$.BoundaryRelationship." + "code"  or "$.BoundaryRelationship.*." + "code"
            maps.add(m);
        }
        return maps;
    }

    private String relJson(String id, String code, String parent, String amp) {
        return "{"
                + "\"id\":\"" + id + "\","
                + "\"tenantId\":\"mz\","
                + "\"code\":\"" + code + "\","
                + "\"hierarchyType\":\"ADMIN\","
                + "\"boundaryType\":\"Village\","
                + "\"parent\":" + (parent == null ? "null" : "\"" + parent + "\"") + ","
                + "\"ancestralMaterializedPath\":\"" + amp + "\","
                + "\"auditDetails\":{\"createdTime\":100,\"createdBy\":\"u1\",\"lastModifiedTime\":200,\"lastModifiedBy\":\"u2\"}"
                + "}";
    }

    private Object parse(String json) {
        // identical to PersistService.persist(...)
        return Configuration.defaultConfiguration().jsonProvider().parse(json);
    }

    // -------------------- POSITIVE: correct shape + correct mapping --------------------

    @Test
    void singleObject_onSingleMapping_extractsExactlyOneRowWithCorrectValues() {
        String json = "{\"RequestInfo\":{\"ver\":\"1.0\"},\"BoundaryRelationship\":"
                + relJson("id-C1", "C1", null, "C1") + "}";

        List<Object[]> rows = new PersistRepository()
                .getRows(jsonMaps("$.BoundaryRelationship."), parse(json), "$.BoundaryRelationship");

        assertEquals(1, rows.size(), "single-object message must yield exactly ONE row");
        Object[] r = rows.get(0);
        assertEquals(11, r.length, "row must have all 11 columns");
        assertEquals("id-C1", r[0]);
        assertEquals("mz", r[1]);
        assertEquals("C1", r[2]);
        assertEquals("ADMIN", r[3]);
        assertEquals("Village", r[4]);
        assertEquals(null, r[5], "root parent is null");
        assertEquals("C1", r[6]);
        assertEquals("u1", r[8]);
        assertEquals("u2", r[10]);
    }

    @Test
    void array_onBulkMapping_extractsOneRowPerElementWithCorrectValues() {
        StringBuilder arr = new StringBuilder();
        int n = 5;
        for (int i = 0; i < n; i++) {
            if (i > 0) arr.append(",");
            arr.append(relJson("id-V" + i, "V" + i, "P1", "R1|P1|V" + i));
        }
        String json = "{\"RequestInfo\":{\"ver\":\"1.0\"},\"BoundaryRelationship\":[" + arr + "]}";

        List<Object[]> rows = new PersistRepository()
                .getRows(jsonMaps("$.BoundaryRelationship.*."), parse(json), "$.BoundaryRelationship.*");

        assertEquals(n, rows.size(), "array message must yield ONE row per element");
        // first element
        assertEquals("id-V0", rows.get(0)[0]);
        assertEquals("V0", rows.get(0)[2]);
        assertEquals("P1", rows.get(0)[5]);
        assertEquals("R1|P1|V0", rows.get(0)[6]);
        // last element
        assertEquals("id-V4", rows.get(4)[0]);
        assertEquals("V4", rows.get(4)[2]);
        assertEquals("R1|P1|V4", rows.get(4)[6]);
        // every row fully populated
        for (Object[] r : rows) {
            assertEquals(11, r.length);
            assertEquals("mz", r[1]);
            assertEquals("ADMIN", r[3]);
        }
    }

    // -------------------- NEGATIVE: why the topics must stay separate (the original blocker) --------------------

    @Test
    void singleObject_onBulkMapping_breaks() {
        // This is exactly what happened when both shapes shared save-boundary-relationship with a .* mapping.
        String json = "{\"RequestInfo\":{\"ver\":\"1.0\"},\"BoundaryRelationship\":"
                + relJson("id-C1", "C1", null, "C1") + "}";
        assertThrows(Exception.class, () -> new PersistRepository()
                        .getRows(jsonMaps("$.BoundaryRelationship.*."), parse(json), "$.BoundaryRelationship.*"),
                "a single OBJECT under a .* (array) base path must fail — this is why bulk needs its own topic");
    }

    @Test
    void array_onSingleMapping_breaks() {
        String json = "{\"RequestInfo\":{\"ver\":\"1.0\"},\"BoundaryRelationship\":["
                + relJson("id-V0", "V0", "P1", "R1|P1|V0") + "]}";
        assertThrows(Exception.class, () -> new PersistRepository()
                        .getRows(jsonMaps("$.BoundaryRelationship."), parse(json), "$.BoundaryRelationship"),
                "an ARRAY under a single (non-wildcard) base path must fail — confirms single-create keeps its object topic");
    }
}
