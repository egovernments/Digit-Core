package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.EventConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UpdateUtils {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ConsumerConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * A Poll thread that polls es for its status and keeps the kafka container
	 * paused until ES is back up. Once ES is up, container is resumed and all the
	 * stacked up records in the queue are processed.
	 */
	public void orchestrateListenerOnESHealth() {
		EventConsumerConfig.pauseContainer();
		log.info("Polling ES....");
		final Runnable esPoller = new Runnable() {
			boolean threadRun = true;

			public void run() {
				if (threadRun) {
					Object response = null;
					try {
						StringBuilder url = new StringBuilder();
						url.append(config.getEsHostUrl()).append("/_search");
						final HttpHeaders headers = new HttpHeaders();
						headers.add("Authorization", getESEncodedCredentials());
						final HttpEntity entity = new HttpEntity(headers);
						response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, Map.class);
					} catch (Exception e) {
						log.error("ES is DOWN..", e);
					}
					if (response != null) {
						log.info("ES is UP!");
						EventConsumerConfig.resumeContainer();
						threadRun = false;
					}
				}
			}
		};
		scheduler.scheduleAtFixedRate(esPoller, 0, Long.valueOf(config.getPollInterval()), TimeUnit.SECONDS);
	}

	public JSONArray constructArrayForUpdate(String kafkaJson, String jsonPath) throws Exception {
		JSONArray kafkaJsonArray = null;
		try {
			kafkaJsonArray = new JSONArray(JsonPath.read(kafkaJson, jsonPath).toString());
		} catch (PathNotFoundException e) {
			log.error("JSON path not found: {}", jsonPath, e);
			return null;
		} catch (JSONException e) {
			log.error("Error parsing JSON: {}", kafkaJson, e);
			throw e;
		} catch (Exception e) {
			log.error("Exception while constructing json array for bulk index: ", e);
			log.error("Object: " + kafkaJson);
			throw e;
		}
		return kafkaJsonArray;
	}

	public String getESEncodedCredentials() {
		String credentials = config.getEsUsername() + ":" + config.getEsPassword();
		byte[] credentialsBytes = credentials.getBytes();
		byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
		return "Basic " + new String(base64CredentialsBytes);
	}
}
