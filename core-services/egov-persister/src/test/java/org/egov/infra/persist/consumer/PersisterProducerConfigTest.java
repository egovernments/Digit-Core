package org.egov.infra.persist.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(classes = {PersisterProducerConfig.class, KafkaProperties.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "persister.dead-letter.reprocess.error-topic=test-persister-deadletter")
class PersisterProducerConfigTest {
    @MockBean
    private CustomKafkaTemplate<String, Object> customKafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private PersisterProducerConfig persisterProducerConfig;

    @Test
    void testProducerConfigs() {
        assertEquals(3, this.persisterProducerConfig.producerConfigs().size());
    }

    @Test
    void testProducerFactory() {
        assertTrue(this.persisterProducerConfig
                .producerFactory() instanceof org.springframework.kafka.core.DefaultKafkaProducerFactory);
    }

    @Test
    void testKafkaTemplate() {

        this.persisterProducerConfig.kafkaTemplate();
    }
}

