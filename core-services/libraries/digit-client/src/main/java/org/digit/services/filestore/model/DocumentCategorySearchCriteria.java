package org.digit.services.filestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for a document category search.
 *
 * <p>{@code isSensitive} is sent as the literal string {@code "true"} or {@code "false"}; the
 * service rejects anything else, including {@code 1}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCategorySearchCriteria {
    private String type;
    private String docCode;
    private Boolean isSensitive;
    private Integer limit;
    private Integer offset;
}
