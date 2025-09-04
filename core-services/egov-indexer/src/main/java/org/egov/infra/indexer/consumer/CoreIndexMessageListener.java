package org.egov.infra.indexer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.egov.tracer.model.ErrorQueueContract;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import static org.egov.infra.indexer.util.IndexerConstants.TENANTID_MDC_STRING;
import static org.egov.infra.indexer.util.IndexerConstants.CORRELATION_ID_MDC_STRING;

import com.jayway.jsonpath.JsonPath;

@Service
@Slf4j
public class CoreIndexMessageListener implements MessageListener<String, String> {

	@Autowired
	private IndexerService indexerService;
	
	@Autowired
	private ErrorQueueProducer errorQueueProducer;

	@Value("${egov.statelevel.tenantId}")
	private  String stateLevelTenantId ;
	
	@Value("${tracer.errorsTopic}")
	private String errorTopic;
	
	@Value("${tracer.errorsPublish}")
	private boolean publishErrors;

	@Override
	/**
	 * Messages listener which acts as consumer. This message listener is injected
	 * inside a kafkaContainer. This consumer is a start point to the following
	 * index jobs: 1. Re-index 2. Legacy Index 3. PGR custom index 4. PT custom
	 * index 5. Core indexing
	 */
	public void onMessage(ConsumerRecord<String, String> data) {
		log.info("Topic from CoreIndexMessageListener: " + data.topic());
		
		// Extract correlation ID from message body
		String correlationId = extractCorrelationId(data.value());
		if (correlationId != null) {
			MDC.put(CORRELATION_ID_MDC_STRING, correlationId);
		}
		
		// Adding in MDC so that tracer can add it in header
		MDC.put(TENANTID_MDC_STRING, stateLevelTenantId);
		
		try {
			indexerService.esIndexer(data.topic(), data.value());
		} catch (Exception e) {
			log.error("error while indexing: ", e);
			
			// Send directly to DLQ using tracer's ErrorQueueProducer
			if (publishErrors) {
				try {
					sendToDLQ(data.value(), e, correlationId, stateLevelTenantId);
					log.info("Successfully sent failed message to DLQ topic: {}", errorTopic);
				} catch (Exception dlqException) {
					log.error("Failed to send message to DLQ: ", dlqException);
                    // Log both the original exception and DLQ failure
                    log.error("Original indexing error: ", e);
				}
			}
            // Always re-throw to maintain consistent error handling behavior
            // The tracer at container level will handle retry/DLQ based on configuration
            throw new RuntimeException("Failed to index message", e);
		}
	}
	
	/**
	 * Send failed message to DLQ using tracer's ErrorQueueProducer
	 */
	private void sendToDLQ(String messageBody, Exception exception, String correlationId, String tenantId) {
		try {
			// Create ErrorQueueContract using the correct field names from tracer library
			StackTraceElement[] elements = exception.getStackTrace();
			
			ErrorQueueContract errorContract = ErrorQueueContract.builder()
					.id(UUID.randomUUID().toString())
					.correlationId(correlationId)
					.body(messageBody)
					.source("egov-indexer")
					.ts(new Date().getTime())
					.exception(Arrays.asList(elements))
					.message(exception.getMessage())
					.build();
			
			// Send to DLQ using tracer's ErrorQueueProducer
			errorQueueProducer.sendMessage(errorContract);
		} catch (Exception e) {
			log.error("Failed to create or send ErrorQueueContract to DLQ", e);
			throw e;
		}
	}
	
	/**
	 * Extract correlation ID from Kafka message body
	 */
	private String extractCorrelationId(String messageBody) {
		try {
			// Try to extract correlation ID from RequestInfo
			return JsonPath.read(messageBody, "$.RequestInfo.correlationId");
		} catch (Exception e) {
			try {
				// Fallback: try to extract from different path
				return JsonPath.read(messageBody, "$.correlationId");
			} catch (Exception ex) {
				log.debug("Could not extract correlation ID from message: {}", ex.getMessage());
				return null;
			}
		}
	}

}
