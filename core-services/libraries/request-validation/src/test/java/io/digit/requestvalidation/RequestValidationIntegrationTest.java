package io.digit.requestvalidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.digit.requestvalidation.autoconfigure.RequestValidationAutoConfiguration;
import org.egov.requestvalidation.Structured;
import org.egov.requestvalidation.ValidateRequest;
import org.egov.requestvalidation.ValidationMode;
import org.egov.requestvalidation.core.Violation;
import org.egov.requestvalidation.web.ContentPolicyViolationException;
import org.egov.requestvalidation.web.ScalarParameterInterceptor;
import org.egov.requestvalidation.web.StructuredBodyAdvice;
import org.egov.requestvalidation.web.ValidationAuditLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Actual MVC pipeline tests; test-only error advice models tracer's fixed code/message contract. */
class RequestValidationIntegrationTest {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RequestValidationAutoConfiguration.class))
            .withUserConfiguration(MvcApplication.class)
            .withPropertyValues("egov.request-validation.enabled=true",
                    "egov.request-validation.structured-default=true",
                    "egov.request-validation.mode=ENFORCE");

    @Test
    void checksUnknownFieldsAndWrongTypedValuesBeforeBinding() {
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            for (String json : List.of("{\"unknown\":\"<script>\"}",
                    "{\"id\":\"<script>\"}", "{\"RequestInfo\":{\"name\":\"<script>\"}}")) {
                mvc.perform(post("/typed").contentType("application/json").content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.Errors[0].code").value("REQUEST_CONTENT_NOT_ALLOWED"))
                        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>"))));
            }
            assertThat(context.getBean(Endpoints.class).calls).isZero();
        });
    }

    @Test
    void acceptedBytesAndHttpEntityBytesAreUnchanged() {
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            byte[] body = " { \"note\" : \"O'Brien & स्वास्थ्य < 5\" }\n".getBytes(StandardCharsets.UTF_8);
            for (String path : List.of("/bytes", "/entity")) {
                mvc.perform(post(path).contentType("application/problem+json;charset=UTF-8").content(body))
                        .andExpect(status().isOk()).andExpect(content().bytes(body));
            }
        });
    }

    @Test
    void disabledBodyStillChecksScalarTextBeforeNumericConversion() {
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(post("/skip").contentType("application/json").content("\"<script>\"").param("count", "1"))
                    .andExpect(status().isOk());
            mvc.perform(post("/skip").contentType("application/json").content("{}").param("count", "<script>"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Errors[0].code").value("REQUEST_CONTENT_NOT_ALLOWED"));
        });
    }

    @Test
    void reportModePreservesBytesAndRegistersAdviceOnce() {
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            byte[] body = "\"<script>\"".getBytes(StandardCharsets.UTF_8);
            mvc.perform(post("/bytes").contentType("application/json").content(body))
                    .andExpect(status().isOk()).andExpect(content().bytes(body));
            assertThat(context.getBeansOfType(StructuredBodyAdvice.class)).hasSize(1);
            assertThat(context.getBean(RecordingLogger.class).observations).hasSize(1);
            assertThat(context.getBean(ExistingBodyAdvice.class).calls).isEqualTo(1);
            // REPORT only observes: a malformed body reaches the host's converter unchanged.
            byte[] malformed = "{} {}".getBytes(StandardCharsets.UTF_8);
            mvc.perform(post("/bytes").contentType("application/json").content(malformed))
                    .andExpect(status().isOk()).andExpect(content().bytes(malformed));
        });
    }

    @Test
    void rejectsCharsetDisagreementAndDoesNotActivatePlainControllers() {
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(post("/bytes").contentType("application/json;charset=ISO-8859-1").content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Errors[0].code").value("REQUEST_JSON_MALFORMED"));
            mvc.perform(post("/plain").contentType("application/json").content("\"<script>\""))
                    .andExpect(status().isOk());
        });
    }

    @Test
    void globalKillSwitchDoesNotRequireBodyDefault() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RequestValidationAutoConfiguration.class))
                .withUserConfiguration(MvcApplication.class)
                .withPropertyValues("egov.request-validation.enabled=false")
                .run(context -> assertThat(context.getBeansOfType(StructuredBodyAdvice.class)).isEmpty());
    }

    @Test
    void dependencyOnlyStartupIsInert() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RequestValidationAutoConfiguration.class))
                .withUserConfiguration(MvcApplication.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeansOfType(StructuredBodyAdvice.class)).isEmpty();
                    assertThat(context.getBeansOfType(ScalarParameterInterceptor.class)).isEmpty();
                    MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
                    mvc.perform(post("/bytes").contentType("application/json").content("\"<script>\""))
                            .andExpect(status().isOk());
                });
    }

    @Test
    void enabledWithoutStructuredDefaultFailsStartup() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RequestValidationAutoConfiguration.class))
                .withUserConfiguration(MvcApplication.class)
                .withPropertyValues("egov.request-validation.enabled=true")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("structured-default"));
    }

    @Test
    void reportLogsStructuralFindingsAndLeavesTheBodyToTheConverter() {
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            RecordingLogger logger = context.getBean(RecordingLogger.class);
            for (String json : List.of("{} {}", "{\"a\":1,\"a\":2}", "{\"RequestInfo\":{},\"requestInfo\":{}}",
                    "{\"a\":" + "[".repeat(70) + "]".repeat(70) + "}")) {
                byte[] body = json.getBytes(StandardCharsets.UTF_8);
                logger.observations.clear();
                mvc.perform(post("/bytes").contentType("application/json").content(body))
                        .andExpect(status().isOk()).andExpect(content().bytes(body));
                assertThat(rules(logger)).hasSize(2).last().isEqualTo("inspection-incomplete");
            }
            byte[] latin = "{\"a\":\"caf\u00e9\"}".getBytes(StandardCharsets.ISO_8859_1);
            logger.observations.clear();
            mvc.perform(post("/bytes").contentType("application/json;charset=ISO-8859-1").content(latin))
                    .andExpect(status().isOk()).andExpect(content().bytes(latin));
            assertThat(rules(logger)).containsExactly("charset", "inspection-incomplete");

            byte[] big = "{\"note\":\"more than sixteen bytes\"}".getBytes(StandardCharsets.UTF_8);
            logger.observations.clear();
            mvc.perform(post("/small").contentType("application/json").content(big))
                    .andExpect(status().isOk()).andExpect(content().bytes(big));
            assertThat(rules(logger)).containsExactly("max-body-bytes", "inspection-incomplete");
        });
    }

    @Test
    void bodyLimitsInBothModesReadAtMostLimitPlusOneFromOneStream() {
        byte[] body = "{\"note\":\"more than sixteen bytes\"}".getBytes(StandardCharsets.UTF_8);
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            StructuredBodyAdvice advice = context.getBean(StructuredBodyAdvice.class);
            MethodParameter parameter = smallBody();
            inRequest(() -> {
                CountingMessage undeclared = new CountingMessage(body, false);
                HttpInputMessage replay = advice.beforeBodyRead(undeclared, parameter, byte[].class,
                        ByteArrayHttpMessageConverter.class);
                assertThat(undeclared.bodyCalls).isEqualTo(1);
                assertThat(undeclared.consumed()).isEqualTo(17);
                assertThat(replay.getHeaders()).isSameAs(undeclared.getHeaders());
                assertThat(replay.getBody().readAllBytes()).isEqualTo(body);

                CountingMessage declared = new CountingMessage(body, true);
                assertThat(advice.beforeBodyRead(declared, parameter, byte[].class,
                        ByteArrayHttpMessageConverter.class)).isSameAs(declared);
                assertThat(declared.bodyCalls).isZero();
            });
        });
        runner.run(context -> {
            StructuredBodyAdvice advice = context.getBean(StructuredBodyAdvice.class);
            MethodParameter parameter = smallBody();
            inRequest(() -> {
                for (boolean declaresLength : new boolean[] {false, true}) {
                    CountingMessage input = new CountingMessage(body, declaresLength);
                    assertThatThrownBy(() -> advice.beforeBodyRead(input, parameter, byte[].class,
                            ByteArrayHttpMessageConverter.class)).isInstanceOf(ContentPolicyViolationException.class);
                    assertThat(input.consumed()).isLessThanOrEqualTo(17);
                }
            });
        });
    }

    @Test
    void transportFailurePropagatesInReportAndIsFixedTextInEnforce() {
        IOException transport = new IOException("connection reset");
        HttpInputMessage failing = new HttpInputMessage() {
            @Override public InputStream getBody() {
                return new InputStream() {
                    @Override public int read() throws IOException { throw transport; }
                };
            }
            @Override public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            StructuredBodyAdvice advice = context.getBean(StructuredBodyAdvice.class);
            inRequest(() -> assertThatThrownBy(() -> advice.beforeBodyRead(failing, smallBody(), byte[].class,
                    ByteArrayHttpMessageConverter.class)).isSameAs(transport));
            RecordingLogger logger = context.getBean(RecordingLogger.class);
            assertThat(rules(logger)).containsExactly("inspection-incomplete");
            assertThat(logger.kinds).containsExactly("transport");
        });
        runner.run(context -> {
            StructuredBodyAdvice advice = context.getBean(StructuredBodyAdvice.class);
            inRequest(() -> assertThatThrownBy(() -> advice.beforeBodyRead(failing, smallBody(), byte[].class,
                    ByteArrayHttpMessageConverter.class)).isInstanceOf(ContentPolicyViolationException.class)
                    .hasMessage("Request body is not valid JSON"));
        });
    }

    @Test
    void overflowReplayPassesZeroByteReadsAndLeavesClosingToTheConverter() {
        byte[] body = "{\"note\":\"more than sixteen bytes\"}".getBytes(StandardCharsets.UTF_8);
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            StructuredBodyAdvice advice = context.getBean(StructuredBodyAdvice.class);
            inRequest(() -> {
                ZeroOnceStream source = new ZeroOnceStream(body, 17);
                HttpInputMessage input = new HttpInputMessage() {
                    @Override public InputStream getBody() { return source; }
                    @Override public HttpHeaders getHeaders() {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        return headers;
                    }
                };
                InputStream replay = advice.beforeBodyRead(input, smallBody(), byte[].class,
                        ByteArrayHttpMessageConverter.class).getBody();
                assertThat(replay.readAllBytes()).isEqualTo(body);
                assertThat(source.zeroReturned).isTrue();
                assertThat(source.closes).isZero();
                replay.close();
                assertThat(source.closes).isEqualTo(1);
            });
        });
    }

    @Test
    void enforceRejectionIsLoggedEvenAfterReportFindingsFillTheCap() {
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            RecordingLogger logger = context.getBean(RecordingLogger.class);
            var request = post("/mixed").contentType("application/json").content("\"<script>\"");
            for (int index = 0; index < 10; index++) request = request.param("p" + index, "<a>");
            mvc.perform(request).andExpect(status().isBadRequest());
            assertThat(logger.observations).hasSize(10);
            assertThat(logger.modes.subList(0, 9)).containsOnly(ValidationMode.REPORT);
            assertThat(logger.modes.get(9)).isEqualTo(ValidationMode.ENFORCE);
            assertThat(logger.kinds.get(9)).isEqualTo("body");
        });
    }

    @Test
    void reportStopsScalarInspectionAtALimitButStillInspectsTheBody() {
        byte[] body = "\"<script>\"".getBytes(StandardCharsets.UTF_8);
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            RecordingLogger logger = context.getBean(RecordingLogger.class);
            mvc.perform(post("/scalars").contentType("application/json").content(body)
                            .param("a", "x".repeat(21)).param("b", "<script>"))
                    .andExpect(status().isOk()).andExpect(content().bytes(body));
            assertThat(rules(logger)).containsExactly("scalar-limit", "inspection-incomplete", "R1");
        });
        runner.run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(post("/scalars").contentType("application/json").content("{}")
                            .param("a", "x".repeat(21)).param("b", "ok"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.Errors[0].code").value("REQUEST_LIMIT_EXCEEDED"));
        });
    }

    @Test
    void reportKeepsTheLastLogSlotForIncompleteInspection() {
        runner.withPropertyValues("egov.request-validation.mode=REPORT").run(context -> {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            RecordingLogger logger = context.getBean(RecordingLogger.class);
            String findings = String.join(",", Collections.nCopies(12, "\"<a>\""));
            mvc.perform(post("/bytes").contentType("application/json").content("[" + findings + "] {}"))
                    .andExpect(status().isOk());
            List<String> rules = rules(logger);
            assertThat(rules).hasSize(10);
            assertThat(rules.subList(0, 9)).containsOnly("R1");
            assertThat(rules.get(9)).isEqualTo("inspection-incomplete");
        });
    }

    @Test
    void emptySkipPathFailsStartupNamingTheHandler() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RequestValidationAutoConfiguration.class))
                .withUserConfiguration(BadSkipApplication.class)
                .withPropertyValues("egov.request-validation.enabled=true",
                        "egov.request-validation.structured-default=true")
                .run(context -> assertThat(context).getFailure()
                        .hasMessageContaining("BadSkipEndpoint#bad")
                        .hasMessageContaining("Empty skip path"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void coverageShowsSkipPathsInBracketsAndSkipsOnlyThatField(CapturedOutput output) {
        runner.run(context -> {
            assertThat(output).contains("$Endpoints#skippath index=0 enabled=true structured=true mode=ENFORCE skipPaths=[\"/note\"]");
            assertThat(output).contains("$Endpoints#bytes index=0 enabled=true structured=true mode=ENFORCE skipPaths=[]");
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(post("/skippath").contentType("application/json").content("{\"note\":\"<b>\"}"))
                    .andExpect(status().isOk());
            mvc.perform(post("/skippath").contentType("application/json").content("{\"other\":\"<b>\"}"))
                    .andExpect(status().isBadRequest());
        });
    }

    private static List<String> rules(RecordingLogger logger) {
        return logger.observations.stream().map(Violation::getRuleId).toList();
    }

    private static MethodParameter smallBody() throws NoSuchMethodException {
        return new MethodParameter(Endpoints.class.getMethod("small", byte[].class), 0);
    }

    private static void inRequest(RequestWork work) throws Exception {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest("POST", "/small")));
        try {
            work.run();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    interface RequestWork { void run() throws Exception; }

    /** getBody() returns a fresh stream per call, so a second call would duplicate the replayed prefix. */
    static final class CountingMessage implements HttpInputMessage {
        private final byte[] body;
        private final HttpHeaders headers = new HttpHeaders();
        private PositionStream last;
        int bodyCalls;

        CountingMessage(byte[] body, boolean declaresLength) {
            this.body = body;
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (declaresLength) headers.setContentLength(body.length);
        }

        @Override public InputStream getBody() {
            bodyCalls++;
            last = new PositionStream(body);
            return last;
        }

        @Override public HttpHeaders getHeaders() { return headers; }

        int consumed() { return last == null ? 0 : last.position(); }
    }

    /** Returns one 0-byte read at the given offset and counts close() calls. */
    static final class ZeroOnceStream extends ByteArrayInputStream {
        private final int zeroAt;
        boolean zeroReturned;
        int closes;

        ZeroOnceStream(byte[] body, int zeroAt) {
            super(body);
            this.zeroAt = zeroAt;
        }

        @Override public synchronized int read(byte[] buffer, int offset, int length) {
            if (!zeroReturned && pos == zeroAt && length > 0) {
                zeroReturned = true;
                return 0;
            }
            return super.read(buffer, offset, length);
        }

        @Override public void close() { closes++; }
    }

    static final class PositionStream extends ByteArrayInputStream {
        PositionStream(byte[] body) { super(body); }
        int position() { return pos; }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class BadSkipApplication {
        @Bean BadSkipEndpoint badSkipEndpoint() { return new BadSkipEndpoint(); }
    }

    @RestController
    @ValidateRequest
    static class BadSkipEndpoint {
        @PostMapping("/bad") public void bad(@ValidateRequest(skipPaths = "", reason = "test") @RequestBody byte[] body) { }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @ComponentScan(basePackages = "org.egov.requestvalidation")
    static class MvcApplication {
        @Bean Endpoints endpoints() { return new Endpoints(); }
        @Bean PlainEndpoint plainEndpoint() { return new PlainEndpoint(); }
        @Bean FixedErrorAdvice fixedErrorAdvice() { return new FixedErrorAdvice(); }
        @Bean RecordingLogger recordingLogger() { return new RecordingLogger(); }
        @Bean ExistingBodyAdvice existingBodyAdvice() { return new ExistingBodyAdvice(); }
    }

    @RestController
    @ValidateRequest
    static class Endpoints {
        int calls;
        @PostMapping("/typed") public void typed(@RequestBody TypedBody body) { calls++; }
        @PostMapping("/bytes") public byte[] bytes(@RequestBody byte[] body) { return body; }
        @PostMapping("/entity") public byte[] entity(HttpEntity<byte[]> body) { return body.getBody(); }
        @PostMapping("/small") public byte[] small(
                @ValidateRequest(maxBodyBytes = 16) @RequestBody byte[] body) { return body; }
        @PostMapping("/scalars") @ValidateRequest(maxScalarLength = 20) public byte[] scalars(
                @RequestBody byte[] body, @RequestParam("a") String a, @RequestParam("b") String b) { return body; }
        @PostMapping("/mixed") @ValidateRequest(mode = ValidationMode.REPORT) public byte[] mixed(
                @ValidateRequest(mode = ValidationMode.ENFORCE) @RequestBody byte[] body) { return body; }
        @PostMapping("/skippath") public byte[] skippath(
                @ValidateRequest(skipPaths = "/note", reason = "rich text") @RequestBody byte[] body) { return body; }
        @PostMapping("/skip") public byte[] skip(
                @ValidateRequest(structured = Structured.DISABLED, reason = "test exclusion") @RequestBody byte[] body,
                @RequestParam("count") Integer count) { return body; }
    }

    @RestController
    static class PlainEndpoint {
        @PostMapping("/plain") public byte[] plain(@RequestBody byte[] body) { return body; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TypedBody { public Long id; }

    @RestControllerAdvice
    static class FixedErrorAdvice {
        @ExceptionHandler(ContentPolicyViolationException.class)
        ResponseEntity<?> reject(ContentPolicyViolationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Errors",
                    List.of(Map.of("code", exception.getCode(), "message", exception.getMessage()))));
        }
    }

    static class RecordingLogger extends ValidationAuditLogger {
        final List<Violation> observations = new ArrayList<>();
        final List<ValidationMode> modes = new ArrayList<>();
        final List<String> kinds = new ArrayList<>();
        RecordingLogger() { super(1); }
        @Override public void violation(ValidationMode mode, Violation violation, String kind, String handler, String method) {
            observations.add(violation);
            modes.add(mode);
            kinds.add(kind);
        }
    }

    @ControllerAdvice
    static class ExistingBodyAdvice extends RequestBodyAdviceAdapter {
        int calls;
        @Override public boolean supports(MethodParameter parameter, Type type,
                Class<? extends HttpMessageConverter<?>> converter) { return true; }
        @Override public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter,
                Type type, Class<? extends HttpMessageConverter<?>> converter) {
            calls++;
            return input;
        }
    }
}
