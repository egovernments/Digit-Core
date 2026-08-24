package org.digit.services.filestore.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Download URLs for a set of file ids.
 *
 * <p>The service returns an unusual shape: a {@code fileStoreIds} array of id/URL pairs, and then
 * every one of those pairs flattened in again as a top-level {@code "<id>": "<url>"} entry. The
 * declared property consumes the array, and {@link JsonAnySetter} — which only receives keys no
 * property matched — collects the flattened duplicates.
 */
@Data
public class DownloadUrls {

    @JsonProperty("fileStoreIds")
    private List<FileStoreUrl> fileStoreIds;

    @JsonIgnore
    private final Map<String, String> urlsById = new LinkedHashMap<>();

    @JsonAnySetter
    void putFlattenedEntry(String key, Object value) {
        if (value instanceof String url) {
            this.urlsById.put(key, url);
        }
    }

    /**
     * The id-to-URL mapping, preferring the declared array since it is the shape the service
     * documents, and falling back to the flattened entries.
     */
    @JsonIgnore
    public Map<String, String> asMap() {
        if (this.fileStoreIds == null || this.fileStoreIds.isEmpty()) {
            return Map.copyOf(this.urlsById);
        }
        Map<String, String> merged = new LinkedHashMap<>();
        for (FileStoreUrl entry : this.fileStoreIds) {
            if (entry != null && entry.getId() != null) {
                merged.put(entry.getId(), entry.getUrl());
            }
        }
        return merged;
    }
}
