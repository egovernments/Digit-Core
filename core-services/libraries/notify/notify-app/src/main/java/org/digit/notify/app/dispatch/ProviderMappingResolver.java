package org.digit.notify.app.dispatch;

import org.digit.notify.app.domain.repository.ProviderMappingRepository;
import org.digit.notify.spi.Channel;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProviderMappingResolver {

    private final ProviderMappingRepository mappingRepository;

    public ProviderMappingResolver(ProviderMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List<String> resolve(Channel channel, @Nullable String countryCode, String tenantId) {
        if (countryCode != null) {
            var specific = mappingRepository.findByTenantIdAndChannelAndCountry(
                tenantId, channel.name(), countryCode);
            if (specific.isPresent()) {
                return specific.get().getProviders();
            }
        }

        var global = mappingRepository.findByTenantIdAndChannelAndCountryIsNull(
            tenantId, channel.name());
        return global.map(e -> e.getProviders()).orElse(Collections.emptyList());
    }
}
