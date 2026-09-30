package org.egov.requestvalidation.config;

import org.egov.requestvalidation.Activation;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.ContentPolicy;
import org.egov.requestvalidation.core.InspectionLimits;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.InitializingBean;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "egov.request-validation", ignoreUnknownFields = false)
public class RequestValidationProperties implements InitializingBean {
    // Opt-in: a service that only has the jar on its classpath gets no validation infrastructure.
    private boolean enabled = false;
    private Activation activation = Activation.ANNOTATED;
    private ValidationMode mode = ValidationMode.REPORT;
    private Boolean structuredDefault;
    private boolean inspectContentTypeHeader = true;
    private boolean rejectDuplicateKeys = true;
    private boolean rejectDualRequestInfo = true;
    private final Limits limits = new Limits();
    private final Rules rules = new Rules();
    private final Log log = new Log();

    // Only an enabled service must be fully configured; a host that binds these properties while the
    // library is off (e.g. through a properties scan) must still start. The resolver calls validate() itself.
    @Override
    public void afterPropertiesSet() { if (enabled) validate(); }

    public void validate() {
        if (structuredDefault == null) {
            throw new IllegalArgumentException("egov.request-validation.structured-default must be explicitly true or false");
        }
        if (activation == null || mode == null || mode == ValidationMode.DEFAULT) {
            throw new IllegalArgumentException("Service activation and mode must be explicit enum values");
        }
        if (!Double.isFinite(log.reportSampleRate) || log.reportSampleRate < 0 || log.reportSampleRate > 1) {
            throw new IllegalArgumentException("report-sample-rate must be between zero and one");
        }
        limits.toLimits();
        rules.toPolicy();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public Activation getActivation() { return activation; }
    public void setActivation(Activation value) { activation = value; }
    public ValidationMode getMode() { return mode; }
    public void setMode(ValidationMode value) { mode = value; }
    public Boolean getStructuredDefault() { return structuredDefault; }
    public void setStructuredDefault(Boolean value) { structuredDefault = value; }
    public boolean isInspectContentTypeHeader() { return inspectContentTypeHeader; }
    public void setInspectContentTypeHeader(boolean value) { inspectContentTypeHeader = value; }
    public boolean isRejectDuplicateKeys() { return rejectDuplicateKeys; }
    public void setRejectDuplicateKeys(boolean value) { rejectDuplicateKeys = value; }
    public boolean isRejectDualRequestInfo() { return rejectDualRequestInfo; }
    public void setRejectDualRequestInfo(boolean value) { rejectDualRequestInfo = value; }
    public Limits getLimits() { return limits; }
    public Rules getRules() { return rules; }
    public Log getLog() { return log; }

    public static class Limits {
        private int maxBodyBytes = 10 * 1024 * 1024;
        private int maxDepth = 64;
        private int maxStringLength = 1_000_000;
        private int maxNameLength = 256;
        private long maxTokens = 2_000_000;
        private int maxNumberLength = 1000;
        private int maxScalarLength = 64 * 1024;

        public InspectionLimits toLimits() {
            return new InspectionLimits(maxBodyBytes, maxDepth, maxStringLength, maxNameLength,
                    maxTokens, maxNumberLength, maxScalarLength);
        }
        public int getMaxBodyBytes() { return maxBodyBytes; }
        public void setMaxBodyBytes(int value) { maxBodyBytes = value; }
        public int getMaxDepth() { return maxDepth; }
        public void setMaxDepth(int value) { maxDepth = value; }
        public int getMaxStringLength() { return maxStringLength; }
        public void setMaxStringLength(int value) { maxStringLength = value; }
        public int getMaxNameLength() { return maxNameLength; }
        public void setMaxNameLength(int value) { maxNameLength = value; }
        public long getMaxTokens() { return maxTokens; }
        public void setMaxTokens(long value) { maxTokens = value; }
        public int getMaxNumberLength() { return maxNumberLength; }
        public void setMaxNumberLength(int value) { maxNumberLength = value; }
        public int getMaxScalarLength() { return maxScalarLength; }
        public void setMaxScalarLength(int value) { maxScalarLength = value; }
    }

    public static class Rules {
        private boolean markupStart = true;
        private boolean urlScheme = true;
        private boolean eventHandler = true;
        private Set<Integer> disallowedControls = new LinkedHashSet<>(Set.of(0));
        private Set<String> deniedSchemes = new LinkedHashSet<>(Set.of("javascript", "vbscript"));
        private Set<String> deniedDataMediaTypes = new LinkedHashSet<>(
                Set.of("text/html", "application/xhtml+xml", "image/svg+xml"));
        private int decodeRounds = 2;
        private boolean normalizeNfkc;

        public ContentPolicy toPolicy() {
            return ContentPolicy.builder().markupStart(markupStart).urlScheme(urlScheme)
                    .eventHandler(eventHandler).disallowedControls(disallowedControls)
                    .deniedSchemes(deniedSchemes).deniedDataMediaTypes(deniedDataMediaTypes)
                    .decodeRounds(decodeRounds).normalizeNfkc(normalizeNfkc).build();
        }
        public boolean isMarkupStart() { return markupStart; }
        public void setMarkupStart(boolean value) { markupStart = value; }
        public boolean isUrlScheme() { return urlScheme; }
        public void setUrlScheme(boolean value) { urlScheme = value; }
        public boolean isEventHandler() { return eventHandler; }
        public void setEventHandler(boolean value) { eventHandler = value; }
        public Set<Integer> getDisallowedControls() { return disallowedControls; }
        public void setDisallowedControls(Set<Integer> value) { disallowedControls = value; }
        public Set<String> getDeniedSchemes() { return deniedSchemes; }
        public void setDeniedSchemes(Set<String> value) { deniedSchemes = value; }
        public Set<String> getDeniedDataMediaTypes() { return deniedDataMediaTypes; }
        public void setDeniedDataMediaTypes(Set<String> value) { deniedDataMediaTypes = value; }
        public int getDecodeRounds() { return decodeRounds; }
        public void setDecodeRounds(int value) { decodeRounds = value; }
        public boolean isNormalizeNfkc() { return normalizeNfkc; }
        public void setNormalizeNfkc(boolean value) { normalizeNfkc = value; }
    }

    public static class Log {
        private double reportSampleRate = 1.0;
        public double getReportSampleRate() { return reportSampleRate; }
        public void setReportSampleRate(double value) { reportSampleRate = value; }
    }
}
