package org.egov.filestore.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "isGcpStorageEnabled", havingValue = "true")
public class GoogleCloudStorageConfig {

    @Value("${gcp.project.id}")
    private String projectId;

    // @Value("${gcp.credentials.path}")
    // private String credentialsPath;

    // @Bean
    // public Storage storage() throws IOException {
    //     return StorageOptions.newBuilder()
    //             .setProjectId(projectId)
    //             .setCredentials(
    //                 ServiceAccountCredentials.fromStream(
    //                     new FileInputStream(credentialsPath)
    //                 )
    //             )
    //             .build()
    //             .getService();
    // }
    
} 