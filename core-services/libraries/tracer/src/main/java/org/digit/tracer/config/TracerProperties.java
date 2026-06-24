package org.digit.tracer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "digit.tracer")
public record TracerProperties(
    boolean requestLoggingEnabled,
    boolean kafkaMessageLoggingEnabled,
    boolean errorsPublish,
    String errorsTopic,
    String errorDetailsTopic,
    boolean shouldPublishErrorDetails,
    String filterSkipPattern,
    PubSubProperties pubsub,
    OtelProperties otel
) {
    public TracerProperties {
        errorsTopic           = errorsTopic           != null ? errorsTopic           : "digit-error";
        errorDetailsTopic     = errorDetailsTopic     != null ? errorDetailsTopic     : "error-details-indexer-topic";
        filterSkipPattern     = filterSkipPattern     != null ? filterSkipPattern     :
            "/api-docs.*|/health|/info|/metrics.*|/prometheus|/actuator.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|/favicon.ico";
    }

    public record PubSubProperties(
        String type,          // "kafka" or "redis"
        KafkaProperties kafka,
        RedisProperties redis
    ) {
        public record KafkaProperties(
            String brokers,
            String consumerGroup,
            int defaultPartitions,
            short defaultReplicationFactor
        ) {
            public KafkaProperties {
                brokers                  = brokers                  != null ? brokers                  : "localhost:9092";
                consumerGroup            = consumerGroup            != null ? consumerGroup            : "digit-default";
                defaultPartitions        = defaultPartitions        > 0    ? defaultPartitions        : 3;
                defaultReplicationFactor = defaultReplicationFactor > 0    ? defaultReplicationFactor : 1;
            }
        }

        public record RedisProperties(
            String address,
            String consumerGroup
        ) {
            public RedisProperties {
                address       = address       != null ? address       : "localhost:6379";
                consumerGroup = consumerGroup != null ? consumerGroup : "digit-default";
            }
        }
    }

    public record OtelProperties(
        boolean enabled,
        String endpoint,
        double samplingRatio,
        String serviceName,
        String serviceVersion
    ) {
        public OtelProperties {
            endpoint      = endpoint      != null ? endpoint      : "http://localhost:4318";
            samplingRatio = samplingRatio > 0    ? samplingRatio : 1.0;
            serviceName   = serviceName   != null ? serviceName   : "unknown-service";
            serviceVersion = serviceVersion != null ? serviceVersion : "unknown";
        }
    }
}
