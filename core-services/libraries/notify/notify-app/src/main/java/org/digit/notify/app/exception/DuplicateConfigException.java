package org.digit.notify.app.exception;

public class DuplicateConfigException extends RuntimeException {
    public DuplicateConfigException(String tenantId, String templateCode) {
        super("NotificationConfig already exists for tenant '" + tenantId
            + "' and templateCode '" + templateCode + "'");
    }
}
