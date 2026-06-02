package org.digit.notify.app.exception;

import org.jspecify.annotations.Nullable;

public class DuplicateMappingException extends RuntimeException {
    public DuplicateMappingException(String tenantId, String channel, @Nullable String country) {
        super("ProviderMapping already exists for tenant '" + tenantId
            + "', channel '" + channel + "', country '" + country + "'");
    }
}
