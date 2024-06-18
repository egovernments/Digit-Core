package org.selco.e4h.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.selco.e4h.config.ServiceConstants;
import org.selco.e4h.service.UpdateService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventListener implements MessageListener<String, String> {

	@Autowired
	private UpdateService indexerService;

	@Value("${egov.statelevel.tenantId}")
	private String stateLevelTenantId;

	@Override
	/**
	 * Messages listener which acts as consumer. This message listener is injected
	 * inside a kafkaContainer. This consumer is a start point to the following
	 * index jobs: 1. Re-index 2. Legacy Index 3. PGR custom index 4. PT custom
	 * index 5. Core indexing
	 */
	public void onMessage(ConsumerRecord<String, String> data) {
		log.info("Topic from CoreIndexMessageListener: " + data.topic());
		// Adding in MDC so that tracer can add it in header
		MDC.put(ServiceConstants.TENANTID_MDC_STRING, stateLevelTenantId);
		try {
			indexerService.updateEsDoc(data.topic(), data.value());
		} catch (Exception e) {
			log.error("error while updating ES document: ", e);
		}
	}

}
