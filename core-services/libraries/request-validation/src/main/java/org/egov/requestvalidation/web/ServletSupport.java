package org.egov.requestvalidation.web;

import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The only code that touches the servlet API. Spring Boot 1.5/2.x use javax.servlet and Boot 3.x uses
 * jakarta.servlet, so the jar contains one implementation for each. {@link #forRuntime()} picks the one
 * matching the running Spring MVC; the other is never loaded.
 */
public interface ServletSupport {
    String JAVAX = "org.egov.requestvalidation.web.JavaxServletSupport";
    String JAKARTA = "org.egov.requestvalidation.web.JakartaServletSupport";

    /** A Spring MVC interceptor that runs the scalar checks for each request. */
    HandlerInterceptor scalarInterceptor(ScalarParameterInterceptor checks);

    /** The request bound to the current thread by Spring MVC. */
    ValidationRequest currentRequest();

    static ServletSupport forRuntime() {
        String implementation = usesJakartaServlet() ? JAKARTA : JAVAX;
        try {
            return (ServletSupport) Class.forName(implementation, true, ServletSupport.class.getClassLoader())
                    .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError ex) {
            throw new IllegalStateException("Request validation cannot load " + implementation, ex);
        }
    }

    /** True when Spring MVC's DispatcherServlet is a jakarta.servlet servlet (Spring 6 / Boot 3). */
    static boolean usesJakartaServlet() {
        try {
            Class<?> servlet = Class.forName("jakarta.servlet.http.HttpServlet", false,
                    DispatcherServlet.class.getClassLoader());
            return servlet.isAssignableFrom(DispatcherServlet.class);
        } catch (ClassNotFoundException | LinkageError ex) {
            return false;
        }
    }
}
