package org.digit.services.filestore.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Metadata for a stored file. Mirrors the service's {@code FileInfo}. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    @JsonProperty("fileStoreId")
    private String fileStoreId;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("url")
    private String url;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("ownerIds")
    private List<String> ownerIds;
}
