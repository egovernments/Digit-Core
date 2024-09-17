package org.egov.tracer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties("tracer")
@Configuration
public class TracerProperties {

    // Enable request body and query parameters logging
    private boolean requestLoggingEnabled;

    // Enable kafka message body logging on send
    private boolean kafkaMessageLoggingEnabled;

    // Enable request and response body logging on all rest template calls
    private boolean restTemplateDetailedLoggingEnabled;

    // Enable errors publishing on a kafka topic
    private boolean errorsPublish;

    // Topic to which errors need to be published
    private String errorsTopic;

    // Topic to which error details need to be published
    private String errorDetailsTopic;

    // Exclusion list for tracer filter
    private String filterSkipPattern;

    // Flag to enable exceptions caught on tracer interceptor to be persisted on ElasticSearch.
    private boolean shouldPublishErrorDetailsFlag;

}

