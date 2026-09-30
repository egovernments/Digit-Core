package io.digit.requestvalidation.autoconfigure;

import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.egov.requestvalidation.core.ContentDetector;
import org.egov.requestvalidation.core.JsonDocumentInspector;
import org.egov.requestvalidation.web.BodyAdviceRegistrar;
import org.egov.requestvalidation.web.CoverageReport;
import org.egov.requestvalidation.web.RequestValidationMvcConfigurer;
import org.egov.requestvalidation.web.ScalarParameterInterceptor;
import org.egov.requestvalidation.web.StructuredBodyAdvice;
import org.egov.requestvalidation.web.ValidationAuditLogger;
import org.egov.requestvalidation.web.ValidationReporter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Outside both org.egov and digit scans; registered by Boot's imports file only. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "egov.request-validation", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(RequestValidationProperties.class)
public class RequestValidationAutoConfiguration {
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
            JsonDocumentInspector inspector, ValidationReporter reporter, RequestValidationProperties properties) {
        return new StructuredBodyAdvice(resolver, inspector, reporter, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    RequestValidationMvcConfigurer requestValidationMvcConfigurer(ScalarParameterInterceptor interceptor) {
        return new RequestValidationMvcConfigurer(interceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    static BodyAdviceRegistrar requestValidationBodyAdviceRegistrar(ObjectProvider<StructuredBodyAdvice> advice) {
        return new BodyAdviceRegistrar(advice);
    }

    @Bean
    @ConditionalOnMissingBean
    CoverageReport requestValidationCoverageReport(ObjectProvider<RequestMappingHandlerMapping> mappings,
                                                   ValidationPolicyResolver resolver) {
        return new CoverageReport(mappings, resolver);
    }
}
