package org.egov.requestvalidation.web;

import java.util.Map;

/**
 * The servlet request operations the library uses, independent of javax.servlet / jakarta.servlet.
 * Implementations are thin views of the current HttpServletRequest (see {@link ServletSupport}).
 */
public interface ValidationRequest {
    String getMethod();
    String getContentType();
    Map<String, String[]> getParameterMap();
    Object getAttribute(String name);
    void setAttribute(String name, Object value);
}
