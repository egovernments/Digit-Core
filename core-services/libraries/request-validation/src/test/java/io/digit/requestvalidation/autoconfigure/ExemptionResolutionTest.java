package io.digit.requestvalidation.autoconfigure;

import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.config.EffectiveValidationPolicy;
import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.egov.requestvalidation.core.ContentExemption;
import org.egov.requestvalidation.core.FlaggedValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExemptionResolutionTest {
    @Test
    void innermostDeclarationWinsAndNoneMeansNoExemption() throws Exception {
        ValidationPolicyResolver resolver = new ValidationPolicyResolver(properties());

        assertThat(body(resolver, "inherits").exemption()).isInstanceOf(ClassRule.class);
        assertThat(body(resolver, "method").exemption()).isInstanceOf(MethodRule.class);
        assertThat(body(resolver, "parameter").exemption()).isInstanceOf(ParameterRule.class);
        assertThat(body(resolver, "inherits").exemption()).isSameAs(body(resolver, "alsoInherits").exemption());
        assertThat(new ValidationPolicyResolver(properties()).parameter(new MethodParameter(
                PlainController.class.getMethod("handle", String.class), 0)).exemption()).isNull();
    }

    @Test
    void aFactoryFailureNamesTheExemption() throws Exception {
        ValidationPolicyResolver failing = new ValidationPolicyResolver(properties(), type -> {
            throw new IllegalStateException("boom");
        });
        ValidationPolicyResolver empty = new ValidationPolicyResolver(properties(), type -> null);

        assertThatThrownBy(() -> body(failing, "method"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(MethodRule.class.getName());
        assertThatThrownBy(() -> body(empty, "method"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(MethodRule.class.getName());
    }

    @Test
    void usesTheRegisteredBeanOtherwiseCreatesOne() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        MethodRule registered = new MethodRule();
        factory.registerSingleton("methodRule", registered);

        assertThat(RequestValidationAutoConfiguration.exemption(factory, MethodRule.class)).isSameAs(registered);
        assertThat(RequestValidationAutoConfiguration.exemption(factory, ClassRule.class)).isInstanceOf(ClassRule.class);

        factory.registerBeanDefinition("second", new RootBeanDefinition(MethodRule.class));
        assertThatThrownBy(() -> RequestValidationAutoConfiguration.exemption(factory, MethodRule.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("More than one");
    }

    private static EffectiveValidationPolicy body(ValidationPolicyResolver resolver, String name) throws Exception {
        return resolver.parameter(new MethodParameter(RuleController.class.getMethod(name, String.class), 0));
    }

    private static RequestValidationProperties properties() {
        RequestValidationProperties properties = new RequestValidationProperties();
        properties.setStructuredDefault(true);
        return properties;
    }

    public static class ClassRule implements ContentExemption {
        @Override public boolean allows(FlaggedValue value) { return false; }
    }

    public static class MethodRule implements ContentExemption {
        @Override public boolean allows(FlaggedValue value) { return false; }
    }

    public static class ParameterRule implements ContentExemption {
        @Override public boolean allows(FlaggedValue value) { return false; }
    }

    @ValidateRequest(exemption = ClassRule.class, reason = "test")
    static class RuleController {
        public void inherits(String body) { }

        public void alsoInherits(String body) { }

        @ValidateRequest(exemption = MethodRule.class, reason = "test")
        public void method(String body) { }

        @ValidateRequest(exemption = MethodRule.class, reason = "test")
        public void parameter(@ValidateRequest(exemption = ParameterRule.class, reason = "test") String body) { }
    }

    @ValidateRequest
    static class PlainController {
        public void handle(String body) { }
    }
}
