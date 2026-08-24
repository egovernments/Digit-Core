package org.digit.services.boundary.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a boundary relationship search.
 *
 * <p>There is no tenant field: the service reads the tenant only from the {@code X-Tenant-Id} header.
 *
 * <p>Shape of the result depends on the flags — with neither {@code includeChildren} nor
 * {@code includeParents} the service returns a flat list of matching boundaries; with either, it
 * returns a nested tree.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundaryRelationshipSearchCriteria {
    private String hierarchyType;
    private String boundaryType;
    /** Sent as one repeated {@code codes} parameter per entry, which is how the service reads them. */
    private List<String> codes;
    private String parent;
    /** Only ever sent when true: the service compares against the literal string {@code "true"}. */
    private Boolean includeChildren;
    /** Only ever sent when true, for the same reason. */
    private Boolean includeParents;
    private Integer limit;
    private Integer offset;
}
