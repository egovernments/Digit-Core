// package org.egov.filestore.repository.impl;

// import com.google.cloud.storage.*;
// import org.egov.filestore.domain.model.Artifact;
// import org.egov.filestore.repository.CloudFilesManager;
// import org.egov.tracer.model.CustomException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.stereotype.Service;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.io.FilenameUtils;

// import java.io.IOException;
// import java.util.*;
// import java.util.concurrent.TimeUnit;
// import java.awt.image.BufferedImage;
// import javax.imageio.ImageIO;
// import java.io.ByteArrayOutputStream;

// @Slf4j
// @Service
// @ConditionalOnProperty(value = "isGcpStorageEnabled", havingValue = "true")
// public class GoogleCloudStorageImpl implements CloudFilesManager {

//     @Autowired
//     private Storage storage;

//     @Value("${gcp.bucket.name}")
//     private String bucketName;

//     @Value("${gcp.signed.url.expiry.minutes}")
//     private Integer signedUrlExpiryMinutes;

//     @Value("${gcp.project.id}")
//     private String projectId;

//     private static final String _large = "_large";
//     private static final String _medium = "_medium";
//     private static final String _small = "_small";

//     @Override
//     public void saveFiles(List<Artifact> artifacts) {
//         try {
//             for (Artifact artifact : artifacts) {
//                 String completeName = artifact.getFileLocation().getFileName();
//                 int index = completeName.indexOf('/');
//                 String fileNameWithPath = completeName.substring(index + 1);

//                 // Upload the main file
//                 BlobId blobId = BlobId.of(bucketName, fileNameWithPath);
//                 BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                         .setContentType(artifact.getMultipartFile().getContentType())
//                         .build();

//                 storage.create(blobInfo, artifact.getMultipartFile().getBytes());

//                 // Handle thumbnail images if present
//                 if (artifact.getThumbnailImages() != null && !artifact.getThumbnailImages().isEmpty()) {
//                     for (Map.Entry<String, BufferedImage> entry : artifact.getThumbnailImages().entrySet()) {
//                         ByteArrayOutputStream os = new ByteArrayOutputStream();
//                         ImageIO.write(entry.getValue(), 
//                             FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename()), 
//                             os);
                        
//                         BlobId thumbnailBlobId = BlobId.of(bucketName, entry.getKey());
//                         BlobInfo thumbnailBlobInfo = BlobInfo.newBuilder(thumbnailBlobId)
//                                 .setContentType(artifact.getMultipartFile().getContentType())
//                                 .build();

//                         storage.create(thumbnailBlobInfo, os.toByteArray());
//                         os.flush();
//                     }
//                 }
//             }
//         } catch (IOException e) {
//             log.error("Error while uploading files to GCP: ", e);
//             throw new CustomException("GCP_UPLOAD_ERROR", "Failed to upload files to Google Cloud Storage");
//         }
//     }

//     @Override
//     public Map<String, String> getFiles(List<org.egov.filestore.persistence.entity.Artifact> artifacts) {
//         Map<String, String> mapOfIdAndUrls = new HashMap<>();
        
//         try {
//             for (org.egov.filestore.persistence.entity.Artifact artifact : artifacts) {
//                 String completeName = artifact.getFileName();
//                 int index = completeName.indexOf('/');
//                 String fileNameWithPath = completeName.substring(index + 1);

//                 if (isFileAnImage(fileNameWithPath)) {
//                     // Handle image files with thumbnails
//                     StringBuilder url = new StringBuilder();
//                     String[] imageFormats = {_large, _medium, _small};
                    
//                     // Get URL for original image
//                     url.append(getSignedUrl(fileNameWithPath));
                    
//                     // Get URLs for thumbnails
//                     String replaceString = fileNameWithPath.substring(
//                         fileNameWithPath.lastIndexOf('.'),
//                         fileNameWithPath.length()
//                     );
                    
//                     for (String format : imageFormats) {
//                         url.append(",");
//                         String thumbnailPath = fileNameWithPath.replaceAll(
//                             replaceString, 
//                             format + replaceString
//                         );
//                         url.append(getSignedUrl(thumbnailPath));
//                     }
                    
//                     mapOfIdAndUrls.put(artifact.getFileStoreId(), url.toString());
//                 } else {
//                     // Handle non-image files
//                     mapOfIdAndUrls.put(
//                         artifact.getFileStoreId(), 
//                         getSignedUrl(fileNameWithPath)
//                     );
//                 }
//             }
//         } catch (Exception e) {
//             log.error("Error while generating signed URLs: ", e);
//             throw new CustomException("GCP_URL_GENERATION_ERROR", "Failed to generate signed URLs");
//         }
        
//         return mapOfIdAndUrls;
//     }

//     private String getSignedUrl(String fileName) {
//         try {
//             BlobId blobId = BlobId.of(bucketName, fileName);
//             Blob blob = storage.get(blobId);
            
//             if (blob == null) {
//                 throw new CustomException("GCP_BLOB_NOT_FOUND", "File not found in Google Cloud Storage");
//             }

//             return blob.signUrl(signedUrlExpiryMinutes, TimeUnit.MINUTES).toString();
//         } catch (Exception e) {
//             log.error("Error generating signed URL for file {}: {}", fileName, e.getMessage());
//             throw new CustomException("GCP_SIGNED_URL_ERROR", "Failed to generate signed URL");
//         }
//     }

//     private boolean isFileAnImage(String filePath) {
//         if (filePath.split("[\\.]").length > 1) {
//             String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
//             return Arrays.asList("jpg", "jpeg", "png", "gif", "bmp").contains(extension);
//         }
//         return false;
//     }
// } 


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

    @Value("${gcp.credentials.path}")
    private String credentialsPath;

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

        storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath)))
                .build()
                .getService();


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

