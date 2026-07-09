package org.egov.tracer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.egov.tracer.constants.TracerConstants.*;
import static org.springframework.util.StringUtils.isEmpty;

/** Carries correlationId + tenantId across Kafka via record headers, and rebuilds MDC on consume. */
@Slf4j
public final class TracerKafkaMdcUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TracerKafkaMdcUtil() {
    }

    /** Copy correlationId + tenantId from MDC into record headers (only if present and not already set). */
    public static void stampHeadersFromMdc(ProducerRecord<?, ?> producerRecord) {
        addHeaderIfAbsent(producerRecord, CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_MDC));
        addHeaderIfAbsent(producerRecord, TENANT_ID_HEADER, MDC.get(TENANTID_MDC));
    }

    /** Set MDC from record headers (correlationId falls back to body); removes the key when absent. */
    public static void applyMdcFromRecord(ConsumerRecord<?, ?> consumerRecord) {
        String correlationId = headerValue(consumerRecord, CORRELATION_ID_HEADER);
        if (isEmpty(correlationId))
            correlationId = correlationIdFromBody(consumerRecord.value());
        setOrRemove(CORRELATION_ID_MDC, correlationId);

        setOrRemove(TENANTID_MDC, headerValue(consumerRecord, TENANT_ID_HEADER));
    }

    /** Remove the tracing keys from MDC (call after each record; listener threads are reused). */
    public static void clearMdc() {
        MDC.remove(CORRELATION_ID_MDC);
        MDC.remove(TENANTID_MDC);
    }

    private static void addHeaderIfAbsent(ProducerRecord<?, ?> producerRecord, String headerName, String value) {
        if (!isEmpty(value) && producerRecord.headers().lastHeader(headerName) == null)
            producerRecord.headers().add(headerName, value.getBytes(StandardCharsets.UTF_8));
    }

    private static String headerValue(ConsumerRecord<?, ?> consumerRecord, String headerName) {
        Header header = consumerRecord.headers().lastHeader(headerName);
        if (isNull(header) || isNull(header.value()))
            return null;
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private static void setOrRemove(String key, String value) {
        if (isEmpty(value))
            MDC.remove(key);
        else
            MDC.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private static String correlationIdFromBody(Object value) {
        try {
            Map<String, Object> requestMap = objectMapper.convertValue(value, Map.class);
            Object requestInfo = requestMap.containsKey(REQUEST_INFO_FIELD_NAME_IN_JAVA_CLASS_CASE)
                    ? requestMap.get(REQUEST_INFO_FIELD_NAME_IN_JAVA_CLASS_CASE)
                    : requestMap.get(REQUEST_INFO_IN_CAMEL_CASE);
            if (isNull(requestInfo))
                return null;
            if (requestInfo instanceof Map)
                return (String) ((Map) requestInfo).get(CORRELATION_ID_FIELD_NAME);
        } catch (Exception ignored) {
        }
        return null;
    }
}
