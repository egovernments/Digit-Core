package org.digit.notify.app.dispatch;

import org.digit.notify.app.domain.entity.NotificationConfigEntity;
import org.digit.notify.app.domain.entity.config.ChannelConfig;
import org.digit.notify.app.domain.entity.config.EmailChannelConfig;
import org.digit.notify.app.domain.entity.config.PushChannelConfig;
import org.digit.notify.app.model.NotifyRequest;
import org.digit.notify.app.plugin.ProviderPluginLoader;
import org.digit.notify.app.template.TemplateRenderException;
import org.digit.notify.app.template.TemplateRenderer;
import org.digit.notify.spi.Channel;
import org.digit.notify.spi.DispatchResult;
import org.digit.notify.spi.DispatchStatus;
import org.digit.notify.spi.NotificationChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;

@Component
public class DispatchEngine {

    private static final Logger log = LoggerFactory.getLogger(DispatchEngine.class);

    private final ProviderMappingResolver mappingResolver;
    private final ProviderPluginLoader pluginLoader;
    private final TemplateRenderer templateRenderer;

    public DispatchEngine(ProviderMappingResolver mappingResolver,
                          ProviderPluginLoader pluginLoader,
                          TemplateRenderer templateRenderer) {
        this.mappingResolver = mappingResolver;
        this.pluginLoader = pluginLoader;
        this.templateRenderer = templateRenderer;
    }

    public DispatchOutcome dispatch(
        NotifyRequest request,
        NotificationConfigEntity config,
        String tenantId
    ) {
        var results = new CopyOnWriteArrayList<DispatchResult>();
        var attempts = new CopyOnWriteArrayList<AttemptRecord>();

        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
            for (Channel channel : Channel.values()) {
                scope.fork(() -> {
                    var result = dispatchChannel(channel, request, config, tenantId, attempts);
                    results.add(result);
                    return null;
                });
            }
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Dispatch interrupted", e);
        }

        return new DispatchOutcome(List.copyOf(results), List.copyOf(attempts));
    }

    private DispatchResult dispatchChannel(
        Channel channel,
        NotifyRequest request,
        NotificationConfigEntity config,
        String tenantId,
        List<AttemptRecord> attempts
    ) {
        var channelConfig = getChannelConfig(config, channel);
        if (channelConfig == null || !channelConfig.isEnabled()) {
            return DispatchResult.skipped(channel, "Channel disabled or not configured");
        }

        var bodyTemplates = channelConfig.getBody() != null ? channelConfig.getBody() : Collections.<String, String>emptyMap();
        Map<String, String> subjectTemplates = channelConfig instanceof EmailChannelConfig e ? e.getSubject() : null;
        Map<String, String> titleTemplates = channelConfig instanceof PushChannelConfig p ? p.getTitle() : null;
        var payloadBindings = channelConfig.getPayloadBindings();

        org.digit.notify.spi.ChannelMessage message;
        try {
            message = templateRenderer.render(
                channel, bodyTemplates, subjectTemplates, titleTemplates,
                payloadBindings, request.payload(), request.locale());
        } catch (TemplateRenderException e) {
            log.warn("Template render failed for channel {}: {}", channel, e.getMessage());
            attempts.add(new AttemptRecord(channel, "none", 1, DispatchStatus.FAILED, e.getMessage(), Instant.now()));
            return DispatchResult.failed(channel, "none", e.getMessage());
        }

        var providerNames = mappingResolver.resolve(channel, request.recipient().countryCode(), tenantId);
        if (providerNames.isEmpty()) {
            String reason = "No provider mapping found for channel " + channel;
            attempts.add(new AttemptRecord(channel, "none", 1, DispatchStatus.FAILED, reason, Instant.now()));
            return DispatchResult.failed(channel, "none", reason);
        }

        var providers = pluginLoader.getProvidersOrdered(channel, providerNames);
        if (providers.isEmpty()) {
            String reason = "No active providers registered for channel " + channel;
            attempts.add(new AttemptRecord(channel, "none", 1, DispatchStatus.FAILED, reason, Instant.now()));
            return DispatchResult.failed(channel, "none", reason);
        }

        int attemptNo = 1;
        for (NotificationChannelProvider provider : providers) {
            DispatchResult result;
            try {
                result = provider.send(message, request.recipient(), request.metadata());
            } catch (Exception e) {
                log.warn("Provider {} threw exception for channel {}: {}", provider.providerName(), channel, e.getMessage());
                result = DispatchResult.failed(channel, provider.providerName(), e.getMessage());
            }

            attempts.add(new AttemptRecord(
                channel, provider.providerName(), attemptNo,
                result.status(), result.reason(), Instant.now()));

            if (result.status() == DispatchStatus.DISPATCHED) {
                return result;
            }

            log.warn("Provider {} failed for channel {}, trying fallback", provider.providerName(), channel);
            attemptNo++;
        }

        return DispatchResult.failed(channel, providers.getLast().providerName(),
            "All providers exhausted for channel " + channel);
    }

    private ChannelConfig getChannelConfig(NotificationConfigEntity config, Channel channel) {
        if (config.getChannels() == null) return null;
        return switch (channel) {
            case SMS -> config.getChannels().getSms();
            case EMAIL -> config.getChannels().getEmail();
            case WHATSAPP -> config.getChannels().getWhatsapp();
            case PUSH -> config.getChannels().getPush();
        };
    }
}
