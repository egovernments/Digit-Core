package org.digit.tracer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.digit.tracer.db.TracedDataSource;
import org.digit.tracer.error.ErrorQueueProducer;
import org.digit.tracer.error.ExceptionAdvice;
import org.digit.tracer.http.RequestTracingFilter;
import org.digit.tracer.logger.StructuredLogger;
import org.digit.tracer.observability.ObservabilityMetrics;
import org.digit.tracer.pubsub.KafkaPubSubClient;
import org.digit.tracer.pubsub.PubSubClient;
import org.digit.tracer.pubsub.RedisPubSubClient;
import org.digit.tracer.validation.JsonSchemaValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;

@AutoConfiguration(after = {JacksonAutoConfiguration.class, KafkaAutoConfiguration.class})
@EnableConfigurationProperties(TracerProperties.class)
public class TracerAutoConfiguration {

    // --- Jackson ObjectMapper ---
    // Spring Boot 4's JacksonAutoConfiguration now produces a Jackson 3 (tools.jackson) JsonMapper
    // and no longer publishes a Jackson 2 (com.fasterxml) ObjectMapper bean. The tracer's components
    // are built on Jackson 2, so we provide one here — backing off if the application already defines
    // its own — mirroring Spring Boot's previous auto-configured defaults (registered modules,
    // ISO-8601 dates).
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ObservabilityMetrics observabilityMetrics(io.micrometer.core.instrument.MeterRegistry registry) {
        return new ObservabilityMetrics(registry);
    }

    @Bean
    public StructuredLogger structuredLogger(ObjectMapper objectMapper) {
        return StructuredLogger.forName("org.digit.tracer", objectMapper);
    }

    @Bean
    public JsonSchemaValidator jsonSchemaValidator(ObjectMapper objectMapper) {
        return new JsonSchemaValidator(objectMapper);
    }

    // --- HTTP filter ---

    @Bean
    @ConditionalOnWebApplication
    public FilterRegistrationBean<RequestTracingFilter> requestTracingFilter(
            TracerProperties properties,
            ObservabilityMetrics metrics) {
        FilterRegistrationBean<RequestTracingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new RequestTracingFilter(properties, metrics));
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

    // --- Error queue producer — only wired when Kafka class is present ---

    @Bean
    @ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ErrorQueueProducer errorQueueProducer(
            ObjectProvider<KafkaTemplate> kafkaTemplateProvider,
            TracerProperties properties,
            ObjectMapper objectMapper) {
        KafkaTemplate kt = kafkaTemplateProvider.getIfAvailable();
        if (kt == null) return null;
        return new ErrorQueueProducer(kt, properties, objectMapper);
    }

    // --- Exception advice — always registered; null ErrorQueueProducer means no Kafka publishing ---

    @Bean
    @ConditionalOnWebApplication
    public ExceptionAdvice exceptionAdvice(@Nullable ErrorQueueProducer errorQueueProducer) {
        return new ExceptionAdvice(errorQueueProducer);
    }

    // --- DB tracing ---

    @Bean("tracedDataSource")
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "digit.tracer.db.tracing-enabled", havingValue = "true", matchIfMissing = true)
    public TracedDataSource tracedDataSource(DataSource dataSource, ObservabilityMetrics metrics) {
        return new TracedDataSource(dataSource, metrics);
    }

    // --- PubSub: Kafka ---

    @Bean
    @ConditionalOnProperty(name = "digit.tracer.pubsub.type", havingValue = "kafka")
    public PubSubClient kafkaPubSubClient(
            TracerProperties properties,
            ObservabilityMetrics metrics,
            ObjectMapper objectMapper) {
        var client = new KafkaPubSubClient(properties.pubsub().kafka(), metrics, objectMapper);
        client.connect();
        return client;
    }

    // --- PubSub: Redis ---

    @Bean
    @ConditionalOnProperty(name = "digit.tracer.pubsub.type", havingValue = "redis")
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    @SuppressWarnings("unchecked")
    public PubSubClient redisPubSubClient(
            TracerProperties properties,
            ObjectProvider<RedisTemplate> redisTemplateProvider,
            ObservabilityMetrics metrics,
            ObjectMapper objectMapper) {
        RedisTemplate<String, String> rt = redisTemplateProvider.getIfAvailable();
        if (rt == null) return null;
        var client = new RedisPubSubClient(properties.pubsub().redis(), rt, metrics, objectMapper);
        client.connect();
        return client;
    }
}
