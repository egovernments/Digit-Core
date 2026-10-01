package org.egov.user.web.controller;

import org.egov.tracer.model.Error;
import org.egov.tracer.model.ErrorRes;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class DataAccessExceptionHandlerTest {

    @Test
    public void responseDoesNotLeakSql() {
        String sql = "SELECT data.tenantid FROM chaduat<script>alert(1)</script>.eg_user data";
        BadSqlGrammarException ex = new BadSqlGrammarException("PreparedStatementCallback", sql,
                new SQLException("ERROR: syntax error at or near \"<\" in " + sql));

        ResponseEntity<ErrorRes> response = new DataAccessExceptionHandler().handleDataAccessException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Error error = response.getBody().getErrors().get(0);
        assertEquals("QUERY_EXECUTION_ERROR", error.getCode());
        for (String text : new String[]{error.getMessage(), error.getDescription()}) {
            assertFalse(text.contains("SELECT"));
            assertFalse(text.contains("<script>"));
            assertFalse(text.contains("eg_user"));
        }
    }
}
