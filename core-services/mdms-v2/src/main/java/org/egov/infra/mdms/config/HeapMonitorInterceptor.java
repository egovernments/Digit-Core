package org.egov.infra.mdms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Logs heap usage before and after each request so heap pressure from large result sets
 * is visible in logs without any APM tooling. Enable via logging.level for this class.
 * Overhead is negligible — MemoryMXBean.getHeapMemoryUsage() is a direct JVM call.
 */
@Slf4j
public class HeapMonitorInterceptor implements HandlerInterceptor {

    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    private static final long MB = 1024 * 1024;

    private static final ThreadLocal<Long> heapBefore = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (log.isDebugEnabled()) {
            heapBefore.set(MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed() / MB);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (log.isDebugEnabled()) {
            Long before = heapBefore.get();
            if (before != null) {
                long after = MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed() / MB;
                long delta = after - before;
                long max = MEMORY_MX_BEAN.getHeapMemoryUsage().getMax() / MB;
                // Only log when delta is significant (>10MB) or heap usage is high (>70%)
                if (delta > 10 || (after * 100 / max) > 70) {
                    log.debug("HEAP uri={} method={} before={}MB after={}MB delta={}MB usedOfMax={}%",
                            request.getRequestURI(), request.getMethod(),
                            before, after, delta, (after * 100 / max));
                }
                heapBefore.remove();
            }
        }
    }
}
