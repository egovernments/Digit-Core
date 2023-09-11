package org.egov.infra.indexer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.infra.indexer.service.LegacyIndexService;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
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
public class LegacyIndexMessageListener implements MessageListener<String, String> {


	@Autowired
	private IndexerUtils indexerUtils;
	
	@Autowired
	private LegacyIndexService legacyIndexService;
	
	@Autowired
	private IndexerService indexerService;
	
	@Value("${egov.core.legacyindex.topic.name}")
	private String legacyIndexTopic;

	@Value("${egov.statelevel.tenantId}")
	private  String stateLevelTenantId ;

	@Override
	/**
	 * Messages listener which acts as consumer. This message listener is injected inside a kafkaContainer.
	 * This consumer is a start point to the following index jobs:
	 * 1. Re-index
	 * 2. Legacy Index
	 * 3. PGR custom index
	 * 4. PT custom index
	 * 5. Core indexing
	 */
	public void onMessage(ConsumerRecord<String, String> data) {
		log.info("Topic: " + data.topic());
		// Adding in MDC so that tracer can add it in header
		MDC.put(TENANTID_MDC_STRING, stateLevelTenantId );
		ObjectMapper mapper = indexerUtils.getObjectMapper();
		if(data.topic().equals(legacyIndexTopic)) {
			try {
				LegacyIndexRequest legacyIndexRequest = mapper.readValue(data.value(), LegacyIndexRequest.class);
				legacyIndexService.beginLegacyIndex(legacyIndexRequest);
			}catch(Exception e) {
				log.error("Couldn't parse legacyindex request: ", e);
			}
		}else {
			try {
				indexerService.esIndexer(data.topic(), data.value());
			} catch (Exception e) {
				log.error("error while indexing: ", e);
			}
		}
	}

}
