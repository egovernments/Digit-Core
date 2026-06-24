package org.digit.tracer.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Structured logger wrapping SLF4J, mirroring the Go logger/logger.go Zap wrapper.
 * Enriches log output with key-value fields serialized as JSON.
 */
public class StructuredLogger {

    private final Logger log;
    private final ObjectMapper objectMapper;

    private StructuredLogger(String name, ObjectMapper objectMapper) {
        this.log          = LoggerFactory.getLogger(name);
        this.objectMapper = objectMapper;
    }

    public static StructuredLogger forClass(Class<?> clazz, ObjectMapper objectMapper) {
        return new StructuredLogger(clazz.getName(), objectMapper);
    }

    public static StructuredLogger forName(String name, ObjectMapper objectMapper) {
        return new StructuredLogger(name, objectMapper);
    }

    public void info(String message, Map<String, Object> fields) {
        log.info("{} {}", message, toJson(fields));
    }

    public void error(String message, Throwable throwable, Map<String, Object> fields) {
        log.error("{} {}", message, toJson(fields), throwable);
    }

    public void warn(String message, Map<String, Object> fields) {
        log.warn("{} {}", message, toJson(fields));
    }

    public void debug(String message, Map<String, Object> fields) {
        log.debug("{} {}", message, toJson(fields));
    }

    public void info(String message) { log.info(message); }
    public void error(String message, Throwable t) { log.error(message, t); }
    public void warn(String message) { log.warn(message); }
    public void debug(String message) { log.debug(message); }

    private String toJson(Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) return "";
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (Exception e) {
            return fields.toString();
        }
    }
}
