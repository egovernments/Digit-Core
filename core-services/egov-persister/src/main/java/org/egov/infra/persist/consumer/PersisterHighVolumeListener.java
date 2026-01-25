package org.egov.infra.persist.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dedicated batch listener for high-volume topics.
 * Bypasses parallel topic processing since all messages are from a single topic.
 * This provides better throughput for high-volume single-topic containers.
 */
@Service
@Slf4j
public class PersisterHighVolumeListener implements BatchMessageListener<String, Object> {

    @Autowired
    private PersisterBatchProcessingService batchProcessingService;

    @Override
    public void onMessage(List<ConsumerRecord<String, Object>> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        // All records are from the same topic in high-volume containers
        String topic = dataList.get(0).topic();
        batchProcessingService.processSingleTopicBatch(topic, dataList);
    }
}