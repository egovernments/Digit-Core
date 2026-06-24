package com.digit.tenant.migration;

import java.io.IOException;

import javax.sql.DataSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-request tenant DB context filter.
 *
 * <p>Java port of the Go {@code tenantdb.GinMiddleware}. For each request it:
 * <ul>
 *   <li>skips paths ending in {@code /health} and the exact path {@code /internal/migrate};</li>
 *   <li>requires the tenant header (default {@code X-Tenant-ID}); if missing/blank responds 400 with
 *       JSON body {@code {"error":"<header> header is required"}};</li>
 *   <li>begins a DB transaction via the supplied {@link PlatformTransactionManager} (a
 *       {@code DataSourceTransactionManager} over the same DataSource the app's JdbcTemplate uses),
 *       so the connection is bound to the thread for the request;</li>
 *   <li>issues {@code SET LOCAL search_path TO "<schema>"} on that bound connection, using the
 *       tenant schema when schema separation is enabled, else {@code public};</li>
 *   <li>runs the request, then COMMITs if the final status &lt; 400 and no error was thrown, else
 *       ROLLBACKs.</li>
 * </ul>
 *
 * <p>{@link TenantContext} is set for the request and cleared in a finally block.
 */
public class TenantTransactionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantTransactionFilter.class);

    private static final String DEFAULT_TENANT_HEADER = "X-Tenant-ID";
    private static final String MIGRATE_PATH = "/internal/migrate";

    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager txManager;
    private final String tenantHeader;
    private final boolean schemaSeparationEnabled;

    public TenantTransactionFilter(DataSource dataSource,
                                   PlatformTransactionManager txManager,
                                   String tenantHeader,
                                   boolean schemaSeparationEnabled) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.txManager = txManager;
        this.tenantHeader = (tenantHeader == null || tenantHeader.isEmpty())
                ? DEFAULT_TENANT_HEADER : tenantHeader;
        this.schemaSeparationEnabled = schemaSeparationEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.endsWith("/health") || MIGRATE_PATH.equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenantId = request.getHeader(tenantHeader);
        if (tenantId == null || tenantId.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                    tenantHeader + " header is required");
            return;
        }

        String schemaName = schemaSeparationEnabled ? tenantId : "public";

        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        boolean failed = false;
        try {
            TenantContext.set(tenantId);

            // Runs on the transaction-bound connection via DataSourceUtils.
            jdbcTemplate.execute("SET LOCAL search_path TO " + Identifiers.quoteIdent(schemaName));

            filterChain.doFilter(request, response);

            if (response.getStatus() >= 400) {
                failed = true;
            }
        } catch (Exception e) {
            failed = true;
            completeTransaction(status, true);
            throw new ServletException(e);
        } finally {
            TenantContext.clear();
        }

        completeTransaction(status, failed);
    }

    private void completeTransaction(TransactionStatus status, boolean rollback) {
        if (status.isCompleted()) {
            return;
        }
        try {
            if (rollback) {
                txManager.rollback(status);
            } else {
                txManager.commit(status);
            }
        } catch (RuntimeException e) {
            log.error("failed to {} tenant transaction: {}",
                    rollback ? "rollback" : "commit", e.getMessage());
            throw e;
        }
    }

    private void writeJsonError(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"error\":\"" + escaped + "\"}");
    }
}
