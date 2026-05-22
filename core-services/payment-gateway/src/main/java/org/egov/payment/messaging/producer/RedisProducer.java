package org.egov.payment.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class RedisProducer implements Producer {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void push(String topic, Object message) {
        redisTemplate.convertAndSend(topic, message);
        log.debug("Message published to Redis topic {}", topic);
    }
}
