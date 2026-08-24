package org.digit.services.registry.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a registry data search. The two maps are not interchangeable:
 *
 * <ul>
 *   <li>{@code filters} matches a value at a JSON path exactly, compared as text, and supports
 *       dotted paths into nested objects.</li>
 *   <li>{@code contains} is a JSON containment test against the whole document, so it also matches
 *       nested fragments and array membership, and compares with JSON types rather than as text.</li>
 * </ul>
 *
 * <p>Both may be set, in which case the service applies both.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrySearchCriteria {
    private Map<String, Object> filters;
    private Map<String, Object> contains;
    private Integer limit;
    private Integer offset;
}
