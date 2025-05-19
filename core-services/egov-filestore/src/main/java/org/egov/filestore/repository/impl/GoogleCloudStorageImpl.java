package org.egov.filestore.repository.impl;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.repository.CloudFilesManager;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Slf4j
@Service
@ConditionalOnProperty(value = "isGcpStorageEnabled", havingValue = "true")
public class GoogleCloudStorageImpl implements CloudFilesManager {

    private Storage storage;

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.client.email}")
    private String clientEmail;

    @Value("${gcp.private.key}")
    private String privateKey;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${image.small}")
    private String _small;

    @Value("${image.medium}")
    private String _medium;

    @Value("${image.large}")
    private String _large;

    @Autowired
    private CloudFileMgrUtils util;

    @PostConstruct
    public void init() throws IOException {
        try {
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                null, // clientId (optional)
                clientEmail,
                privateKey,
                null, // privateKeyId (optional)
                null  // scopes or user-defined headers
            );
            
            storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
        } catch (Exception e) {
            log.error("Error initializing GCP storage: ", e);
            throw new CustomException("GCP_INIT_ERROR", "Failed to initialize Google Cloud Storage");
        }
    }

    @Override
    public void saveFiles(List<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            try {
                String filePath = artifact.getFileLocation().getFileName();
                BlobId blobId = BlobId.of(bucketName, filePath);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(artifact.getMultipartFile().getContentType()).build();

                if (artifact.getMultipartFile().getContentType().startsWith("image/")) {
                    String extension = filePath.substring(filePath.lastIndexOf('.') + 1);
                    Map<String, BufferedImage> thumbnails = artifact.getThumbnailImages();
                    for (String key : thumbnails.keySet()) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(thumbnails.get(key), extension, os);
                        BlobId thumbBlobId = BlobId.of(bucketName, key);
                        BlobInfo thumbBlobInfo = BlobInfo.newBuilder(thumbBlobId).setContentType(artifact.getMultipartFile().getContentType()).build();
                        storage.create(thumbBlobInfo, os.toByteArray());
                    }
                }

                storage.create(blobInfo, artifact.getMultipartFile().getBytes());

            } catch (Exception e) {
                throw new CustomException("GCP_UPLOAD_ERROR", e.getMessage());
            }
        }
    }

    @Override
    public Map<String, String> getFiles(List<org.egov.filestore.persistence.entity.Artifact> artifacts) {
        Map<String, String> result = new HashMap<>();
        for (org.egov.filestore.persistence.entity.Artifact artifact : artifacts) {
            String fileName = artifact.getFileName();
            if (util.isFileAnImage(fileName)) {
                StringBuilder url = new StringBuilder();
                String[] formats = {_large, _medium, _small};
                url.append(generateSignedUrl(fileName));
                String ext = fileName.substring(fileName.lastIndexOf('.'));
                for (String format : formats) {
                    String altPath = fileName.replace(ext, format + ext);
                    url.append(",").append(generateSignedUrl(altPath));
                }
                result.put(artifact.getFileStoreId(), url.toString());
            } else {
                result.put(artifact.getFileStoreId(), generateSignedUrl(fileName));
            }
        }
        return result;
    }

    
    public Map<String, String> getFiles(Map<String, String> mapOfIdAndFilePath) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : mapOfIdAndFilePath.entrySet()) {
            String path = entry.getValue();
            if (util.isFileAnImage(path)) {
                StringBuilder url = new StringBuilder();
                String[] formats = {_large, _medium, _small};
                url.append(generateSignedUrl(path));
                String ext = path.substring(path.lastIndexOf('.'));
                for (String format : formats) {
                    String altPath = path.replace(ext, format + ext);
                    url.append(",").append(generateSignedUrl(altPath));
                }
                result.put(entry.getKey(), url.toString());
            } else {
                result.put(entry.getKey(), generateSignedUrl(path));
            }
        }
        return result;
    }

    
    public Resource read(FileLocation fileLocation) {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, fileLocation.getFileName()));
            File tempFile = File.createTempFile("gcp_file", null);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(blob.getContent());
            }
            return new FileSystemResource(tempFile);
        } catch (Exception e) {
            throw new CustomException("GCP_READ_ERROR", e.getMessage());
        }
    }

    private String generateSignedUrl(String objectName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();
        return storage.signUrl(blobInfo, 15, java.util.concurrent.TimeUnit.MINUTES).toString();
    }
}

