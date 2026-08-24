package org.digit.services.filestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

/**
 * One file of a multipart upload.
 *
 * <p>Supply either {@code content} or {@code resource}. Prefer a resource for anything large: the
 * byte array is held in heap for the whole request, whereas a file-backed resource streams.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPart {
    private String fileName;
    private String contentType;
    private byte[] content;
    private Resource resource;
}
