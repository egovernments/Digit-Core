package org.digit.services.filestore.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A multipart upload: one or more files plus the metadata they are filed under.
 *
 * <p>Only the files are multipart parts. {@code module}, {@code tag} and {@code ownerIds} are bound
 * by the service as ordinary request parameters, so they travel in the query string.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {
    private List<UploadPart> files;
    private String module;
    private String tag;
    private List<String> ownerIds;
}
