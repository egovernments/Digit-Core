package org.selco.e4h.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import static org.selco.e4h.config.ServiceConstants.*;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class UpdateService {

	@Autowired
	private UpdateUtils indexerUtils;

	@Autowired
	private ConsumerConfiguration config;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public void configureRestTemplate() {
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
	}

	public void updateEsDoc(String topic, String kafkaJson) {
		try {
			JSONArray kafkaJsonArray = indexerUtils.constructArrayForUpdate(kafkaJson, EMPLOYEE_PATH);
			for (int i = 0; i < kafkaJsonArray.length(); i++) {
				if (kafkaJsonArray.get(i) != null) {
					JSONObject jsonObject = kafkaJsonArray.getJSONObject(i);
					String stringifiedObject = jsonObject.toString();

					Optional<String> id = Optional.ofNullable(JsonPath.read(stringifiedObject, TENANT_PATH));
					Optional<Boolean> isActive = Optional.ofNullable(JsonPath.read(stringifiedObject, IS_ACTIVE_PATH));
					Optional<Long> accountCreationTime = Optional.ofNullable(JsonPath.read(stringifiedObject, CREATED_DATE_PATH));

					if (id.isPresent() && isActive.isPresent() && accountCreationTime.isPresent()) {
						String jsonPayload = buildJsonPayload(isActive.get(), accountCreationTime.get());
						String updateUrl = config.getEsHostUrl() + config.getUpdateIndexPath() + id.get();

						try {
							log.debug("Record being updated: {}", jsonPayload);
							HttpEntity<String> entity = new HttpEntity<>(jsonPayload, buildHeaders());
							String response = restTemplate.postForObject(updateUrl, entity, String.class);
							processResponse(response, id.get());
						} catch (HttpClientErrorException | HttpServerErrorException e) {
							log.error("HTTP error while updating the ES document with ID {}: {}", id, e.getMessage());
						} catch (ResourceAccessException e) {
							log.error("ES is DOWN, Pausing Kafka listener...", e);
							indexerUtils.orchestrateListenerOnESHealth();
						} catch (Exception e) {
							log.error("Unexpected error while updating the ES document with ID {}: {}", id, e.getMessage(), e);
						}
					} else {
						log.warn("Missing necessary fields for updating ES document: tenantId, isActive, or createdDate");
					}
				}
			}
		} catch (JSONException e) {
			log.error("JSON processing error while building curl for updating", e);
		} catch (Exception e) {
			log.error("Unexpected error while processing Kafka JSON for update", e);
		}
	}

	private String buildJsonPayload(boolean isActive, long accountCreationTime) {
		String isLive = isActive ? "true" : "false";
		return new JSONObject()
				.put("script", new JSONObject()
						.put("source", "if (ctx._source.Data.isLive.toString().equalsIgnoreCase('false')) { ctx._source.Data.isLive = params.newIsLive; ctx._source.Data.accountCreationTime = params.accountCreationTime; }")
						.put("lang", "painless")
						.put("params", new JSONObject()
								.put("newIsLive", isLive)
								.put("accountCreationTime", accountCreationTime)))
				.put("doc_as_upsert", false)
				.toString();
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", indexerUtils.getESEncodedCredentials());
		return headers;
	}

	private void processResponse(String response, String id) {
		if (response.contains("\"result\":\"updated\"")) {
			log.info("Document with ID {} successfully updated.", id);
		} else if (response.contains("\"type\":\"document_missing_exception\"")) {
			log.warn("Document with ID {} not found.", id);
		} else if (response.contains("\"type\":\"version_conflict_engine_exception\"")) {
			log.error("Version conflict for document with ID {}.", id);
		} else if (response.contains("\"type\":\"shard_failed\"")) {
			log.error("Shard failure while updating document with ID {}.", id);
		} else if (response.contains("\"type\":\"illegal_argument_exception\"")) {
			log.error("Illegal argument exception while updating document with ID {}: {}", id, response);
		} else {
			log.error("Unexpected response: {}", response);
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
