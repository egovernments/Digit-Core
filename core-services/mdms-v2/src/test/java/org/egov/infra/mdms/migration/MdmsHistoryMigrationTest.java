package org.egov.infra.mdms.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class MdmsHistoryMigrationTest {

    private static final String SCHEMA_A = "tenant_a";
    private static final String SCHEMA_B = "tenant_b";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14");

    @BeforeAll
    static void migrate() throws SQLException {
        try (Connection connection = ownerConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + SCHEMA_A);
            statement.execute("CREATE SCHEMA " + SCHEMA_B);
        }
        // Existing row in tenant_a before history is enabled -> must be recorded as MIGRATION
        migrateSchema(SCHEMA_A, "20230531144020");
        execute(SCHEMA_A, "INSERT INTO eg_mdms_data VALUES ('base','pg','BASE','S','{\"v\":0}',true,'u','u',1,1)");

        migrateSchema(SCHEMA_A, null);
        migrateSchema(SCHEMA_B, null);
    }

    @Test
    void existingRowsAreRecordedAsMigration() throws SQLException {
        assertEquals(List.of("1:MIGRATION:{\"v\": 0}"), dataHistory(SCHEMA_A, "BASE"));
    }

    @Test
    void insertUpdateDeleteAreRecordedAndNoOpUpdateIsSkipped() throws SQLException {
        execute(SCHEMA_A, "INSERT INTO eg_mdms_data VALUES ('r1','pg','IUD','S','{\"v\":1}',true,'u','u',1,1)");
        execute(SCHEMA_A, "UPDATE eg_mdms_data SET data = '{\"v\":2}' WHERE id = 'r1'");
        execute(SCHEMA_A, "UPDATE eg_mdms_data SET data = '{\"v\":2}' WHERE id = 'r1'");
        execute(SCHEMA_A, "DELETE FROM eg_mdms_data WHERE id = 'r1'");
        execute(SCHEMA_A, "INSERT INTO eg_mdms_data VALUES ('r1b','pg','IUD','S','{\"v\":3}',true,'u','u',1,1)");

        assertEquals(List.of("1:INSERT:{\"v\": 1}", "2:UPDATE:{\"v\": 2}", "3:DELETE:{\"v\": 2}", "4:INSERT:{\"v\": 3}"), dataHistory(SCHEMA_A, "IUD"));
    }

    @Test
    void keyChangeIsRecordedAsDeleteAndInsert() throws SQLException {
        execute(SCHEMA_A, "INSERT INTO eg_mdms_data VALUES ('k1','pg','OLDKEY','S','{}',true,'u','u',1,1)");
        execute(SCHEMA_A, "UPDATE eg_mdms_data SET uniqueidentifier = 'NEWKEY' WHERE id = 'k1'");

        assertEquals(List.of("1:INSERT:{}", "2:DELETE:{}"), dataHistory(SCHEMA_A, "OLDKEY"));
        assertEquals(List.of("1:INSERT:{}"), dataHistory(SCHEMA_A, "NEWKEY"));
    }

    @Test
    void schemaChangesAreRecorded() throws SQLException {
        execute(SCHEMA_A, "INSERT INTO eg_mdms_schema_definition VALUES ('s1','pg','common.Dept','d','{}',true,'u','u',1,1)");
        execute(SCHEMA_A, "UPDATE eg_mdms_schema_definition SET definition = '{\"type\":\"object\"}' WHERE code = 'common.Dept'");

        assertEquals(List.of("1:INSERT", "2:UPDATE"), query(SCHEMA_A,
                "SELECT version || ':' || operation FROM eg_mdms_schema_definition_history WHERE code = 'common.Dept' ORDER BY version"));
    }

    @Test
    void truncateIsRecordedAndHistoryIsPerSchema() throws SQLException {
        execute(SCHEMA_B, "INSERT INTO eg_mdms_data VALUES ('t1','pg','TRUNC','S','{}',true,'u','u',1,1)");
        execute(SCHEMA_B, "TRUNCATE eg_mdms_data");

        assertEquals(List.of("1:INSERT:{}", "2:TRUNCATE:{}"), dataHistory(SCHEMA_B, "TRUNC"));
        assertEquals(List.of(), dataHistory(SCHEMA_A, "TRUNC"));
    }

    @Test
    void historyTablesAreAppendOnly() {
        for (String sql : new String[]{
                "UPDATE eg_mdms_data_history SET operation = 'UPDATE'",
                "DELETE FROM eg_mdms_data_history",
                "TRUNCATE eg_mdms_data_history",
                "INSERT INTO eg_mdms_data_history (version, operation, tenantid, uniqueidentifier, schemacode, changedtime) VALUES (99, 'UPDATE', 'pg', 'X', 'S', 0)"}) {
            PSQLException e = assertThrows(PSQLException.class, () -> execute(SCHEMA_A, sql), sql);
            assertEquals("42501", e.getSQLState(), sql);
        }
    }

    @Test
    void temporaryTableCannotCaptureHistory() throws SQLException {
        try (Connection connection = ownerConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SET search_path = " + SCHEMA_A);
            statement.execute("CREATE TEMP TABLE eg_mdms_data_history (LIKE " + SCHEMA_A + ".eg_mdms_data_history INCLUDING DEFAULTS)");
            statement.execute("INSERT INTO eg_mdms_data VALUES ('tmp','pg','TEMP','S','{}',true,'u','u',1,1)");

            try (ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM pg_temp.eg_mdms_data_history")) {
                resultSet.next();
                assertEquals(0, resultSet.getInt(1));
            }
        }
        assertEquals(List.of("1:INSERT:{}"), dataHistory(SCHEMA_A, "TEMP"));
    }

    private static void migrateSchema(String schema, String target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl() + "&currentSchema=" + schema, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/main")
                .table("flyway_" + schema)
                .baselineOnMigrate(true)
                .outOfOrder(true);
        if (target != null)
            configuration.target(target);
        configuration.load().migrate();
    }

    private static Connection ownerConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static void execute(String schema, String sql) throws SQLException {
        try (Connection connection = ownerConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SET search_path = " + schema);
            statement.execute(sql);
        }
    }

    private static List<String> query(String schema, String sql) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Connection connection = ownerConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SET search_path = " + schema);
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next())
                    rows.add(resultSet.getString(1));
            }
        }
        return rows;
    }

    private static List<String> dataHistory(String schema, String uniqueIdentifier) throws SQLException {
        return query(schema, "SELECT version || ':' || operation || ':' || data FROM eg_mdms_data_history WHERE uniqueidentifier = '"
                + uniqueIdentifier + "' ORDER BY version");
    }
}
