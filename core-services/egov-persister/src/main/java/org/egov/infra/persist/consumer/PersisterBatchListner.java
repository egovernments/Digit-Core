package org.egov.infra.persist.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Batch message listener for Kafka.
 * Delegates processing to PersisterBatchProcessingService.
 */
@Service
@Slf4j
public class PersisterBatchListner implements BatchMessageListener<String, Object> {

    @Autowired
    private PersisterBatchProcessingService batchProcessingService;

    @Override
    public void onMessage(List<ConsumerRecord<String, Object>> dataList) {
        batchProcessingService.processBatch(dataList);
    }
}