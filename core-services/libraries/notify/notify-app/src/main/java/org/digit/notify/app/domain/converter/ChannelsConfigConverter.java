package org.digit.notify.app.domain.converter;

import jakarta.persistence.Converter;
import org.digit.notify.app.domain.entity.config.ChannelsConfig;

@Converter(autoApply = false)
public class ChannelsConfigConverter extends JsonbConverter<ChannelsConfig> {
    public ChannelsConfigConverter() {
        super(ChannelsConfig.class);
    }
}
