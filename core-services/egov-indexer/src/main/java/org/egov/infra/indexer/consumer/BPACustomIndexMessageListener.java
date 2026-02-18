package org.egov.infra.indexer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.custom.bpa.BPACustomDecorator;
import org.egov.infra.indexer.custom.bpa.BPARequest;
import org.egov.infra.indexer.custom.bpa.EnrichedBPARequest;
import org.egov.infra.indexer.custom.pt.PTCustomDecorator;
import org.egov.infra.indexer.custom.pt.PropertyRequest;
import org.egov.infra.indexer.producer.IndexerProducer;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.util.DLQHandler;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import static org.egov.infra.indexer.util.IndexerConstants.TENANTID_MDC_STRING;

@Service
@Slf4j
public class BPACustomIndexMessageListener implements MessageListener<String, String> {

    @Autowired
    private IndexerService indexerService;

    @Autowired
    private IndexerUtils indexerUtils;

    @Autowired
    private BPACustomDecorator bpaCustomDecorator;

    @Autowired
    private IndexerProducer  indexerProducer;

    @Autowired
    private DLQHandler dlqHandler;


    @Override
    /**
     * Messages listener which acts as consumer. This message listener is injected
     * inside a kafkaContainer. This consumer is a start point to the following
     * index jobs: 1. Re-index 2. Legacy Index 3. PGR custom index 4. PT custom
     * index 5. Core indexing
     */
    public void onMessage(ConsumerRecord<String, String> data) {
        log.info("Topic from BPACustomIndexMessageListener: " + data.topic());
        
        ObjectMapper mapper = indexerUtils.getObjectMapperWithNull();
        String tenantId = null;
        
        try {
            BPARequest bpaRequest = mapper.readValue(data.value(), BPARequest.class);
            if (bpaRequest.getBPA() != null) {
                tenantId = bpaRequest.getBPA().getTenantId();
            }
            
            // Adding in MDC so that tracer can add it in header
            if (tenantId != null) {
                MDC.put(TENANTID_MDC_STRING, tenantId);
            }

            EnrichedBPARequest enrichedBPARequest = bpaCustomDecorator.transformData(bpaRequest);
            indexerService.esIndexer(data.topic(), mapper.writeValueAsString(enrichedBPARequest));
        } catch (Exception e) {
            try {
                dlqHandler.handleError(data.value(), e, "BPACustomIndexMessageListener");
            } catch (RuntimeException dlqException) {
                log.error("Failed to handle error in DLQ for BPACustomIndexMessageListener", dlqException);
            }
        } finally {
            // Always clear MDC to prevent data leakage across thread reuse
            if (tenantId != null) {
                MDC.remove(TENANTID_MDC_STRING);
            }
        }
    }
}
