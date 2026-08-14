package org.egov.filestore.repository.impl.minio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.apache.commons.io.FilenameUtils;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.persistence.entity.Artifact;
import org.egov.filestore.repository.CloudFilesManager;
import org.egov.filestore.repository.impl.CloudFileMgrUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(value = "isS3Enabled", havingValue = "true")
public class MinioRepository implements CloudFilesManager {

	private static final String ERROR_IN_CONFIGURATION = "Error in Configuration";

	@Autowired
	private MinioClient minioClient;
	
	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private CloudFileMgrUtils util;
	
	@Autowired
	private FileStoreConfig fileStoreConfig;

	@Override
	public void saveFiles(List<org.egov.filestore.domain.model.Artifact> artifacts) {

		List<org.egov.filestore.persistence.entity.Artifact> persistList = new ArrayList<>();
		artifacts.forEach(artifact -> {
			FileLocation fileLocation = artifact.getFileLocation();
			String completeName = fileLocation.getFileName();
			int index = completeName.indexOf('/');
			String fileNameWithPath = completeName.substring(index + 1, completeName.length());
			push(artifact.getMultipartFile(), fileNameWithPath);

			if (artifact.getThumbnailImages() != null && !artifact.getThumbnailImages().isEmpty())
				pushThumbnailImages(artifact);

			fileLocation.setFileSource(minioConfig.getSource());
			persistList.add(mapToEntity(artifact));

		});
	}

	

	private void push(MultipartFile multipartFile, String fileNameWithPath) {
		try {
			InputStream is = multipartFile.getInputStream();
			long fileSize = multipartFile.getSize();

			PutObjectArgs args = PutObjectArgs.builder()
					.bucket(minioConfig.getBucketName())
					.object(fileNameWithPath)
					.stream(is, fileSize, -1)
					.contentType(multipartFile.getContentType())
					.build();

			minioClient.putObject(args);
			log.debug("Upload Successful");

		} catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException
				| IOException e) {
			log.error("Error occurred: ", e);
			throw new RuntimeException(ERROR_IN_CONFIGURATION);
		}

	}

	private void push(InputStream is, long contentLength, String contentType, String fileNameWithPath) {
		try {
			PutObjectArgs args = PutObjectArgs.builder()
					.bucket(minioConfig.getBucketName())
					.object(fileNameWithPath)
					.stream(is, contentLength, -1)
					.contentType(contentType)
					.build();
			minioClient.putObject(args);

		} catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException
				| IOException e) {
			log.error("Error occurred: ", e);
			throw new RuntimeException(ERROR_IN_CONFIGURATION);
		}

	}

	private void pushThumbnailImages(org.egov.filestore.domain.model.Artifact artifact) {

		try {

			for (Map.Entry<String, BufferedImage> entry : artifact.getThumbnailImages().entrySet()) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(entry.getValue(),
						FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename()), os);
				byte[] byteArray = os.toByteArray();
				ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
				push(is, byteArray.length, artifact.getMultipartFile().getContentType(), entry.getKey());
				os.flush();
			}

		} catch (Exception ioe) {

			Map<String, String> map = new HashMap<>();
			log.error("Exception while uploading the image: ", ioe);
			map.put("ERROR_MINIO_UPLOAD", "An error has occured while trying to upload image to filestore system .");
			throw new CustomException(map);
		}
	}

	@Override
	public Map<String, String> getFiles(List<Artifact> artifacts) {

		Map<String, String> mapOfIdAndSASUrls = new HashMap<>();

		for(Artifact artifact : artifacts) {
			
			String fileLocation = artifact.getFileLocation().getFileName();
			String fileName = fileLocation.
					substring(fileLocation.indexOf('/') + 1, fileLocation.length());
			String signedUrl = getSignedUrl(fileName);
			if (util.isFileAnImage(artifact.getFileName())) {
				try {
					signedUrl = setThumnailSignedURL(fileName, new StringBuilder(signedUrl));
				} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
						| InsufficientDataException | InternalException
						| InvalidResponseException | NoSuchAlgorithmException
						| XmlParserException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mapOfIdAndSASUrls.put(artifact.getFileStoreId(), signedUrl);
			
		}
		return mapOfIdAndSASUrls;
	}
		
	private String setThumnailSignedURL(String fileName, StringBuilder url) throws InvalidKeyException, ErrorResponseException, IllegalArgumentException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, IOException {
		String[] imageFormats = { fileStoreConfig.get_large(), fileStoreConfig.get_medium(), fileStoreConfig.get_small() };
		for (String  format : Arrays.asList(imageFormats)) {
			url.append(",");
			String replaceString = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
			String path = fileName.replaceAll(replaceString, format + replaceString);
			url.append(getSignedUrl(path));
		}
		return url.toString();
	}
	
	private String getSignedUrl(String fileName) {

		String signedUrl = null;
		try {
			signedUrl = minioClient.getPresignedObjectUrl(
					GetPresignedObjectUrlArgs.builder()
							.method(io.minio.http.Method.GET)
							.bucket(minioConfig.getBucketName())
							.object(fileName)
							.expiry(fileStoreConfig.getPreSignedUrlTimeOut(), TimeUnit.SECONDS)
							.build());
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | XmlParserException
				| ServerException | IOException e) {
			e.printStackTrace();
		}
        return signedUrl;
	}

	public Resource read(FileLocation fileLocation) {

		Resource resource = null;

		if (fileLocation.getFileSource() == null || fileLocation.getFileSource().equals(minioConfig.getSource())) {
			String fileName = fileLocation.getFileName().substring(fileLocation.getFileName().indexOf('/') + 1,
					fileLocation.getFileName().length());

			try {
				InputStream stream = minioClient.getObject(
						GetObjectArgs.builder()
								.bucket(minioConfig.getBucketName())
								.object(fileName)
								.build());
				resource = new InputStreamResource(stream) {
					@Override
					public long contentLength() {
						return -1; // prevents Spring from pre-reading the stream to determine length
					}
				};
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException |
                     InsufficientDataException | InternalException |
                     InvalidResponseException | NoSuchAlgorithmException | XmlParserException | IOException |
                     ServerException e) {
				log.error("Error while downloading the file ", e);
				Map<String, String> map = new HashMap<>();
				map.put("ERROR_MINIO_DOWNLOAD",
						"An error has occured while trying to download image from filestore system .");
				throw new CustomException(map);
			}
		}
		return resource;
	}

	private Artifact mapToEntity(org.egov.filestore.domain.model.Artifact artifact) {

		FileLocation fileLocation = artifact.getFileLocation();
		return Artifact.builder().fileStoreId(fileLocation.getFileStoreId()).fileName(fileLocation.getFileName())
				.contentType(artifact.getMultipartFile().getContentType()).module(fileLocation.getModule())
				.tag(fileLocation.getTag()).tenantId(fileLocation.getTenantId())
				.fileSource(fileLocation.getFileSource()).build();
	}

}
