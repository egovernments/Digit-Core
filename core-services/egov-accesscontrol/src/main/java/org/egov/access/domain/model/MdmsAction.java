package org.egov.access.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An action projected from MDMS with optional, opaque policy metadata.
 *
 * <p>This type is intentionally limited to the {@code /v1/actions/mdms/_get} response path. The
 * shared {@link Action} model remains the unchanged request and persistence contract for the
 * database-backed action endpoints.
 */
public class MdmsAction extends Action {

    private String method;
    private Object resource;
    private Object condition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getResource() {
        return resource;
    }

    public void setResource(Object resource) {
        this.resource = resource;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getCondition() {
        return condition;
    }

    public void setCondition(Object condition) {
        this.condition = condition;
    }
}
