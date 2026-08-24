package org.digit.services.filestore.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A file as returned by upload. Mirrors the service's {@code File}; named differently here to
 * avoid colliding with {@link java.io.File} at call sites.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {
    @JsonProperty("fileStoreId")
    private String fileStoreId;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("ownerIds")
    private List<String> ownerIds;
}
