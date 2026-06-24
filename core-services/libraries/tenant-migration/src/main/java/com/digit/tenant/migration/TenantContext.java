package com.digit.tenant.migration;

/**
 * ThreadLocal holder for the current request's tenant id.
 * Mirrors the Go context value {@code tenantIDKey} carried per-request.
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void set(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
