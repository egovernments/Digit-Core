package org.egov.infra.indexer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.infra.indexer.service.ReindexService;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.web.contract.ReindexRequest;
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
public class ReindexMessageListener implements MessageListener<String, String> {

	@Autowired
	private IndexerUtils indexerUtils;
	
	@Autowired
	private ReindexService reindexService;
	
	@Autowired
	private IndexerService indexerService;
	
	@Value("${egov.core.reindex.topic.name}")
	private String reindexTopic;

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
		if(data.topic().equals(reindexTopic)) {
			try {
				ReindexRequest reindexRequest = mapper.readValue(data.value(), ReindexRequest.class);
				reindexService.beginReindex(reindexRequest);
			} catch (Exception e) {
				log.error("Couldn't parse reindex request: ", e);
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
