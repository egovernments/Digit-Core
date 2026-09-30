package org.egov.requestvalidation.web;

import jakarta.servlet.http.HttpServletRequest;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.config.EffectiveValidationPolicy;
import org.egov.requestvalidation.config.RequestValidationProperties;
import org.egov.requestvalidation.config.ValidationPolicyResolver;
import org.egov.requestvalidation.core.InspectionException;
import org.egov.requestvalidation.core.JsonDocumentInspector;
import org.egov.requestvalidation.core.Violation;
import org.egov.requestvalidation.core.ViolationCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Registered explicitly before MVC adapter initialization; deliberately has no stereotype. */
public final class StructuredBodyAdvice extends RequestBodyAdviceAdapter {
    private final ValidationPolicyResolver resolver;
    private final JsonDocumentInspector inspector;
    private final ValidationReporter reporter;
    private final boolean rejectDuplicateKeys;
    private final boolean rejectDualRequestInfo;

    public StructuredBodyAdvice(ValidationPolicyResolver resolver, JsonDocumentInspector inspector,
                                ValidationReporter reporter, RequestValidationProperties properties) {
        this.resolver = resolver;
        this.inspector = inspector;
        this.reporter = reporter;
        rejectDuplicateKeys = properties.isRejectDuplicateKeys();
        rejectDualRequestInfo = properties.isRejectDualRequestInfo();
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        if (!isBody(parameter)) return false;
        EffectiveValidationPolicy policy = resolver.parameter(parameter);
        return policy.enabled() && policy.structured();
    }

    public static boolean isBody(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class)
                || HttpEntity.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter, Type targetType,
                                          Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        EffectiveValidationPolicy policy = resolver.parameter(parameter);
        if (!policy.enabled() || !policy.structured()) return input;
        HttpServletRequest request = currentRequest();
        String handler = parameter.getContainingClass().getName() + "#" + parameter.getMethod().getName();
        ValidationMode mode = policy.mode();
        MediaType media;
        try {
            media = input.getHeaders().getContentType();
        } catch (IllegalArgumentException ex) {
            return unread(request, mode, ViolationCode.REQUEST_JSON_MALFORMED, "content-type", handler, input);
        }
        if (!isJson(media)) return input;
        try {
            if (media.getCharset() != null && !StandardCharsets.UTF_8.equals(media.getCharset())) {
                return unread(request, mode, ViolationCode.REQUEST_JSON_MALFORMED, "charset", handler, input);
            }
        } catch (IllegalArgumentException ex) {
            return unread(request, mode, ViolationCode.REQUEST_JSON_MALFORMED, "charset", handler, input);
        }
        int maximum = policy.limits().getMaxBodyBytes();
        long declared;
        try {
            declared = input.getHeaders().getContentLength();
        } catch (IllegalArgumentException ex) {
            return unread(request, mode, ViolationCode.REQUEST_JSON_MALFORMED, "content-length", handler, input);
        }
        if (declared > maximum) {
            return unread(request, mode, ViolationCode.REQUEST_LIMIT_EXCEEDED, "max-body-bytes", handler, input);
        }
        // getBody() is called once: the overflow replay below must continue this same stream.
        InputStream source;
        byte[] bytes;
        try {
            source = input.getBody();
            bytes = source.readNBytes(maximum + 1);
        } catch (IOException ex) {
            if (mode == ValidationMode.ENFORCE) {
                // Do not pass an input-dependent I/O exception to the host's generic error renderer.
                throw reject(request, ViolationCode.REQUEST_JSON_MALFORMED, "body-read", handler);
            }
            // A transport failure is not a policy finding; REPORT must not pretend the body was intact.
            reporter.incomplete(request, ViolationCode.REQUEST_JSON_MALFORMED, "transport", handler);
            throw ex;
        }
        if (bytes.length > maximum) {
            ContentPolicyViolationException rejection = reporter.structural(request, mode,
                    new Violation(ViolationCode.REQUEST_LIMIT_EXCEEDED, "max-body-bytes", "/", 0), "body", handler);
            if (rejection != null) throw rejection;
            // REPORT: the bounded prefix, then the unread remainder of the same stream, is the original body.
            return replay(input, new PrefixedInputStream(bytes, source));
        }
        try {
            inspector.inspect(bytes, policy.limits(), policy.matcher(), rejectDuplicateKeys, rejectDualRequestInfo,
                    violation -> reporter.content(request, mode, violation, "body", handler));
        } catch (InspectionException ex) {
            ContentPolicyViolationException rejection = reporter.structural(request, mode, ex.getViolation(), "body", handler);
            if (rejection != null) throw rejection;
        }
        return replay(input, new ByteArrayInputStream(bytes));
    }

    /** ENFORCE rejects; REPORT records the finding and returns the input untouched and unread. */
    private HttpInputMessage unread(HttpServletRequest request, ValidationMode mode, ViolationCode code,
                                    String rule, String handler, HttpInputMessage input) {
        ContentPolicyViolationException rejection = reporter.structural(request, mode,
                new Violation(code, rule, "/", 0), "body", handler);
        if (rejection != null) throw rejection;
        return input;
    }

    private static HttpInputMessage replay(HttpInputMessage input, InputStream body) {
        return new HttpInputMessage() {
            @Override public InputStream getBody() { return body; }
            @Override public HttpHeaders getHeaders() { return input.getHeaders(); }
        };
    }

    /**
     * The buffered prefix, then the unread rest of the same source. Unlike SequenceInputStream it does not
     * close the source at end of data and passes a 0-byte source read on unchanged, so the converter sees the
     * stream exactly as it would without this library.
     */
    static final class PrefixedInputStream extends InputStream {
        private final byte[] prefix;
        private final InputStream source;
        private int position;

        PrefixedInputStream(byte[] prefix, InputStream source) {
            this.prefix = prefix;
            this.source = source;
        }

        @Override
        public int read() throws IOException {
            return position < prefix.length ? prefix[position++] & 0xff : source.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            Objects.checkFromIndexSize(offset, length, buffer.length);
            if (length == 0) return 0;
            if (position < prefix.length) {
                int count = Math.min(length, prefix.length - position);
                System.arraycopy(prefix, position, buffer, offset, count);
                position += count;
                return count;
            }
            return source.read(buffer, offset, length);
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(Integer.MAX_VALUE, (long) (prefix.length - position) + source.available());
        }

        @Override
        public void close() throws IOException {
            source.close();
        }
    }

    private static boolean isJson(MediaType type) {
        return type != null && "application".equalsIgnoreCase(type.getType())
                && ("json".equalsIgnoreCase(type.getSubtype())
                || type.getSubtype().toLowerCase(java.util.Locale.ROOT).endsWith("+json"));
    }

    private ContentPolicyViolationException reject(HttpServletRequest request, ViolationCode code,
                                                   String rule, String handler) {
        return reporter.rejected(request, new Violation(code, rule, "/", 0), "body", handler);
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        throw new IllegalStateException("Request validation requires a servlet request context");
    }
}
