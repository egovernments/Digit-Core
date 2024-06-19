package org.selco.e4h.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.selco.e4h.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
public class UpdateService {

	@Autowired
	private UpdateUtils indexerUtils;

	@Value("${egov.infra.indexer.host}")
	private String esHostUrl;

	@Value("${egov.update.index.path}")
	private String updateIndexPath;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public void configureRestTemplate() {
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
	}


	public void updateEsDoc(String topic, String kafkaJson) {
		JSONArray kafkaJsonArray = null;
		try {
			kafkaJsonArray = indexerUtils.constructArrayForUpdate(kafkaJson, "$.Employees.*", true);
			for (int i = 0; i < kafkaJsonArray.length(); i++) {
				if (null != kafkaJsonArray.get(i)) {
					String stringifiedObject = indexerUtils.buildString(kafkaJsonArray.get(i));
					String id = JsonPath.read(stringifiedObject, "$.tenantId");
					String isLive = JsonPath.read(stringifiedObject, "$.isActive") ? "TRUE" : "FALSE";
					Long createdTime = JsonPath.read(stringifiedObject, "$.auditDetails.createdDate");

					// Prepare the JSON payload for the update request
					String jsonPayload = String.format(
							"{ \"script\": { \"source\": \"if (ctx._source.isLive == 'FALSE') { ctx._source.isLive = params.newIsLive; ctx._source.createdTime = params.newCreatedTime; }\", \"lang\": \"painless\", \"params\": { \"newIsLive\": \"%s\", \"newCreatedTime\": %d } }, \"doc_as_upsert\": false }",
							isLive, createdTime
					);

					// Construct the update URL
					String updateUrl = esHostUrl + updateIndexPath + id;

					try {
						log.debug("Record being updated: " + jsonPayload);

						final HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
						headers.add("Authorization", indexerUtils.getESEncodedCredentials());
						final HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
						String response = restTemplate.postForObject(updateUrl, entity, String.class);
						// Log based on the response
						if (response.contains("\"result\":\"updated\"")) {
							log.info("Document with ID {} successfully updated.", id);
						} else if (response.contains("\"type\":\"document_missing_exception\"")) {
							log.warn("Document with ID {} not found.", id);
						} else if (response.contains("\"type\":\"version_conflict_engine_exception\"")) {
							log.error("Version conflict for document with ID {}.", id);
						} else if (response.contains("\"type\":\"shard_failed\"")) {
							log.error("Shard failure while updating document with ID {}.", id);
						} else {
							log.error("Unexpected response: {}", response);
						}
					} catch (final ResourceAccessException e) {
						log.error("ES is DOWN, Pausing kafka listener.......");
						indexerUtils.orchestrateListenerOnESHealth();
					} catch (Exception e) {
						log.error("Exception while trying to update the ES document. Note: ES is not Down.", e);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while building curl for updating", e);
		}
	}

	private class CustomResponseErrorHandler implements ResponseErrorHandler {

		@Override
		public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
			HttpStatusCode statusCode = response.getStatusCode();
			return statusCode.is4xxClientError() || statusCode.is5xxServerError();
		}

		@Override
		public void handleError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
			if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
				// Handle 404 error specifically
				log.warn("Document not found (404): {}", response.getStatusText());
			} else {
				// Let other errors be handled by the default handler
				new DefaultResponseErrorHandler().handleError(response);
			}
		}
	}
}
