package io.digit.requestvalidation.autoconfigure;

import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.egov.requestvalidation.core.ContentDetector;
import org.egov.requestvalidation.core.JsonDocumentInspector;
import org.egov.requestvalidation.web.BodyAdviceRegistrar;
import org.egov.requestvalidation.web.CoverageReport;
import org.egov.requestvalidation.web.RequestValidationMvcConfigurer;
import org.egov.requestvalidation.web.ScalarParameterInterceptor;
import org.egov.requestvalidation.web.ServletSupport;
import org.egov.requestvalidation.web.StructuredBodyAdvice;
import org.egov.requestvalidation.web.ValidationAuditLogger;
import org.egov.requestvalidation.web.ValidationReporter;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Outside both org.egov and digit scans; registered only by Boot's AutoConfiguration.imports (Boot 2.7+/3.x)
 * and spring.factories (Boot 1.5-2.6). Written against the Spring Boot 1.5 API so that the same class works
 * on Boot 1.5, 2.x and 3.x: plain @Configuration (no @AutoConfiguration, no proxyBeanMethods; no bean method
 * here calls another) and a servlet-application condition equivalent to Boot's.
 */
@Configuration
@Conditional(ServletWebApplicationCondition.class)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "egov.request-validation", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(RequestValidationProperties.class)
public class RequestValidationAutoConfiguration {
    /** javax.servlet (Boot 1.5/2.x) or jakarta.servlet (Boot 3.x), detected from the running Spring MVC. */
    @Bean
    @ConditionalOnMissingBean
    ServletSupport requestValidationServletSupport() {
        return ServletSupport.forRuntime();
    }

    @Bean
    @ConditionalOnMissingBean
    ValidationPolicyResolver requestValidationPolicyResolver(RequestValidationProperties properties) {
        return new ValidationPolicyResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    ContentDetector requestContentDetector(RequestValidationProperties properties) {
        return new ContentDetector(properties.getRules().toPolicy());
    }

    @Bean
    @ConditionalOnMissingBean
    JsonDocumentInspector requestJsonDocumentInspector(ContentDetector detector) {
        return new JsonDocumentInspector(detector);
    }

    @Bean
    @ConditionalOnMissingBean
    ValidationAuditLogger requestValidationAuditLogger(RequestValidationProperties properties) {
        return new ValidationAuditLogger(properties.getLog().getReportSampleRate());
    }

    @Bean
    @ConditionalOnMissingBean
    ValidationReporter requestValidationReporter(ValidationAuditLogger logger) {
        return new ValidationReporter(logger);
    }

    @Bean
    @ConditionalOnMissingBean
    ScalarParameterInterceptor requestScalarParameterInterceptor(ValidationPolicyResolver resolver,
            ContentDetector detector, ValidationReporter reporter, RequestValidationProperties properties) {
        return new ScalarParameterInterceptor(resolver, detector, reporter, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    StructuredBodyAdvice requestStructuredBodyAdvice(ValidationPolicyResolver resolver,
            JsonDocumentInspector inspector, ValidationReporter reporter, RequestValidationProperties properties,
            ServletSupport servlet) {
        return new StructuredBodyAdvice(resolver, inspector, reporter, properties, servlet);
    }

    @Bean
    @ConditionalOnMissingBean
    RequestValidationMvcConfigurer requestValidationMvcConfigurer(ScalarParameterInterceptor interceptor,
                                                                  ServletSupport servlet) {
        return new RequestValidationMvcConfigurer(servlet.scalarInterceptor(interceptor));
    }

    @Bean
    @ConditionalOnMissingBean
    static BodyAdviceRegistrar requestValidationBodyAdviceRegistrar(ObjectProvider<StructuredBodyAdvice> advice) {
        return new BodyAdviceRegistrar(advice);
    }

    @Bean
    @ConditionalOnMissingBean
    CoverageReport requestValidationCoverageReport(ListableBeanFactory beanFactory,
                                                   ValidationPolicyResolver resolver) {
        return new CoverageReport(beanFactory, resolver);
    }
}
