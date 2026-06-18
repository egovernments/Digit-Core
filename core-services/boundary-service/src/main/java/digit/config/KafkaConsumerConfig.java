package digit.config;

import digit.kafka.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.BackOffHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerPausingBackOffHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerContainerPauseService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configures the Kafka listener container so a job that throws (only transient failures do — see
 * {@link digit.kafka.TransientRelationshipException}) is redelivered with backoff rather than lost.
 *
 * <p>The backoff is <b>non-blocking</b>: {@link ContainerPausingBackOffHandler} pauses the partition
 * and schedules a resume, so the consumer keeps polling (the session stays alive) and the retry
 * window never breaches {@code max.poll.interval.ms} regardless of the ceiling. Transient failures
 * (parent/entity persistence lag, transient DB errors) are retried up to the ceiling; after that the
 * job is dead-lettered to the bulk-create error topic (best-effort, so a failing error topic cannot
 * cause an infinite reprocessing loop).</p>
 */
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    // ~5 minute redelivery ceiling (150 attempts x 2s). Because the backoff is non-blocking, the
    // ceiling can be generous without risking consumer eviction.
    private static final long RETRY_INTERVAL_MS = 2000L;
    private static final long MAX_RETRY_ATTEMPTS = 150L;

    @Bean
    public ThreadPoolTaskScheduler kafkaBackOffScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("kafka-backoff-");
        // Do NOT call initialize() here: as a Spring @Bean, the container invokes afterPropertiesSet()
        // (InitializingBean) which initializes it exactly once. A manual initialize() would create and
        // orphan a second, never-shut-down thread pool.
        return scheduler;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(Producer producer, ApplicationProperties applicationProperties,
                                                 KafkaListenerEndpointRegistry registry, ThreadPoolTaskScheduler kafkaBackOffScheduler) {
        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            log.error("Bulk boundary relationship job exhausted redelivery attempts; dead-lettering to error topic", exception);
            try {
                producer.push(applicationProperties.getBulkCreateBoundaryRelationshipErrorTopic(), record.value());
            } catch (Exception e) {
                // Best-effort: a failed dead-letter publish must not block the offset commit or cause
                // an infinite reprocessing loop. Record is dropped to the log of last resort.
                log.error("Failed to dead-letter bulk boundary relationship job to error topic", e);
            }
        };
        // Non-blocking backoff: pause the partition between attempts (keeps the consumer polling).
        BackOffHandler backOffHandler = new ContainerPausingBackOffHandler(new ListenerContainerPauseService(registry, kafkaBackOffScheduler));
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRY_ATTEMPTS), backOffHandler);
        handler.setCommitRecovered(true);
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // Applies all spring.kafka.* consumer/listener settings (concurrency, deserializers, auto-offset-reset).
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
