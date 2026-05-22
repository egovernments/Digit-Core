package org.egov.payment.messaging.producer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpMessageProducer implements Producer {

    @Override
    public void push(String topic, Object message) {
        log.debug("Message broker disabled. Skipping publish for topic {}", topic);
    }
}
