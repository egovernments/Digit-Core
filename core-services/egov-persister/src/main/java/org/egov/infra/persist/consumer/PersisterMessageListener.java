package org.egov.infra.persist.consumer;

import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

	@Autowired
	private ErrorQueueProducer errorQueueProducer;

    @Override
	public void onMessage(ConsumerRecord<String, Object> data) {
		String rcvData = null;
		long startTime = System.currentTimeMillis();

		try {
			rcvData = objectMapper.writeValueAsString(data.value());
			persistService.persist(data.topic(),rcvData);
			if(!data.topic().equalsIgnoreCase(persistAuditKafkaTopic)){
				Map<String, Object> producerRecord = new HashMap<>();
				producerRecord.put("topic", data.topic());
				producerRecord.put("value", data.value());
				kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
			}
			log.info("Message processed successfully in {} ms.", System.currentTimeMillis() - startTime);
		} catch (Exception e) {
			log.error("Error while persisting message from topic: {}", data.topic(), e);
			pushToErrorQueue(data.topic(), data.value(), e);
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

}
