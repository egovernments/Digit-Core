package org.digit.services.filestore.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a file metadata search. At least one must be set — the service rejects an
 * unfiltered search.
 *
 * <p>The list filters travel as single comma-separated parameters, which is how the service parses
 * them; repeating the parameter would silently use only the first value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchCriteria {
    private String tag;
    private List<String> ownerIds;
    private List<String> fileStoreIds;
}
