package org.egov.tracer.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;

/** Rebuilds MDC from record headers before each record and clears it after (per-record, with cleanup). */
public class MdcRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        TracerKafkaMdcUtil.applyMdcFromRecord(record);
        return record;
    }

    @Override
    public void success(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
        TracerKafkaMdcUtil.clearMdc();
    }

    @Override
    public void failure(ConsumerRecord<K, V> record, Exception exception, Consumer<K, V> consumer) {
        TracerKafkaMdcUtil.clearMdc();
    }
}
