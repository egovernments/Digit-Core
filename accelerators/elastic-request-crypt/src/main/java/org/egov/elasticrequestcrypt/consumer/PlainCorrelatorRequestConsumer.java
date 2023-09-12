package org.egov.elasticrequestcrypt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.elasticrequestcrypt.models.HttpRequestLog;
import org.egov.elasticrequestcrypt.service.CorrelatorRequestProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class PlainCorrelatorRequestConsumer {

    private CorrelatorRequestProcessingService correlatorRequestProcessingService;

    private ObjectMapper mapper;

    @Autowired
    public PlainCorrelatorRequestConsumer(CorrelatorRequestProcessingService correlatorRequestProcessingService, ObjectMapper mapper) {
        this.correlatorRequestProcessingService = correlatorRequestProcessingService;
        this.mapper = mapper;
    }

    /**
     * This kafka consumer request handler listens on correlator requests emitted by zuul and processes them.
     * @param record
     * @param topic
     */
    @KafkaListener(topics = { "${process.correlator.requests.kafka.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            // Convert incoming payload to HttpRequestLog.
            HttpRequestLog httpRequestLog = mapper.convertValue(record, HttpRequestLog.class);

            // Process the http request log which was consumed.
            correlatorRequestProcessingService.processHttpLogRequest(httpRequestLog);
        } catch (Exception ex) {
            StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
                    .append("on topic: ").append(topic);
            log.error(builder.toString(), ex);
        }
    }

}
