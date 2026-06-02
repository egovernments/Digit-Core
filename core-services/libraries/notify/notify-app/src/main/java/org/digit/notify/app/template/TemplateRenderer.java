package org.digit.notify.app.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.digit.notify.spi.Channel;
import org.digit.notify.spi.ChannelMessage;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    public ChannelMessage render(
        Channel channel,
        Map<String, String> bodyTemplates,
        @Nullable Map<String, String> subjectTemplates,
        @Nullable Map<String, String> titleTemplates,
        @Nullable Map<String, String> payloadBindings,
        Map<String, Object> payload,
        @Nullable String locale
    ) {
        Map<String, String> variables = payloadBindings != null
            ? extractVariables(channel, payloadBindings, payload)
            : Collections.emptyMap();

        String effectiveLocale = locale != null ? locale : "default";

        String renderedBody = renderMustache(channel,
            selectTemplate(channel, bodyTemplates, effectiveLocale), variables);

        String renderedSubject = null;
        if (subjectTemplates != null) {
            renderedSubject = renderMustache(channel,
                selectTemplate(channel, subjectTemplates, effectiveLocale), variables);
        }

        String renderedTitle = null;
        if (titleTemplates != null) {
            renderedTitle = renderMustache(channel,
                selectTemplate(channel, titleTemplates, effectiveLocale), variables);
        }

        return new ChannelMessage(channel, renderedBody, renderedSubject, renderedTitle, Map.of());
    }

    private Map<String, String> extractVariables(
        Channel channel,
        Map<String, String> bindings,
        Map<String, Object> payload
    ) {
        var result = new HashMap<String, String>();
        var ctx = JsonPath.parse(payload);
        for (var entry : bindings.entrySet()) {
            try {
                Object value = ctx.read(entry.getValue());
                result.put(entry.getKey(), String.valueOf(value));
            } catch (PathNotFoundException | ClassCastException e) {
                throw TemplateRenderException.badJsonPath(channel, entry.getValue(), e);
            }
        }
        return result;
    }

    private String selectTemplate(
        Channel channel,
        Map<String, String> templates,
        String locale
    ) {
        String template = templates.get(locale);
        if (template == null) {
            template = templates.get("default");
        }
        if (template == null) {
            throw TemplateRenderException.missingDefaultLocale(channel);
        }
        return template;
    }

    private String renderMustache(
        Channel channel,
        String template,
        Map<String, String> variables
    ) {
        var matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!variables.containsKey(varName)) {
                throw TemplateRenderException.missingVariable(channel, varName);
            }
        }

        var factory = new DefaultMustacheFactory();
        var mustache = factory.compile(new StringReader(template), "template");
        var writer = new StringWriter();
        mustache.execute(writer, variables);
        return writer.toString();
    }
}
