package org.egov.access.persistence.repository.querybuilder;

public interface BaseQueryBuilder {
    String build();

    /**
     * Values to bind to the positional (?) placeholders produced by build().
     * Call build() first; implementations collect parameters while building.
     */
    default Object[] getParameters() {
        return new Object[0];
    }
}
