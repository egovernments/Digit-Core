package org.egov.infra.persist.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.persist.service.PersistService;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.tracer.kafka.ErrorQueueProducer;
import org.egov.tracer.model.ErrorQueueContract;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class PersisterBatchListner implements BatchMessageListener<String, Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersistService persistService;

    @Autowired
    private CustomKafkaTemplate kafkaTemplate;

    @Autowired
    private ErrorQueueProducer errorQueueProducer;

    @Value("${audit.persist.kafka.topic}")
    private String persistAuditKafkaTopic;

    @Value("${audit.generate.kafka.topic}")
    private String auditGenerateKafkaTopic;

    private static final String CORRELATION_ID_MDC = "CORRELATION_ID";

    @Override
    public void onMessage(List<ConsumerRecord<String, Object>> dataList) {

        long startTime = System.currentTimeMillis();
        int totalRecordsSuccess = 0;
        int totalRecordsFailed = 0;

        Map<String,List<String>> topicTorcvDataList = new HashMap<>();

            dataList.forEach(data -> {
                try {
                    if(!topicTorcvDataList.containsKey(data.topic())){
                        List<String> rcvDataList= new LinkedList<>();
                        rcvDataList.add(objectMapper.writeValueAsString(data.value()));
                        topicTorcvDataList.put(data.topic(),rcvDataList);
                    }
                    else {
                        topicTorcvDataList.get(data.topic()).add(objectMapper.writeValueAsString(data.value()));
                    }
                }
                catch (JsonProcessingException e) {
                    log.error("Failed to serialize incoming message", e);
                    pushToErrorQueue(data.topic(), data.value(), e);
                }
            });

        for(Map.Entry<String,List<String>> entry : topicTorcvDataList.entrySet()){
            try {
                persistService.persist(entry.getKey(),entry.getValue());
                if(!entry.getKey().equalsIgnoreCase(persistAuditKafkaTopic)){
                    Map<String, Object> producerRecord = new HashMap<>();
                    producerRecord.put("topic", entry.getKey());
                    producerRecord.put("value", entry.getValue());
                    kafkaTemplate.send(auditGenerateKafkaTopic, producerRecord);
                }
                totalRecordsSuccess += entry.getValue().size();
            } catch (Exception e) {
                log.error("Error while persisting message from topic: {}", entry.getKey(), e);
                entry.getValue().forEach(data -> {
                    pushToErrorQueue(entry.getKey(), data, e);
                });
                totalRecordsFailed += entry.getValue().size();
            }
        }

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("Total messages persisted successfully: {}, failed: {}, time taken: {} ms", totalRecordsSuccess, totalRecordsFailed, timeTaken);

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
