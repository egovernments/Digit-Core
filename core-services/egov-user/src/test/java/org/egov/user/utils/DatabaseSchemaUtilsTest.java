package org.egov.user.utils;

import org.egov.tracer.model.CustomException;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseSchemaUtilsTest {

    private static final String QUERY = "select * from {schema}.eg_user";

    private final DatabaseSchemaUtils central = new DatabaseSchemaUtils(1, true, 1);
    private final DatabaseSchemaUtils nonCentral = new DatabaseSchemaUtils(1, false, 1);

    @Test
    public void validTenantsResolveSchema() {
        assertEquals("select * from amritsar.eg_user", central.replaceSchemaPlaceholder(QUERY, "pb.amritsar"));
        assertEquals("select * from chaduat.eg_user", central.replaceSchemaPlaceholder(QUERY, "chaduat"));
        assertEquals("select * from mz_01.eg_user", central.replaceSchemaPlaceholder(QUERY, "mz_01"));
    }

    @Test
    public void maliciousTenantsRejectedWithoutEcho() {
        String[] payloads = {
                "chaduat<script>alert(1)</script>",
                "chaduatu1x24<script>alert(1)</script>den3w",
                "x;drop table eg_user",
                "a\"b",
                "pb.am ritsar",
                "pb.(select 1)x",
                "pb.a$1"
        };
        for (String payload : payloads) {
            try {
                central.replaceSchemaPlaceholder(QUERY, payload);
                fail("Expected rejection for " + payload);
            } catch (CustomException e) {
                assertEquals("INVALID_TENANT_ID", e.getCode());
                assertFalse(e.getMessage().contains(payload));
            }
        }
    }

    @Test
    public void nonCentralStripsPlaceholderAndIgnoresTenant() {
        assertEquals("select * from eg_user", nonCentral.replaceSchemaPlaceholder(QUERY, "x<script>"));
    }
}
