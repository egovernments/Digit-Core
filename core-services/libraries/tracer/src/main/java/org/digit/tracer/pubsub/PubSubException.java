package org.digit.tracer.pubsub;

public class PubSubException extends RuntimeException {
    public PubSubException(String message) { super(message); }
    public PubSubException(String message, Throwable cause) { super(message, cause); }
}
