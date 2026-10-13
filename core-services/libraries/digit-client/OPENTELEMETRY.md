# OpenTelemetry Integration in Digit Client Library

## Overview

The digit-client library now includes comprehensive OpenTelemetry tracing support for automatic distributed tracing of all HTTP calls made via RestTemplate. This implementation follows the same pattern as the tracer library and provides production-ready observability.

## Features

- **Automatic HTTP Tracing**: All outbound HTTP calls are automatically traced with detailed span attributes
- **W3C Trace Context Propagation**: Automatic trace context injection into HTTP headers for distributed tracing
- **OTLP Exporter Support**: Export traces to any OTLP-compatible backend (Jaeger, Zipkin, etc.)
- **Configurable Sampling**: Control trace sampling ratio for production environments
- **Error Recording**: Automatic exception recording in spans with proper status codes
- **Spring Boot Auto-Configuration**: Zero-code integration for Spring Boot applications
- **Optional Dependency**: OpenTelemetry is completely optional and won't affect applications that don't use it

## Dependencies Added

The following OpenTelemetry dependencies have been added to the library:

```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.35.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>1.35.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
    <version>1.35.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.1.0-alpha</version>
    <optional>true</optional>
</dependency>
```

## Configuration

### Spring Boot Application

Add the following properties to your `application.properties` or `application.yml`:

#### application.properties
```properties
# Enable OpenTelemetry tracing
digit.opentelemetry.enabled=true

# Service name (appears in traces)
digit.opentelemetry.service-name=my-service

# OTLP exporter endpoint (Jaeger, Zipkin, etc.)
digit.opentelemetry.endpoint=http://localhost:4317

# Sampling ratio (0.0 to 1.0)
digit.opentelemetry.sampling-ratio=1.0

# Enable detailed logging (optional)
digit.opentelemetry.detailed-logging=false

# Export timeout in milliseconds (optional)
digit.opentelemetry.export-timeout=30000

# Batch size for span export (optional)
digit.opentelemetry.batch-size=512
```

#### application.yml
```yaml
digit:
  opentelemetry:
    enabled: true
    service-name: my-service
    endpoint: http://localhost:4317
    sampling-ratio: 1.0
    detailed-logging: false
    export-timeout: 30000
    batch-size: 512
```

### Environment Variables

You can also configure OpenTelemetry using environment variables:

```bash
DIGIT_OPENTELEMETRY_ENABLED=true
DIGIT_OPENTELEMETRY_SERVICE_NAME=my-service
DIGIT_OPENTELEMETRY_ENDPOINT=http://localhost:4317
DIGIT_OPENTELEMETRY_SAMPLING_RATIO=1.0
```

## Usage

### Spring Boot Applications

For Spring Boot applications, OpenTelemetry is automatically configured via auto-configuration. Simply add the configuration properties and the library will handle the rest.

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### Regular Spring Applications

For non-Spring Boot applications, you need to import the configuration manually:

```java
@Configuration
@Import({ApiConfig.class, OpenTelemetryConfig.class})
public class AppConfig {
    // Your configuration
}
```

## Span Attributes

The OpenTelemetry interceptor automatically adds the following attributes to each HTTP span:

- `http.method`: HTTP method (GET, POST, etc.)
- `http.url`: Full URL of the request
- `http.scheme`: URL scheme (http, https)
- `http.host`: Target host
- `http.target`: Request path
- `net.peer.port`: Target port (if specified)
- `http.request.body.size`: Size of request body in bytes
- `http.status_code`: HTTP response status code

## Trace Context Propagation

The library automatically injects W3C Trace Context headers into all outbound HTTP requests:

- `traceparent`: Contains trace ID, span ID, and trace flags
- `tracestate`: Contains vendor-specific trace information

This enables distributed tracing across multiple services.

## Integration with Observability Backends

### Jaeger

To send traces to Jaeger, configure the OTLP endpoint:

```properties
digit.opentelemetry.endpoint=http://localhost:4317
```

Run Jaeger with OTLP support:
```bash
docker run -d --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 16686:16686 \
  jaegertracing/all-in-one:latest
```

Access Jaeger UI at: http://localhost:16686

### Zipkin

For Zipkin, use the Zipkin exporter endpoint:

```properties
digit.opentelemetry.endpoint=http://localhost:9411/api/v2/spans
```

### Custom OTLP Collector

You can send traces to any OTLP-compatible collector:

```properties
digit.opentelemetry.endpoint=http://your-collector:4317
```

## Production Considerations

### Sampling

In production, you may want to reduce the sampling ratio to decrease overhead:

```properties
# Sample 10% of traces
digit.opentelemetry.sampling-ratio=0.1
```

### Performance

The OpenTelemetry implementation uses:
- **Batch Processing**: Spans are exported in batches to reduce network overhead
- **Async Export**: Span export happens asynchronously and doesn't block application threads
- **Graceful Shutdown**: Proper cleanup on application shutdown

### Disabling Tracing

To disable tracing without removing the configuration:

```properties
digit.opentelemetry.enabled=false
```

## Troubleshooting

### Traces Not Appearing

1. **Check if OpenTelemetry is enabled**:
   ```properties
   digit.opentelemetry.enabled=true
   ```

2. **Verify the endpoint is correct**:
   ```bash
   curl http://localhost:4317
   ```

3. **Check application logs** for OpenTelemetry initialization messages:
   ```
   OpenTelemetry initialized successfully for service: my-service
   ✅ OpenTelemetry tracing enabled for RestTemplate
   ```

4. **Verify sampling ratio** is not set to 0:
   ```properties
   digit.opentelemetry.sampling-ratio=1.0
   ```

### High Memory Usage

If you experience high memory usage, try:

1. **Reduce batch size**:
   ```properties
   digit.opentelemetry.batch-size=256
   ```

2. **Reduce sampling ratio**:
   ```properties
   digit.opentelemetry.sampling-ratio=0.1
   ```

3. **Decrease export timeout**:
   ```properties
   digit.opentelemetry.export-timeout=10000
   ```

## Example Trace Output

When tracing is enabled, you'll see spans like:

```
Span: POST /api/v1/users
  - http.method: POST
  - http.url: http://localhost:8080/api/v1/users
  - http.status_code: 201
  - Duration: 45ms
```

## Compatibility

- **Java**: 17+
- **Spring Framework**: 6.x
- **Spring Boot**: 3.x (optional)
- **OpenTelemetry**: 1.35.0

## Related Documentation

- [OpenTelemetry Java Documentation](https://opentelemetry.io/docs/instrumentation/java/)
- [OTLP Specification](https://opentelemetry.io/docs/reference/specification/protocol/otlp/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)
