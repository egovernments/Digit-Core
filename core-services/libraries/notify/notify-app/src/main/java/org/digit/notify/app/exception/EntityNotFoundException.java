package org.digit.notify.app.exception;

public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String id;

    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " not found with id: " + id);
        this.entityType = entityType;
        this.id = id;
    }

    public String getEntityType() { return entityType; }
    public String getId() { return id; }
}
