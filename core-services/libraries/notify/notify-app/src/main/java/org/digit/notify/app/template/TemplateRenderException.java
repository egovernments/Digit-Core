package org.digit.notify.app.template;

import org.digit.notify.spi.Channel;

public class TemplateRenderException extends RuntimeException {

    private final Channel channel;

    public TemplateRenderException(Channel channel, String message, Throwable cause) {
        super(message, cause);
        this.channel = channel;
    }

    public TemplateRenderException(Channel channel, String message) {
        super(message);
        this.channel = channel;
    }

    public Channel getChannel() { return channel; }

    public static TemplateRenderException missingVariable(Channel channel, String varName) {
        return new TemplateRenderException(channel,
            "Template variable '" + varName + "' not resolved for channel " + channel);
    }

    public static TemplateRenderException badJsonPath(Channel channel, String expression, Throwable cause) {
        return new TemplateRenderException(channel,
            "JSONPath expression '" + expression + "' failed for channel " + channel, cause);
    }

    public static TemplateRenderException missingDefaultLocale(Channel channel) {
        return new TemplateRenderException(channel,
            "No template found for 'default' locale for channel " + channel);
    }
}
