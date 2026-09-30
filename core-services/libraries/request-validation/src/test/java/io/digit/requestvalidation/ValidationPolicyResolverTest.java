package io.digit.requestvalidation;

import org.egov.requestvalidation.Structured;
import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationPolicyResolverTest {
    @Test
    void requiresExplicitStructuredDefault() {
        assertThatThrownBy(() -> new ValidationPolicyResolver(new RequestValidationProperties()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("structured-default");
    }

    @Test
    void propertiesBoundWhileDisabledNeverFailButEnabledRequiresTheDefault() {
        var disabled = new RequestValidationProperties();
        disabled.afterPropertiesSet();
        var enabled = new RequestValidationProperties();
        enabled.setEnabled(true);
        assertThatThrownBy(enabled::afterPropertiesSet)
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("structured-default");
    }

    @Test
    void parameterOverridesModeAndBodySwitchWhilePathsCombine() throws Exception {
        var properties = new RequestValidationProperties();
        properties.setStructuredDefault(false);
        var resolver = new ValidationPolicyResolver(properties);
        var method = PolicyController.class.getMethod("handle", String.class);
        var body = resolver.parameter(new MethodParameter(method, 0));
        assertThat(body.enabled()).isTrue();
        assertThat(body.structured()).isFalse();
        assertThat(body.mode()).isEqualTo(ValidationMode.ENFORCE);
        assertThat(body.skipPaths()).containsExactly("/class", "/method", "/parameter");
        assertThat(body.matcher().matches(List.of("method", "child"))).isTrue();
        assertThat(resolver.handler(new HandlerMethod(new PolicyController(), method)).structured()).isTrue();
    }

    @Test
    void parameterAnnotationDoesNotActivateAnUnannotatedHandler() throws Exception {
        var properties = new RequestValidationProperties();
        properties.setStructuredDefault(true);
        var resolver = new ValidationPolicyResolver(properties);
        assertThat(resolver.parameter(new MethodParameter(
                UnannotatedController.class.getMethod("handle", String.class), 0)).enabled()).isFalse();
    }

    @Test
    void rejectsInvalidHandlerLimits() throws Exception {
        var properties = new RequestValidationProperties();
        properties.setStructuredDefault(true);
        var resolver = new ValidationPolicyResolver(properties);
        var method = PolicyController.class.getMethod("invalid", String.class);
        assertThatThrownBy(() -> resolver.parameter(new MethodParameter(method, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ValidateRequest(skipPaths = "/class", reason = "class markup")
    static class PolicyController {
        @ValidateRequest(structured = Structured.ENABLED, skipPaths = "/method", reason = "method markup")
        public void handle(@ValidateRequest(structured = Structured.DISABLED, mode = ValidationMode.ENFORCE,
                skipPaths = "/parameter", reason = "body disabled") String body) { }
        @ValidateRequest(maxDepth = 0)
        public void invalid(String body) { }
    }

    static class UnannotatedController {
        public void handle(@ValidateRequest String body) { }
    }
}
