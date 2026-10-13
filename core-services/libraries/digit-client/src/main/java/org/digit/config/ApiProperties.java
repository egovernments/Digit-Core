package org.digit.config;

import org.springframework.beans.factory.annotation.Value;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiProperties {
    @Value(value="${digit.services.account.base-url:http://localhost:8080}")
    private String accountServiceUrl;
    @Value(value="${digit.services.boundary.base-url:http://localhost:8080}")
    private String boundaryServiceUrl;
    @Value(value="${digit.services.workflow.base-url:http://localhost:8085}")
    private String workflowServiceUrl;
    @Value(value="${digit.services.idgen.base-url:http://localhost:8100}")
    private String idgenServiceUrl;
    @Value(value="${digit.services.notification.base-url:http://localhost:8091}")
    private String notificationServiceUrl;
    @Value(value="${digit.services.individual.base-url:http://localhost:8999}")
    private String individualServiceUrl;
    @Value(value="${digit.services.filestore.base-url:http://localhost:8080}")
    private String filestoreServiceUrl;
    @Value(value="${digit.services.mdms.base-url:http://localhost:8080}")
    private String mdmsServiceUrl;
    @Value(value="${digit.services.registry.base-url:http://localhost:8085}")
    private String registryServiceUrl;
    @Value(value="${digit.services.accesscontrol.base-url:http://localhost:8080}")
    private String accessControlServiceUrl;
    @Value(value="${digit.services.localization.base-url:http://localhost:8080}")
    private String localizationServiceUrl;
    @Value(value="${digit.services.otp.base-url:http://localhost:8080}")
    private String otpServiceUrl;
    @Value(value="${digit.services.urlshortener.base-url:http://localhost:8080}")
    private String urlShortenerServiceUrl;
    @Value(value="${digit.services.employee.base-url:http://localhost:8080}")
    private String employeeServiceUrl;
    @Value(value="${digit.services.billing.base-url:http://localhost:8080}")
    private String billingServiceUrl;
    @Value(value="${digit.services.timeout.connect:5000}")
    private int connectTimeout;
    @Value(value="${digit.services.timeout.read:30000}")
    private int readTimeout;
    @Value(value="${digit.services.retry.max-attempts:3}")
    private int maxRetryAttempts;
    @Value(value="${digit.services.retry.delay:1000}")
    private long retryDelay;
    @Value(value="${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value(value="${spring.data.redis.port:6380}")
    private int redisPort;
    @Value(value="${spring.cache.type:none}")
    private String cacheType;
}