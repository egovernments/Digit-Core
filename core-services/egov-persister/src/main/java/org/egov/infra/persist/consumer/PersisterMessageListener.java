package org.egov.infra.persist.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.egov.tracer.model.ErrorQueueContract;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static org.egov.tracer.constants.TracerConstants.CORRELATION_ID_MDC;

@Service
@Slf4j
public class PersisterMessageListener implements MessageListener<String, Object> {
	
	@Autowired
	private PersistService persistService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CustomKafkaTemplate kafkaTemplate;

	@Value("${audit.persist.kafka.topic}")
	private String persistAuditKafkaTopic;

	@Value("${audit.generate.kafka.topic}")
	private String auditGenerateKafkaTopic;

	@Value("${persister.dead-letter.reprocess.error-topic}")
	private String deadLetterReprocessErrorTopic;

	@Value("${tracer.errorsTopic}")
	private String tracerErrorsTopic;

	@Autowired
	private ErrorQueueProducer errorQueueProducer;

    @Override
	public void onMessage(ConsumerRecord<String, Object> data) {
		String rcvData = null;
		long startTime = System.currentTimeMillis();

		String topic = data.topic();
		String deadLetterTopic = null;
		Object body = null;
		try {
			if (Objects.equals(topic, tracerErrorsTopic)) {
				LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) data.value();
				topic = message.get("source").toString();
				body = message.get("body");
				deadLetterTopic = data.topic();
			} else {
				body = data.value();
			}
			rcvData = objectMapper.writeValueAsString(body);
			persistService.persist(topic, rcvData);
			if(!data.topic().equalsIgnoreCase(persistAuditKafkaTopic)){
				Map<String, Object> producerRecord = new HashMap<>();
				producerRecord.put("topic", topic);
				producerRecord.put("value", body);
				kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
			}
			log.info("Message from topic: {} processed successfully in {} ms.", topic, System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			log.error("Error while persisting message from topic: {}", topic, e);
			if(deadLetterTopic == null) {
				pushToErrorQueue(topic, body, e);
			} else {
				sendErrorMessage(deadLetterReprocessErrorTopic, deadLetterTopic, body, e);
			}
		}
	}

	private void pushToErrorQueue(String topic, Object body, Exception e) {
		try {
			ErrorQueueContract errorQueueContract = ErrorQueueContract.builder()
					.id(UUID.randomUUID().toString())
					.source(topic)
					.body(body)
					.ts(System.currentTimeMillis())
					.message(e.getMessage())
					.exception(Arrays.asList(e.getStackTrace()))
					.correlationId(MDC.get(CORRELATION_ID_MDC))
					.build();
			errorQueueProducer.sendMessage(errorQueueContract);
			log.info("Message pushed to error queue for topic: {}", topic);
		} catch (Exception ex) {
			log.error("Failed to push message to error queue for topic: {}", topic, ex);
		}
	}

	public void sendErrorMessage(String errorTopic, String topic, Object body, Exception ex) {
		ErrorQueueContract errorQueueContract = ErrorQueueContract.builder()
				.id(UUID.randomUUID().toString())
				.source(topic)
				.body(body)
				.ts(System.currentTimeMillis())
				.message(ex.getMessage())
				.exception(Arrays.asList(ex.getStackTrace()))
				.correlationId(MDC.get(CORRELATION_ID_MDC))
				.build();
		try {
			log.info("Sending message to topic - " + errorTopic);
			kafkaTemplate.send(errorTopic, errorQueueContract);
		} catch (SerializationException serializationException) {
			log.info("SerializationException exception occurred while sending exception to error queue");
			try {
				kafkaTemplate.send(errorTopic, objectMapper.writeValueAsString(errorQueueContract));
			} catch (JsonProcessingException e) {
				log.error("exception occurred while converting ErrorQueueContract to json string", e);
			}
		} catch (Exception e) {
			log.error("exception occurred while sending exception to error queue", e);
		}
	}

}
