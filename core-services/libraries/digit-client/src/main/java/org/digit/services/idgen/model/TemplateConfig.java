package org.digit.services.idgen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How a template composes an id. Mirrors the idgen service's {@code TemplateConfig}.
 *
 * <p>The service fills in defaults for anything omitted and stores the normalised form, so a template
 * read back carries more than was sent: sequence scope {@code GLOBAL}, start {@code 1}, random length
 * {@code 2} and charset {@code A-Z0-9}. {@code padding} is the exception — it stays absent unless
 * supplied.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateConfig {
    /** The pattern, e.g. {@code PT-{seq}}. */
    @JsonProperty("template")
    private String template;
    @JsonProperty("sequence")
    private Sequence sequence;
    @JsonProperty("random")
    private Random random;

    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sequence {
        @JsonProperty("scope")
        private SequenceScope scope;
        /** At least 1; defaults to 1. */
        @JsonProperty("start")
        private Integer start;
        @JsonProperty("padding")
        private Padding padding;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Padding {
        /** 1..10; defaults to 4. */
        @JsonProperty("length")
        private Integer length;
        /**
         * The single character to pad with, defaulting to {@code "0"}.
         *
         * <p>Serialized as {@code char} — the service's own field is named {@code character} but its
         * wire name is the reserved-looking short form.
         */
        @JsonProperty("char")
        private String character;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Random {
        /** 1..10; defaults to 2. */
        @JsonProperty("length")
        private Integer length;
        /** Ranges or literals, e.g. {@code A-Z0-9}; defaults to {@code A-Z0-9}. */
        @JsonProperty("charset")
        private String charset;
    }
}
