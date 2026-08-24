package org.digit.services.filestore;

import org.digit.config.ApiProperties;
import org.digit.config.PropagationProperties;
import org.digit.exception.DigitClientException;
import org.digit.services.filestore.model.ConfirmUploadResponse;
import org.digit.services.filestore.model.DeleteResult;
import org.digit.services.filestore.model.DocumentCategory;
import org.digit.services.filestore.model.DocumentCategorySearchCriteria;
import org.digit.services.filestore.model.DownloadUrls;
import org.digit.services.filestore.model.FileInfo;
import org.digit.services.filestore.model.FileSearchCriteria;
import org.digit.services.filestore.model.FileSearchResponse;
import org.digit.services.filestore.model.FileUploadRequest;
import org.digit.services.filestore.model.StorageResponse;
import org.digit.services.filestore.model.StoredFile;
import org.digit.services.filestore.model.UploadPart;
import org.digit.services.filestore.model.UploadUrlRequest;
import org.digit.services.filestore.model.UploadUrlResponse;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class FilestoreClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final PropagationProperties propagationProperties;

    public FilestoreClient(RestTemplate restTemplate, ApiProperties apiProperties, PropagationProperties propagationProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.propagationProperties = propagationProperties;
    }

    // ── Files ────────────────────────────────────────────────────────────────

    /**
     * Whether the file exists, answered from its metadata.
     *
     * <p>Deliberately not a GET of the file itself: that endpoint streams the whole object back, so
     * an existence check used to download the entire file and discard it.
     */
    public boolean isFileAvailable(String fileId) {
        requireText(fileId, "File ID cannot be null or empty");
        try {
            return getFileMetadata(fileId) != null;
        }
        catch (HttpClientErrorException e) {
            return false;
        }
        catch (DigitClientException e) {
            log.debug("File {} unavailable: {}", fileId, e.getMessage());
            return false;
        }
    }

    /** As {@link #isFileAvailable}, but reports why the file could not be reached. */
    public void validateFileAvailability(String fileId) {
        requireText(fileId, "File ID cannot be null or empty");
        if (getFileMetadata(fileId) == null) {
            throw new DigitClientException("File not found: " + fileId);
        }
    }

    /** Metadata for one file, or null when the service knows nothing about it. */
    public FileInfo getFileMetadata(String fileStoreId) {
        requireText(fileStoreId, "File ID cannot be null or empty");
        List<FileInfo> files = searchFiles(FileSearchCriteria.builder()
                .fileStoreIds(List.of(fileStoreId)).build());
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    /** Searches file metadata. At least one filter is required. */
    public List<FileInfo> searchFiles(FileSearchCriteria criteria) {
        if (criteria == null
                || (isBlank(criteria.getTag()) && isEmpty(criteria.getOwnerIds()) && isEmpty(criteria.getFileStoreIds()))) {
            throw new DigitClientException("at least one of tag, ownerIds or fileStoreIds is required");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(filesUrl());
        addIfText(builder, "tag", criteria.getTag());
        addIfText(builder, "ownerIds", csv(criteria.getOwnerIds()));
        addIfText(builder, "fileStoreIds", csv(criteria.getFileStoreIds()));

        ResponseEntity<FileSearchResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), FileSearchResponse.class);
        return response.getBody() == null ? List.of() : response.getBody().getFiles();
    }

    /** Downloads the file's bytes. */
    public byte[] downloadFile(String fileStoreId) {
        requireText(fileStoreId, "File ID cannot be null or empty");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        ResponseEntity<byte[]> response = this.restTemplate.exchange(
                filesUrl() + "/" + fileStoreId, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        return response.getBody();
    }

    public boolean deleteFile(String fileStoreId) {
        requireText(fileStoreId, "File ID cannot be null or empty");
        ResponseEntity<DeleteResult> response = this.restTemplate.exchange(
                filesUrl() + "/" + fileStoreId, HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), DeleteResult.class);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getDeleted());
    }

    /** Pre-signed download URLs, keyed by file id. */
    public Map<String, String> getDownloadUrls(List<String> fileStoreIds) {
        if (isEmpty(fileStoreIds)) {
            throw new DigitClientException("fileStoreIds is required");
        }
        String url = UriComponentsBuilder.fromUriString(filesUrl() + "/download-urls")
                .queryParam("fileStoreIds", csv(fileStoreIds))
                .toUriString();
        ResponseEntity<DownloadUrls> response = this.restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), DownloadUrls.class);
        return response.getBody() == null ? Map.of() : response.getBody().asMap();
    }

    // ── Upload ───────────────────────────────────────────────────────────────

    /**
     * Uploads one or more files as multipart form data.
     *
     * <p>Only the files are parts; module, tag and owner ids are bound by the service as request
     * parameters, so they go in the query string.
     */
    public List<StoredFile> uploadFiles(FileUploadRequest request) {
        if (request == null || isEmpty(request.getFiles())) {
            throw new DigitClientException("at least one file is required");
        }
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        for (UploadPart part : request.getFiles()) {
            form.add("file", filePart(part));
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(filesUrl() + "/upload");
        addIfText(builder, "module", request.getModule());
        addIfText(builder, "tag", request.getTag());
        if (request.getOwnerIds() != null) {
            for (String ownerId : request.getOwnerIds()) {
                builder.queryParam("ownerIds", ownerId);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<StorageResponse> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.POST, new HttpEntity<>(form, headers), StorageResponse.class);
        return response.getBody() == null ? List.of() : response.getBody().getFiles();
    }

    public StoredFile uploadFile(String fileName, byte[] content, String contentType,
                                 String module, String tag, List<String> ownerIds) {
        List<StoredFile> stored = uploadFiles(FileUploadRequest.builder()
                .files(List.of(UploadPart.builder().fileName(fileName).content(content).contentType(contentType).build()))
                .module(module).tag(tag).ownerIds(ownerIds).build());
        return stored.isEmpty() ? null : stored.get(0);
    }

    /** Requests a pre-signed URL so the caller can upload the bytes directly to storage. */
    public UploadUrlResponse createUploadUrl(UploadUrlRequest request) {
        if (request == null || isBlank(request.getFileName())) {
            throw new DigitClientException("fileName is required to request an upload URL");
        }
        ResponseEntity<UploadUrlResponse> response = this.restTemplate.postForEntity(
                filesUrl() + "/upload-url", request, UploadUrlResponse.class);
        return response.getBody();
    }

    /** Confirms a pre-signed upload finished, so the service marks the file usable. */
    public String confirmUpload(String fileStoreId) {
        requireText(fileStoreId, "fileStoreId is required");
        ResponseEntity<ConfirmUploadResponse> response = this.restTemplate.postForEntity(
                filesUrl() + "/confirm-upload", Map.of("fileStoreId", fileStoreId), ConfirmUploadResponse.class);
        return response.getBody() == null ? null : response.getBody().getStatus();
    }

    // ── Document categories ──────────────────────────────────────────────────

    /** Searches document categories. The service answers with a bare array. */
    public List<DocumentCategory> searchDocumentCategories(DocumentCategorySearchCriteria criteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(documentCategoriesUrl());
        if (criteria != null) {
            addIfText(builder, "type", criteria.getType());
            addIfText(builder, "docCode", criteria.getDocCode());
            if (criteria.getIsSensitive() != null) {
                // The service accepts only the literal strings "true" and "false" here.
                builder.queryParam("isSensitive", criteria.getIsSensitive() ? "true" : "false");
            }
            if (criteria.getLimit() != null) {
                builder.queryParam("limit", criteria.getLimit());
            }
            if (criteria.getOffset() != null) {
                builder.queryParam("offset", criteria.getOffset());
            }
        }
        ResponseEntity<List<DocumentCategory>> response = this.restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                new ParameterizedTypeReference<List<DocumentCategory>>(){});
        return response.getBody() == null ? List.of() : response.getBody();
    }

    /** One document category by its code, or null when there is none. */
    public DocumentCategory getDocumentCategory(String docCode) {
        requireText(docCode, "docCode is required");
        try {
            ResponseEntity<DocumentCategory> response = this.restTemplate.exchange(
                    documentCategoriesUrl() + "/" + docCode, HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()), DocumentCategory.class);
            return response.getBody();
        }
        catch (HttpClientErrorException.NotFound e) {
            return null;
        }
        catch (DigitClientException e) {
            if (e.getHttpStatus() != null && e.getHttpStatus().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Registers a document category — the allowed formats and size bounds a document of that code
     * must satisfy.
     *
     * <p>Note the size bounds are strings on the wire, matching the service.
     */
    public DocumentCategory createDocumentCategory(DocumentCategory category) {
        if (category == null || isBlank(category.getCode())) {
            throw new DigitClientException("code is required to create a document category");
        }
        ResponseEntity<DocumentCategory> response = this.restTemplate.postForEntity(
                documentCategoriesUrl(), category, DocumentCategory.class);
        return response.getBody();
    }

    public DocumentCategory updateDocumentCategory(String docCode, DocumentCategory category) {
        requireText(docCode, "docCode is required");
        if (category == null) {
            throw new DigitClientException("update payload is required");
        }
        ResponseEntity<DocumentCategory> response = this.restTemplate.exchange(
                documentCategoriesUrl() + "/" + docCode, HttpMethod.PUT,
                new HttpEntity<>(category), DocumentCategory.class);
        return response.getBody();
    }

    public boolean deleteDocumentCategory(String docCode) {
        requireText(docCode, "docCode is required");
        ResponseEntity<DeleteResult> response = this.restTemplate.exchange(
                documentCategoriesUrl() + "/" + docCode, HttpMethod.DELETE,
                new HttpEntity<>(new HttpHeaders()), DeleteResult.class);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getDeleted());
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private String filesUrl() {
        return this.apiProperties.getFilestoreServiceUrl() + "/filestore/v3/files";
    }

    private String documentCategoriesUrl() {
        return this.apiProperties.getFilestoreServiceUrl() + "/filestore/v3/document-categories";
    }

    /**
     * Wraps one part so the multipart converter sends a filename and content type for it. A
     * {@code ByteArrayResource} has no filename of its own, which the service needs, hence the
     * override.
     */
    private static HttpEntity<Resource> filePart(UploadPart part) {
        if (part == null || (part.getContent() == null && part.getResource() == null)) {
            throw new DigitClientException("upload part must carry either content or a resource");
        }
        Resource resource = part.getResource() != null
                ? part.getResource()
                : new ByteArrayResource(part.getContent()) {
                    @Override
                    public String getFilename() {
                        return part.getFileName();
                    }
                };
        HttpHeaders headers = new HttpHeaders();
        if (!isBlank(part.getContentType())) {
            headers.setContentType(MediaType.parseMediaType(part.getContentType()));
        }
        return new HttpEntity<>(resource, headers);
    }

    private static void addIfText(UriComponentsBuilder builder, String name, String value) {
        if (!isBlank(value)) {
            builder.queryParam(name, value);
        }
    }

    private static String csv(List<String> values) {
        return isEmpty(values) ? null : String.join(",", values);
    }

    private static boolean isEmpty(List<?> values) {
        return values == null || values.isEmpty();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new DigitClientException(message);
        }
    }
}
