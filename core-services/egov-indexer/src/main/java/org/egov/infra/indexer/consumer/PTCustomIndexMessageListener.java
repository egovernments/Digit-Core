package org.egov.infra.indexer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.custom.pt.PTCustomDecorator;
import org.egov.infra.indexer.custom.pt.PropertyRequest;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.infra.indexer.util.DLQHandler;
import org.egov.infra.indexer.util.IndexerUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import static org.egov.infra.indexer.util.IndexerConstants.TENANTID_MDC_STRING;

@Service
@Slf4j
public class PTCustomIndexMessageListener implements MessageListener<String, String> {

	@Autowired
	private IndexerService indexerService;

	@Autowired
	private IndexerUtils indexerUtils;

	@Autowired
	private PTCustomDecorator ptCustomDecorator;

	@Autowired
	private DLQHandler dlqHandler;

	@Value("${egov.indexer.pt.update.topic.name}")
	private String ptUpdateTopic;

	@Value("${egov.statelevel.tenantId}")
	private  String stateLevelTenantId ;
	@Override
	/**
	 * Messages listener which acts as consumer. This message listener is injected
	 * inside a kafkaContainer. This consumer is a start point to the following
	 * index jobs: 1. Re-index 2. Legacy Index 3. PGR custom index 4. PT custom
	 * index 5. Core indexing
	 */
	public void onMessage(ConsumerRecord<String, String> data) {
		ObjectMapper mapper = indexerUtils.getObjectMapper();
		
		String tenantId = null;
		try {
			PropertyRequest propertyRequest = mapper.readValue(data.value(), PropertyRequest.class);
			// Adding in MDC so that tracer can add it in header
			tenantId = propertyRequest.getProperties().get(0).getTenantId();
			MDC.put(TENANTID_MDC_STRING, tenantId);

			if (data.topic().equals(ptUpdateTopic))
				propertyRequest = ptCustomDecorator.dataTransformForPTUpdate(propertyRequest);
			propertyRequest.setProperties(ptCustomDecorator.transformData(propertyRequest.getProperties()));
			indexerService.esIndexer(data.topic(), mapper.writeValueAsString(propertyRequest));
		} catch (Exception e) {
			dlqHandler.handleError(data.value(), e, "PTCustomIndexMessageListener");
		} finally {
			// Always clear MDC to prevent data leakage across thread reuse
			if (tenantId != null) {
				MDC.remove(TENANTID_MDC_STRING);
			}
		}
	}

}
