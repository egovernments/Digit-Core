package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.selco.e4h.kafka.consumer.EventConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${egov.infra.indexer.host}")
	private String esHostUrl;

	@Value("${elasticsearch.poll.interval.seconds}")
	private String pollInterval;


	@Value("${egov.indexer.es.username}")
	private String esUsername;

	@Value("${egov.indexer.es.password}")
	private String esPassword;

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
						url.append(esHostUrl).append("/_search");
						final HttpHeaders headers = new HttpHeaders();
						headers.add("Authorization", getESEncodedCredentials());
						final HttpEntity entity = new HttpEntity(headers);
						response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, Map.class);
					} catch (Exception e) {
						log.error("ES is DOWN..");
					}
					if (response != null) {
						log.info("ES is UP!");
						EventConsumerConfig.resumeContainer();
						threadRun = false;
					}
				}
			}
		};
		scheduler.scheduleAtFixedRate(esPoller, 0, Long.valueOf(pollInterval), TimeUnit.SECONDS);
	}

	/**
	 * Helper method in data transformation
	 *
	 * @param object
	 * @return
	 */
	public String buildString(Object object) {
		// JsonPath cannot be applied on the type JSONObject. String has to be built of
		// it and then used.
		String[] array = object.toString().split(":");
		StringBuilder jsonArray = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			jsonArray.append(array[i]);
			if (i != array.length - 1)
				jsonArray.append(":");
		}
		return jsonArray.toString();
	}

	/**
	 * Helper method in data transformation
	 *
	 * @param jsonString
	 * @return
	 */
	public String pullArrayOutOfString(String jsonString) {
		String[] array = jsonString.split(":");
		StringBuilder jsonArray = new StringBuilder();
		for (int i = 1; i < array.length; i++) {
			jsonArray.append(array[i]);
			if (i != array.length - 1)
				jsonArray.append(":");
		}
		jsonArray.deleteCharAt(jsonArray.length() - 1);

		return jsonArray.toString();
	}

	/**
	 * Helper method for data transformation.
	 *
	 * @param kafkaJson
	 * @param index
	 * @param isBulk
	 * @return
	 * @throws Exception
	 */
	public JSONArray constructArrayForUpdate(String kafkaJson, String jsonPath, boolean isBulk) throws Exception {
		JSONArray kafkaJsonArray = null;
		try {
			if (isBulk) {
				// Validating if the request is a valid json array.
				if (null != jsonPath) {
					if (JsonPath.read(kafkaJson, jsonPath) instanceof net.minidev.json.JSONArray) {
						String inputArray = mapper.writeValueAsString(JsonPath.read(kafkaJson, jsonPath));
						kafkaJsonArray = new JSONArray(inputArray);
					}
				} else if (pullArrayOutOfString(kafkaJson).startsWith("[")
						&& pullArrayOutOfString(kafkaJson).endsWith("]")) {
					kafkaJsonArray = new JSONArray(pullArrayOutOfString(kafkaJson));
				} else {
					log.info("Invalid request for a json array!");
					return null;
				}
			} else {
				String jsonArray = null;
				if (null != jsonPath) {
					kafkaJson = mapper.writeValueAsString(JsonPath.read(kafkaJson, jsonPath));
					jsonArray = "[" + kafkaJson + "]";
				} else {
					jsonArray = "[" + kafkaJson + "]";
				}
				kafkaJsonArray = new JSONArray(jsonArray);
			}
		} catch (Exception e) {
			log.error("Exception while constructing json array for bulk index: ", e);
			log.error("Object: " + kafkaJson);
			throw e;
		}
		return kafkaJsonArray;
	}

	public String getESEncodedCredentials() {
		String credentials = esUsername + ":" + esPassword;
		byte[] credentialsBytes = credentials.getBytes();
		byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
		return "Basic " + new String(base64CredentialsBytes);
	}
}
