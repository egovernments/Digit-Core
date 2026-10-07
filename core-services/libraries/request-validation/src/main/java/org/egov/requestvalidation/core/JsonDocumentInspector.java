package org.egov.requestvalidation.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/** Streaming JSON inspection without DTO binding or tree conversion. */
public final class JsonDocumentInspector {
    private final ContentDetector detector;

    public JsonDocumentInspector(ContentDetector detector) {
        this.detector = Objects.requireNonNull(detector, "detector");
    }

    public void inspect(byte[] body, InspectionLimits limits, SkipPathMatcher skipPaths,
            boolean rejectDuplicateKeys, boolean rejectDualRequestInfo,
            Consumer<Violation> contentViolations) {
        inspect(body, limits, skipPaths, rejectDuplicateKeys, rejectDualRequestInfo, null, contentViolations);
    }

    /**
     * As above; a non-null exemption is offered each flagged string value once the whole body has passed the
     * syntax and limit checks, and findings it accepts are dropped. Findings are still reported in document
     * order. If the body fails a syntax or limit check, every finding before the failure is reported unchanged
     * and the exemption is not consulted.
     */
    public void inspect(byte[] body, InspectionLimits limits, SkipPathMatcher skipPaths,
            boolean rejectDuplicateKeys, boolean rejectDualRequestInfo, ContentExemption exemption,
            Consumer<Violation> contentViolations) {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(limits, "limits");
        Objects.requireNonNull(skipPaths, "skipPaths");
        Objects.requireNonNull(contentViolations, "contentViolations");
        if (exemption == null) {
            scan(body, limits, skipPaths, rejectDuplicateKeys, rejectDualRequestInfo, null, contentViolations);
            return;
        }
        Deferred deferred = new Deferred(exemption);
        try {
            scan(body, limits, skipPaths, rejectDuplicateKeys, rejectDualRequestInfo, deferred, contentViolations);
        } catch (InspectionException failure) {
            deferred.report(contentViolations, false);
            throw failure;
        }
        deferred.report(contentViolations, true);
    }

    private void scan(byte[] body, InspectionLimits limits, SkipPathMatcher skipPaths,
            boolean rejectDuplicateKeys, boolean rejectDualRequestInfo, Deferred deferred,
            Consumer<Violation> contentViolations) {
        if (body.length > limits.getMaxBodyBytes()) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                    "max-body-bytes", Collections.<String>emptyList(), body.length);
        }
        if (body.length == 0) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", Collections.<String>emptyList(), 0);
        }

        JsonFactory factory = JsonFactory.builder()
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNestingDepth(limits.getMaxDepth())
                        .maxStringLength(limits.getMaxStringLength())
                        .maxNumberLength(limits.getMaxNumberLength())
                        .build())
                .build();

        int offset = hasUtf8Bom(body) ? 3 : 0;
        Reader utf8Reader = new InputStreamReader(
                new ByteArrayInputStream(body, offset, body.length - offset),
                StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT));

        Deque<Context> contexts = new ArrayDeque<Context>();
        // Raw segments of the innermost open container, plus the last name or value (or an ended container)
        // until the next token moves past it, so a failure while reading the next token names the last
        // token's location, as 1.0.0 did. Never emitted directly: safe locations are formatted only when a
        // finding or failure is raised.
        List<String> path = new ArrayList<String>();
        boolean pending = false;
        boolean started = false;
        boolean complete = false;
        long tokenCount = 0L;

        try (Reader reader = utf8Reader; JsonParser parser = factory.createParser(reader)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                tokenCount++;
                if (tokenCount > limits.getMaxTokens()) {
                    throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                            "max-tokens", path, saturatedLength(tokenCount));
                }
                if (complete) {
                    throw failure(ViolationCode.REQUEST_JSON_MALFORMED,
                            "trailing-content", path, 0);
                }
                started = true;

                if (token == JsonToken.FIELD_NAME) {
                    Context object = contexts.peek();
                    if (object == null || !object.object) {
                        throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
                    }
                    pending = settle(path, pending);
                    String name = parser.getCurrentName();
                    path.add(name);
                    if (name.length() > limits.getMaxNameLength()) {
                        throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                                "max-name-length", path, name.length());
                    }
                    if (rejectDuplicateKeys && !object.names.add(name)) {
                        throw failure(ViolationCode.REQUEST_JSON_DUPLICATE_KEY,
                                "duplicate-key", path, name.length());
                    }
                    if (rejectDualRequestInfo && contexts.size() == 1) {
                        if ("RequestInfo".equals(name)) {
                            object.upperRequestInfo = true;
                        } else if ("requestInfo".equals(name)) {
                            object.lowerRequestInfo = true;
                        }
                        if (object.upperRequestInfo && object.lowerRequestInfo) {
                            throw failure(ViolationCode.REQUEST_JSON_MALFORMED,
                                    "dual-request-info", path, name.length());
                        }
                    }
                    object.currentField = name;
                    inspectContent(name, false, path, skipPaths, deferred, contentViolations);
                    pending = true;
                    continue;
                }

                if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
                    if (contexts.isEmpty()) {
                        throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
                    }
                    Context ended = contexts.pop();
                    if ((token == JsonToken.END_OBJECT) != ended.object) {
                        throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
                    }
                    pending = settle(path, pending);
                    // The path now names the ended container; its own segment goes when the next token arrives.
                    pending = ended.hasSegment;
                    if (contexts.isEmpty()) {
                        complete = true;
                    }
                    continue;
                }

                pending = settle(path, pending);
                Context parent = contexts.peek();
                if (parent != null) {
                    path.add(nextSegment(parent, path));
                }
                if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
                    int depth = contexts.size() + 1;
                    if (depth > limits.getMaxDepth()) {
                        throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, "max-depth", path, depth);
                    }
                    // The container's segment stays on the path until its matching end token.
                    contexts.push(new Context(token == JsonToken.START_OBJECT, parent != null));
                    continue;
                }

                if (!token.isScalarValue()) {
                    throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
                }
                if (token == JsonToken.VALUE_STRING) {
                    String value = parser.getText();
                    if (value.length() > limits.getMaxStringLength()) {
                        throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                                "max-string-length", path, value.length());
                    }
                    inspectContent(value, true, path, skipPaths, deferred, contentViolations);
                } else if (token.isNumeric()) {
                    int length = parser.getTextLength();
                    if (length > limits.getMaxNumberLength()) {
                        throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED,
                                "max-number-length", path, length);
                    }
                }
                pending = parent != null;
                if (contexts.isEmpty()) {
                    complete = true;
                }
            }
        } catch (InspectionException exception) {
            throw exception;
        } catch (StreamConstraintsException exception) {
            throw failure(ViolationCode.REQUEST_LIMIT_EXCEEDED, "parser-limit", path, 0);
        } catch (IOException exception) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
        }

        if (!started || !complete || !contexts.isEmpty()) {
            throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
        }
    }

    private void inspectContent(String value, boolean stringValue, List<String> path, SkipPathMatcher skipPaths,
            Deferred deferred, Consumer<Violation> contentViolations) {
        if (deferred != null && stringValue) {
            deferred.strings.put(FlaggedValue.format(path), value);
        }
        if (skipPaths.matches(path)) {
            return;
        }
        Optional<String> rule = detector.detect(value);
        if (rule.isPresent()) {
            Violation violation = new Violation(ViolationCode.REQUEST_CONTENT_NOT_ALLOWED,
                    rule.get(), SafeLocationFormatter.format(path), value.length());
            if (deferred == null) {
                contentViolations.accept(violation);
            } else {
                // Field names are never offered to the exemption.
                deferred.findings.add(new Finding(violation, stringValue ? new ArrayList<String>(path) : null, value));
            }
        }
    }

    private static String nextSegment(Context parent, List<String> path) {
        if (parent.object) {
            if (parent.currentField == null) {
                throw failure(ViolationCode.REQUEST_JSON_MALFORMED, "json", path, 0);
            }
            return parent.currentField;
        }
        return Integer.toString(parent.nextIndex++);
    }

    /** Drops the previous token's pending segment; always returns false so callers can reset the flag. */
    private static boolean settle(List<String> path, boolean pending) {
        if (pending) {
            path.remove(path.size() - 1);
        }
        return false;
    }

    private static boolean hasUtf8Bom(byte[] body) {
        return body.length >= 3 && (body[0] & 0xff) == 0xef
                && (body[1] & 0xff) == 0xbb && (body[2] & 0xff) == 0xbf;
    }

    private static int saturatedLength(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static InspectionException failure(
            ViolationCode code, String rule, List<String> path, int length) {
        return new InspectionException(new Violation(code, rule,
                SafeLocationFormatter.format(path), Math.max(0, length)));
    }

    /** Findings held back until the whole body has been read, and every string value by JSON Pointer. */
    private static final class Deferred {
        private final ContentExemption exemption;
        private final Map<String, String> strings = new HashMap<String, String>();
        private final List<Finding> findings = new ArrayList<Finding>();

        private Deferred(ContentExemption exemption) {
            this.exemption = exemption;
        }

        private void report(Consumer<Violation> contentViolations, boolean consult) {
            for (Finding finding : findings) {
                if (consult && finding.path != null && allows(finding)) {
                    continue;
                }
                contentViolations.accept(finding.violation);
            }
        }

        private boolean allows(Finding finding) {
            try {
                return exemption.allows(new FlaggedValue(finding.path, finding.value,
                        finding.violation.getRuleId(), strings));
            } catch (RuntimeException exception) {
                // Fails closed: a broken exemption keeps the finding (REPORT logs it, ENFORCE rejects).
                return false;
            }
        }
    }

    private static final class Finding {
        private final Violation violation;
        private final List<String> path;
        private final String value;

        private Finding(Violation violation, List<String> path, String value) {
            this.violation = violation;
            this.path = path;
            this.value = value;
        }
    }

    private static final class Context {
        private final boolean object;
        private final boolean hasSegment;
        private final Set<String> names;
        private int nextIndex;
        private String currentField;
        private boolean upperRequestInfo;
        private boolean lowerRequestInfo;

        private Context(boolean object, boolean hasSegment) {
            this.object = object;
            this.hasSegment = hasSegment;
            this.names = object ? new HashSet<String>() : Collections.<String>emptySet();
        }
    }
}
