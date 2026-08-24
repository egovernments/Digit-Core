package org.digit.services.filestore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The pre-signed URL to PUT the bytes to, and the id it will be stored under. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponse {
    @JsonProperty("preSignedUrl")
    private String preSignedUrl;
    @JsonProperty("fileStoreId")
    private String fileStoreId;
}
