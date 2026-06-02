package org.digit.notify.app.template;

import org.digit.notify.spi.Channel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void happyPath_sms_rendersBodyWithExtractedVariables() {
        var payload = Map.<String, Object>of(
            "data", Map.of("otp_code", "123456", "expiry", Map.of("minutes", 10)));
        var bindings = Map.of("otp", "$.data.otp_code", "expiry", "$.data.expiry.minutes");
        var templates = Map.of("default", "Your OTP is {{otp}}, expires in {{expiry}} minutes");

        var result = renderer.render(Channel.SMS, templates, null, null, bindings, payload, null);

        assertThat(result.renderedBody()).isEqualTo("Your OTP is 123456, expires in 10 minutes");
        assertThat(result.channel()).isEqualTo(Channel.SMS);
        assertThat(result.renderedSubject()).isNull();
    }

    @Test
    void localeFallback_usesDefaultWhenRequestedLocaleAbsent() {
        var payload = Map.<String, Object>of("data", Map.of("otp_code", "999"));
        var bindings = Map.of("otp", "$.data.otp_code");
        var templates = Map.of("default", "Your OTP is {{otp}}");

        var result = renderer.render(Channel.SMS, templates, null, null, bindings, payload, "fr");

        assertThat(result.renderedBody()).isEqualTo("Your OTP is 999");
    }

    @Test
    void missingDefaultLocale_throwsTemplateRenderException() {
        var payload = Map.<String, Object>of("data", Map.of("otp_code", "123"));
        var bindings = Map.of("otp", "$.data.otp_code");
        var templates = Map.of("en", "Your OTP is {{otp}}");

        assertThatThrownBy(() ->
            renderer.render(Channel.SMS, templates, null, null, bindings, payload, "fr"))
            .isInstanceOf(TemplateRenderException.class)
            .hasMessageContaining("default");
    }

    @Test
    void missingVariable_throwsTemplateRenderExceptionWithVarName() {
        var payload = Map.<String, Object>of("data", Map.of("otp_code", "123"));
        var bindings = Map.of("otp", "$.data.otp_code");
        var templates = Map.of("default", "Hello {{name}}, your OTP is {{otp}}");

        assertThatThrownBy(() ->
            renderer.render(Channel.SMS, templates, null, null, bindings, payload, null))
            .isInstanceOf(TemplateRenderException.class)
            .hasMessageContaining("name");
    }

    @Test
    void badJsonPath_throwsTemplateRenderException() {
        var payload = Map.<String, Object>of();
        var bindings = Map.of("val", "$.nonexistent.deep.path");
        var templates = Map.of("default", "Value: {{val}}");

        assertThatThrownBy(() ->
            renderer.render(Channel.SMS, templates, null, null, bindings, payload, null))
            .isInstanceOf(TemplateRenderException.class)
            .hasMessageContaining("$.nonexistent.deep.path");
    }

    @Test
    void nullPayloadBindings_templateWithNoPlaceholders_rendersSuccessfully() {
        var payload = Map.<String, Object>of();
        var templates = Map.of("default", "Hello from notify-service");

        var result = renderer.render(Channel.SMS, templates, null, null, null, payload, null);

        assertThat(result.renderedBody()).isEqualTo("Hello from notify-service");
    }

    @Test
    void email_rendersBothBodyAndSubject() {
        var payload = Map.<String, Object>of("data", Map.of("otp_code", "456"));
        var bindings = Map.of("otp", "$.data.otp_code");
        var bodyTemplates = Map.of("default", "Your OTP is {{otp}}");
        var subjectTemplates = Map.of("default", "OTP: {{otp}}");

        var result = renderer.render(Channel.EMAIL, bodyTemplates, subjectTemplates, null, bindings, payload, null);

        assertThat(result.renderedBody()).isEqualTo("Your OTP is 456");
        assertThat(result.renderedSubject()).isEqualTo("OTP: 456");
    }

    @Test
    void sms_renderedSubjectIsNull() {
        var payload = Map.<String, Object>of();
        var templates = Map.of("default", "Hello");

        var result = renderer.render(Channel.SMS, templates, null, null, null, payload, null);

        assertThat(result.renderedSubject()).isNull();
    }
}
