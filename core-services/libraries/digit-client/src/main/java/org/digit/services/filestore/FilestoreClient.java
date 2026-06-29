package org.digit.services.filestore;

import org.digit.config.ApiProperties;
import org.digit.config.PropagationProperties;
import org.digit.exception.DigitClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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

    public boolean isFileAvailable(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new DigitClientException("File ID cannot be null or empty");
        }
        try {
            log.debug("Checking file availability for fileId: {}", (Object)fileId);
            String url = this.apiProperties.getFilestoreServiceUrl() + "/filestore/v3/files/" + fileId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "*/*");
            ResponseEntity<byte[]> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class, new Object[0]);
            boolean available = response.getStatusCode().is2xxSuccessful();
            log.debug("File {} availability: {}", (Object)fileId, (Object)available);
            return available;
        }
        catch (HttpClientErrorException.Forbidden e) {
            log.warn("Access forbidden for file {}: {}", (Object)fileId, (Object)e.getMessage());
            return false;
        }
        catch (HttpClientErrorException.NotFound e) {
            log.debug("File {} not found", (Object)fileId);
            return false;
        }
        catch (HttpClientErrorException.BadRequest e) {
            log.error("Bad request for file {}: {}", new Object[]{fileId, e.getMessage()});
            return false;
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            log.error("Error checking file {} availability: {}", new Object[]{fileId, e.getMessage(), e});
            return false;
        }
    }

    public boolean validateFileAvailability(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new DigitClientException("File ID cannot be null or empty");
        }
        try {
            log.debug("Validating file availability for fileId: {}", (Object)fileId);
            String url = this.apiProperties.getFilestoreServiceUrl() + "/filestore/v3/files/" + fileId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "*/*");
            ResponseEntity<byte[]> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class, new Object[0]);
            log.debug("File {} is available", (Object)fileId);
            return response.getStatusCode().is2xxSuccessful();
        }
        catch (HttpClientErrorException.Forbidden e) {
            log.error("Access forbidden for file {}: {}", (Object)fileId, (Object)e.getMessage());
            throw new DigitClientException("Access forbidden for file: " + fileId, e);
        }
        catch (HttpClientErrorException.NotFound e) {
            log.error("File not found: {}", (Object)fileId);
            throw new DigitClientException("File not found: " + fileId, e);
        }
        catch (HttpClientErrorException.BadRequest e) {
            log.error("Bad request for file {}: {}", new Object[]{fileId, e.getMessage()});
            throw new DigitClientException("Bad request for file: " + fileId + " - " + e.getMessage(), e);
        }
        catch (Exception e) {
            if (e instanceof DigitClientException) {
                throw e;
            }
            throw new DigitClientException("File not available: " + fileId + " - " + e.getMessage(), e);
        }
    }
}
