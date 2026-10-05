# Changelog

## 0.0.1

First release: the Node.js port of `org.egov.services:request-validation` 0.0.1.

- Express middleware with six touch points: `beforeParsers`, the body-parser `verify` hooks `jsonVerify` and
  `formVerify`, `afterParsers`, `pathParams` and `multipartFields` (after a multipart parser), plus
  `inspectJsonBuffer` for JSON a service reads itself.
- REPORT (log only) and ENFORCE (HTTP 400 with the DIGIT error body) modes, per service and per route.
- JSON bodies are inspected from the raw bytes before parsing, with the Java library's rules, limits, duplicate-key
  and `RequestInfo` checks, failure locations and log lines.
- The `Content-Type` header, query and urlencoded parameters (in servlet parameter-map order, within the servlet
  container's parameter limits), the text the application's query and form parsers produce, multipart text fields
  and path parameters are inspected as scalars. Route policies are resolved on the path Express routes on.
- Content rules R1-R4 with Java 8 character semantics, jsoup 1.17.2 entity decoding, percent decoding and optional
  NFKC normalization.
- Configuration through the same 23 `EGOV_REQUEST_VALIDATION_*` environment variables as the Java library, with
  Spring-style value conversion, plus code options, a route table in place of `@ValidateRequest`, and the Node-only
  settings `excludePaths`, `maxInspectionMillis` and `slowInspectionWarnMillis`.
- Disabled unless `EGOV_REQUEST_VALIDATION_ENABLED=true`; when disabled every middleware is a no-op.
- Supports Node.js 8.4 and later, Express 4.16 and later and 5.x, body-parser 1.18 and later and 2.x, and NestJS 11.
  CommonJS, ES module and TypeScript entry points. No runtime dependencies.
