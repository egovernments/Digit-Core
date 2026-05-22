package org.egov.payment.messaging.producer;

public interface Producer {

    void push(String topic, Object value);
}
