# @egovernments/request-validation

Request content validation for Express and Node.js services. It is the Node.js port of the Java library
`org.egov.services:request-validation` 0.0.1 and makes the same decisions for the same request:

- **REPORT** logs findings and changes nothing.
- **ENFORCE** rejects the request with HTTP 400 and a fixed error body.
- Input is never modified: the body, query, parameters and headers your handlers receive are exactly what they would
  receive without the package.

What is inspected:

- the JSON request body, from the raw bytes the body parser received, before `JSON.parse`;
- the `Content-Type` header;
- query and `application/x-www-form-urlencoded` parameters, names and values;
- `multipart/form-data` text fields, after the service's multipart parser;
- route path parameters.

Each string is checked against four content rules (markup start, dangerous URL schemes, event-handler attributes,
control characters), after HTML-entity and percent decoding. The JSON structure, duplicate keys and size limits are
checked as well.

This is a secondary input control. It does not replace output encoding or HTML sanitization where data is rendered.

- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Mount points](#mount-points)
- [REPORT and ENFORCE](#report-and-enforce)
- [Error response](#error-response)
- [Logging](#logging)
- [Configuration](#configuration)
- [Routes, activation and exclusions](#routes-activation-and-exclusions)
- [Limits](#limits)
- [Content rules](#content-rules)
- [Service integration](#service-integration)
- [Rollout](#rollout)
- [Differences from the Java library](#differences-from-the-java-library)
- [API](#api)

## Requirements

| item | supported |
|---|---|
| Node.js | 8.4.0 and later |
| Express | 4.16 and later, and 5.x |
| body-parser (or `express.json` / `express.urlencoded`) | 1.18 and later, and 2.x |
| NestJS | 11, with `@nestjs/platform-express` |
| Modules | CommonJS (`require`) and ES modules (`import`); TypeScript types included |

The package has no runtime dependencies and no install scripts.

## Quick start

```sh
npm install @egovernments/request-validation
```

```js
const express = require('express');
const bodyParser = require('body-parser');
const multer = require('multer');                                           // only where a route takes multipart
const { createRequestValidation } = require('@egovernments/request-validation');

const rv = createRequestValidation({
  logger: { warn: (line) => logger.warn(line), info: (line) => logger.info(line) }
});

const app = express();
app.use(rv.beforeParsers);                                                  // 1. before the first body parser
app.use(bodyParser.json({ limit: '2mb', verify: rv.jsonVerify }));          // 2. every JSON parser gets jsonVerify
app.use(bodyParser.urlencoded({ extended: true, verify: rv.formVerify }));  //    every form parser gets formVerify
app.use(rv.afterParsers);                                                   // 3. directly after the last parser
app.post('/items/:id/_update', rv.pathParams, handler);                     // 4. only on routes with path parameters
app.post('/upload', multer().none(), rv.multipartFields, handler);          // 5. after every multipart parser
```

Then switch it on with environment variables:

```sh
EGOV_REQUEST_VALIDATION_ENABLED=true
EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT=true
EGOV_REQUEST_VALIDATION_ACTIVATION=ALL
EGOV_REQUEST_VALIDATION_MODE=REPORT
```

The package is **disabled by default**. Without `EGOV_REQUEST_VALIDATION_ENABLED=true` (or `enabled: true` in the
options) every middleware is a no-op and nothing is validated, so the code can be merged before it is switched on.

## Mount points

| touch point | where | what it does |
|---|---|---|
| `beforeParsers` | `app.use`, before the first body parser (after static file middleware) | Matches the request to its policy. Inspects the `Content-Type` header and the query string (for a form body this waits for `formVerify`, so query and form parameters are inspected together). For a JSON body it checks the declared charset and `Content-Length` before the body is read. |
| `jsonVerify` | the `verify` option of every JSON body parser | Inspects the exact body bytes the parser received (after inflating `Content-Encoding`), before they are parsed. |
| `formVerify` | the `verify` option of every urlencoded body parser | Inspects the query and form parameters. |
| `afterParsers` | `app.use`, directly after the last body parser and before your routes and error handlers | An array of two middlewares. The first inspects what has not been inspected yet. The second is an error middleware: it turns a rejection thrown from a verify hook into the 400 response, and handles aborted JSON uploads. Mount the array as it is. |
| `pathParams` | first handler of every route that has path parameters | Inspects `req.params`. |
| `multipartFields` | directly after every multipart parser (multer, busboy, express-fileupload), on the route or with `app.use` | Inspects the text fields the parser put on `req.body`, names and values, as query/form parameters (Java inspects them through the servlet parameter map). File parts are not inspected, as in Java. |
| `inspectJsonBuffer(req, buffer)` | your own code | For JSON that your code reads itself, for example a gzip-compressed body. Throws a `RequestValidationError` in ENFORCE. |

Notes:

- Express recognises error middleware by its number of parameters. `afterParsers[1]` takes four parameters, every
  other middleware three, and the verify hooks four. Do not wrap them in functions with a different number of
  parameters.
- `express.json()` and `express.urlencoded()` are body-parser, so they accept `verify` the same way.
- A JSON or urlencoded parser without the hook still has its result checked from the parsed object, with reduced
  accuracy, and the package logs `request_validation_hook_missing parser=<json|urlencoded>` once. Add the hook to
  every such parser, including parsers with a custom `type`.
- Bodies read by other parsers (raw, text) are not inspected; their `Content-Type` header and query string are. When
  such a parser takes a JSON or urlencoded body, the same warning is logged; a text parser's JSON string is then
  checked as one value and its urlencoded string as form parameters, and a raw parser's bytes are not checked.
- Multipart text fields are inspected only where `multipartFields` runs after the multipart parser. A multipart body
  that no parser reads is not inspected (the application never receives its fields).
- The package never reads the request stream itself, never assigns to `req.body`, `req.query`, `req.params`,
  `req.headers` or `req.url`, and keeps its per-request state under a private symbol.

## REPORT and ENFORCE

**REPORT** (the default mode) never rejects and never changes a request or response.

- Every content finding is logged, and inspection continues.
- A structural or limit finding (malformed JSON, a duplicate key, a charset other than UTF-8, a size limit) is logged
  together with one `inspection-incomplete` line, and inspection of that part of the request stops.
- Body-parser's own errors (malformed JSON, 413, 415) reach your error handling unchanged.

**ENFORCE** ends the request at the first finding of any kind, in this order: `Content-Type` header, query and form
parameters, the body in document order, then multipart text fields and path parameters (route middleware: they are
only known once the route runs).

The mode can be set for the whole service and per route (see [Routes](#routes-activation-and-exclusions)).

## Error response

In ENFORCE, a rejected request gets:

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
Connection: close

{"ResponseInfo":null,"Errors":[{"code":"REQUEST_CONTENT_NOT_ALLOWED","message":"Request contains content that is not allowed","description":null,"params":null}]}
```

| code | message |
|---|---|
| `REQUEST_CONTENT_NOT_ALLOWED` | Request contains content that is not allowed |
| `REQUEST_JSON_MALFORMED` | Request body is not valid JSON |
| `REQUEST_LIMIT_EXCEEDED` | Request exceeds an allowed size limit |
| `REQUEST_JSON_DUPLICATE_KEY` | Request body contains a duplicate property |

- The body has the same keys as the DIGIT `ErrorRes` envelope, and is exactly what the Java library's services send.
- It never contains the request text, the rule or the location; those are only in the log.
- `Connection: close` is sent because the request body may be left unread.

If the package itself fails while inspecting (an internal error), REPORT lets the request through and logs
`request_validation_internal_error`; ENFORCE answers 500:

```json
{"ResponseInfo":null,"Errors":[{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error","description":null,"params":null}]}
```

To send a service-specific envelope instead, pass `respond`:

```js
createRequestValidation({
  respond: (req, res, error) => {          // error: { status: 400 | 500, code, message }
    res.statusCode = error.status;
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(myEnvelope(error.code, error.message)));
  }
});
```

`req` and `res` are Node's `IncomingMessage` and `ServerResponse`; to call Express helpers such as `res.status()` from
TypeScript, cast `res` to `express.Response` first.

`RequestValidationError` (thrown by `inspectJsonBuffer`, and by the verify hooks for the error middleware) has
`status` and `statusCode` 400 (500 for an internal error), `expose`, `code` and `message`; `new
RequestValidationError(code)` takes one of the four codes or `INTERNAL_SERVER_ERROR`. Recognise it with
`isRequestValidationError(err)`; this works across copies of the package.

## Logging

One `WARN` line per finding, through `logger.warn`:

```text
request_validation mode=ENFORCE code=REQUEST_CONTENT_NOT_ALLOWED rule=R1 location=/RequestInfo/userInfo/name kind=body handler=POST /_create method=POST length=8
```

| field | meaning |
|---|---|
| `mode` | `REPORT` or `ENFORCE` |
| `code` | one of the four codes above |
| `rule` | `R1`-`R4` for content, or a structural or limit rule: `json`, `trailing-content`, `duplicate-key`, `dual-request-info`, `charset`, `max-body-bytes`, `max-depth`, `max-string-length`, `max-name-length`, `max-number-length`, `max-tokens`, `parser-limit`, `scalar-limit`, `body-read`, `inspection-incomplete`, `inspection-budget` |
| `location` | JSON-pointer-like path for the body (`/` is the root), the parameter name for scalars, `/Content-Type` for the header. Any segment that is not 1-64 characters of `A-Z a-z 0-9 _ . -` is written as `*` |
| `kind` | `body`, `query/form`, `path`, `header` or `transport` |
| `handler` | the matched route's `name`, else the request path, where every segment that is not 1-64 characters of `A-Z a-z 0-9 _ . -` is written as `*` |
| `length` | length of the offending value in UTF-16 units, or 0 |

- Parameter and body values and their decoded variants are never logged. The request path can be: for a request
  that matches no route with a `name`, `handler` is the path with every segment that is not 1-64 characters of
  `A-Z a-z 0-9 _ . -` written as `*`, so an identifier in such a segment appears in the log. Give routes a `name` to
  log a fixed label instead.
- `rule`, `location`, `handler`, `method` and every `logContext` key and value are cut to 256 characters, and any
  character outside printable ASCII, and `<` and `>`, is written as `_`.
- At most ten lines per request: nine findings and one `inspection-incomplete` line. In ENFORCE the rejecting finding
  is always logged.
- `EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE` (0 to 1) samples REPORT lines; ENFORCE lines are never sampled.
- `logContext: (req) => ({ correlationId: req.headers['x-correlation-id'] })` appends ` key=value` pairs (for
  example a correlation id) to every finding line.

Other lines:

| level | line |
|---|---|
| INFO | `request_validation_config enabled=true activation=… mode=… structuredDefault=… jar_sha256=…` at startup |
| INFO | `request_validation_coverage handler=… enabled=… mode=…`, one per route and exclusion, at startup |
| INFO / WARN | `request_validation_exclusion handler=… reason=…`, or `request_validation_exclusion_missing_reason handler=…` for an exclusion without a reason |
| WARN | `request_validation_nothing_validated`: activation is `ANNOTATED` and no route is enabled |
| WARN | `request_validation_hook_missing parser=json\|urlencoded`, once |
| WARN | `request_validation_slow_inspection phase=… ms=… bytes=… handler=…` when one inspection step took longer than `slowInspectionWarnMillis` |
| ERROR | `request_validation_internal_error phase=… error=<error class>`, at most once per request |

The default logger is `console` (`console.warn`, `console.info`, `console.error`). Pass `logger` to use the
service's own logger; `info` and `error` are optional.

## Configuration

Settings come from, in order of precedence:

1. environment variables (`EGOV_REQUEST_VALIDATION_*`), read from `process.env`, or from `options.env`
   (`env: false` ignores the environment);
2. the options passed to `createRequestValidation(options)`;
3. the defaults, which are the Java library's.

When the package is enabled, every setting is validated and an invalid value throws from
`createRequestValidation`, so the service does not start. When it is disabled, nothing is validated.

### Environment variables

| variable | default | values |
|---|---|---|
| `EGOV_REQUEST_VALIDATION_ENABLED` | `false` | only `true` (any case) enables |
| `EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT` | none | **required when enabled**: `true` inspects JSON bodies by default, `false` only where a route says `structured: true` |
| `EGOV_REQUEST_VALIDATION_ACTIVATION` | `ANNOTATED` | `ALL` or `ANNOTATED` (see [activation](#routes-activation-and-exclusions)); use `ALL` |
| `EGOV_REQUEST_VALIDATION_MODE` | `REPORT` | `REPORT` or `ENFORCE` |
| `EGOV_REQUEST_VALIDATION_INSPECT_CONTENT_TYPE_HEADER` | `true` | boolean |
| `EGOV_REQUEST_VALIDATION_REJECT_DUPLICATE_KEYS` | `true` | boolean |
| `EGOV_REQUEST_VALIDATION_REJECT_DUAL_REQUEST_INFO` | `true` | boolean: rejects a body with both `RequestInfo` and `requestInfo` at the top level |
| `EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE` | `1.0` | 0 to 1 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_BODY_BYTES` | `10485760` | 1 to 2147483646 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH` | `64` | 1 to 256 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_STRING_LENGTH` | `1000000` | 1 to 2147483646 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_NAME_LENGTH` | `256` | 1 to 2147483646 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_TOKENS` | `2000000` | 1 to 2147483647 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_NUMBER_LENGTH` | `1000` | 1 to 2147483646 |
| `EGOV_REQUEST_VALIDATION_LIMITS_MAX_SCALAR_LENGTH` | `65536` | 1 to 2147483646 |
| `EGOV_REQUEST_VALIDATION_RULES_MARKUP_START` | `true` | boolean (rule R1) |
| `EGOV_REQUEST_VALIDATION_RULES_URL_SCHEME` | `true` | boolean (rule R2) |
| `EGOV_REQUEST_VALIDATION_RULES_EVENT_HANDLER` | `true` | boolean (rule R3) |
| `EGOV_REQUEST_VALIDATION_RULES_DISALLOWED_CONTROLS` | `0` | comma list of control codes 0-31 except 9, 10 and 13 (rule R4); replaces the default |
| `EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES` | `javascript,vbscript` | comma list; replaces the default; an empty value denies no scheme |
| `EGOV_REQUEST_VALIDATION_RULES_DENIED_DATA_MEDIA_TYPES` | `text/html,application/xhtml+xml,image/svg+xml` | comma list of `data:` media types; replaces the default |
| `EGOV_REQUEST_VALIDATION_RULES_DECODE_ROUNDS` | `2` | 0 to 3 rounds of entity/percent decoding before the rules run |
| `EGOV_REQUEST_VALIDATION_RULES_NORMALIZE_NFKC` | `false` | boolean |
| `EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS` | empty | Node only. Comma list of path prefixes that are never inspected; replaces `excludePaths` from the options. Each logs a missing-reason warning, because a reason cannot be given here |
| `EGOV_REQUEST_VALIDATION_NODE_MAX_INSPECTION_MILLIS` | unset | Node only. Integer of at least 1; see [Limits](#limits) |
| `EGOV_REQUEST_VALIDATION_NODE_SLOW_INSPECTION_WARN_MILLIS` | `250` | Node only. Integer of at least 0 (0 turns the warning off) |

The first 23 variables are the Java library's, with the same names and defaults.

Values are read as Spring Boot reads them:

- `${NAME}` and `${NAME:default}` placeholders are resolved against the same environment.
- Booleans: `true`, `on`, `yes`, `1` and `false`, `off`, `no`, `0`, in any case.
- Integers: decimal, or hexadecimal with a `0x`, `0X` or `#` prefix (`0x10` is 16). Whitespace is ignored. Digits
  are read as Java reads them, so every Unicode decimal digit counts (`٣` is 3).
- `REPORT`, `ENFORCE`, `ALL` and `ANNOTATED` ignore case and the characters `-` and `_` (`enforce` is `ENFORCE`).
- Lists are comma separated; each item is trimmed; an empty item is an error.
- An empty or blank value counts as not set (the next source or the default applies). For a list, an empty value
  (`VAR=`) is an empty list.
- Only the `EGOV_REQUEST_VALIDATION_*` spelling is read.

### Options

The full type is `Options` in `index.d.ts`:

```ts
interface Options {
  enabled?: boolean;
  structuredDefault?: boolean;
  activation?: 'ANNOTATED' | 'ALL';
  mode?: 'REPORT' | 'ENFORCE';
  inspectContentTypeHeader?: boolean;
  rejectDuplicateKeys?: boolean;
  rejectDualRequestInfo?: boolean;
  log?: { reportSampleRate?: number };
  limits?: Partial<Limits>;          // maxBodyBytes, maxDepth, maxStringLength, maxNameLength, maxTokens, maxNumberLength, maxScalarLength
  rules?: Partial<Rules>;            // markupStart, urlScheme, eventHandler, disallowedControls, deniedSchemes, deniedDataMediaTypes, decodeRounds, normalizeNfkc
  routes?: RoutePolicy[];
  excludePaths?: Array<string | ExcludePath>;
  maxInspectionMillis?: number;
  slowInspectionWarnMillis?: number;
  logger?: Logger;                   // { warn(line), info?(line), error?(line) }
  logContext?: (req: IncomingMessage) => { [key: string]: string | number | boolean | null | undefined } | undefined;
  respond?: (req: IncomingMessage, res: ServerResponse, error: { status: 400 | 500; code: string; message: string }) => void;
  env?: { [name: string]: string | undefined } | false;
}
```

Every environment variable overrides the matching option, field by field. Routes, `logger`, `logContext` and
`respond` exist only as options. Unknown option names fail startup, so a misspelt option is not silently ignored.

The effective configuration is available as `rv.config` (frozen).

## Routes, activation and exclusions

Express has no annotations, so the Java `@ValidateRequest` declarations become a route table:

```js
createRequestValidation({
  routes: [
    // class-level declaration: everything under /mdms-v2/v2
    { path: '/mdms-v2/v2', prefix: true, mode: 'REPORT', reason: 'rollout' },
    // method-level declaration: one endpoint
    { path: '/mdms-v2/v2/_create', method: 'POST', name: 'MdmsController#create',
      skipPaths: ['/Mdms/data/help'], reason: 'help text is HTML',
      limits: { maxDepth: 32 } }
  ],
  excludePaths: [{ path: '/tracing', reason: 'reverse proxy, not an API handler' }]
});
```

| field | meaning |
|---|---|
| `path` | raw (undecoded) request path; must start with `/` |
| `prefix` | `true`: the entry covers `path` and everything below it (a class-level declaration). Default `false`: exactly `path` (a method-level declaration) |
| `method` | HTTP method; a `GET` entry also covers `HEAD`. Omitted: every method |
| `name` | handler name in the log; default `"<METHOD or *> <path>"` |
| `enabled` | `false` switches inspection off for the matched requests |
| `structured` | `false` skips the JSON body (the `Content-Type` header, query, form and path parameters are still inspected) |
| `mode` | `REPORT` or `ENFORCE` for the matched requests |
| `skipPaths` | JSON pointers of body subtrees whose content is not checked (`~0` is `~`, `~1` is `/`, `*` matches one whole segment; `/` is the property with the empty name). Structure and limits are still checked |
| `limits` | any of the seven limits; each field inherits independently |
| `reason` | why the entry weakens inspection; logged at startup. Entries with `enabled: false`, `structured: false` or `skipPaths` and no reason log a warning |

Matching uses the path Express routes the request on: the raw (undecoded) path before the query or, for a request
target with a fragment, whitespace or a backslash, the path Express derives from it (fragment removed, ends trimmed,
`\` read as `/`). Like Express's route matching it ignores case (non-ASCII letters included) and an optional
trailing slash, and prefix entries match only at a `/` boundary, so a different spelling of a path cannot pick
another policy than the route Express runs. Policies are merged in this order:

1. the service settings;
2. the longest matching prefix entry (at equal length, one with a matching `method` wins);
3. the matching exact entry (one with a matching `method` wins over one without).

`skipPaths` from both entries are combined. Two entries with the same method, path and kind fail startup.

**Activation.** `ALL` inspects every request except those switched off by a route or excluded; use it for every Node
service. `ANNOTATED` (the Java default) inspects only requests that match a route entry; with no enabled route it
logs `request_validation_nothing_validated`.

**Exclusions.** `excludePaths` lists path prefixes that are never inspected, for example a reverse proxy. Requests
that are not API handlers but are served by the same app (static files) are best left in front of
`beforeParsers`.

## Limits

| limit | default | ceiling | checked against |
|---|---|---|---|
| `maxBodyBytes` | 10485760 | 2147483646 | JSON body size in bytes; also the declared `Content-Length` of a JSON request, and the total characters of all scalars of a request |
| `maxDepth` | 64 | 256 | JSON nesting depth |
| `maxStringLength` | 1000000 | 2147483646 | characters of one JSON string |
| `maxNameLength` | 256 | 2147483646 | characters of one JSON property name |
| `maxTokens` | 2000000 | 2147483647 | JSON tokens; also the number of scalars of a request |
| `maxNumberLength` | 1000 | 2147483646 | characters of one JSON number |
| `maxScalarLength` | 65536 | 2147483646 | characters of one header value, parameter name or value |

A value equal to its limit passes; one more fails with `REQUEST_LIMIT_EXCEEDED`.

- Your body parser's own `limit` applies first: a body above it gets the parser's 413. Set the parser limit and
  `maxBodyBytes` together.
- A JSON request whose declared `Content-Length` is above `maxBodyBytes` is answered at once in ENFORCE, without
  reading the body.
- Urlencoded parameters follow the servlet container's limits: the parameter map holds the first 10,000 parameters
  of the query and the body together, and no body parameters when the body is larger than 2 MiB, as in Java. The
  parameters past those limits are still inspected, because the application receives them, but only once the body
  parser has accepted the body; a body the parser rejects (for example over its `parameterLimit`) costs no inspection.
- Inspection runs synchronously on the event loop. An ordinary 1 MB JSON body takes on the order of 10-20 ms; a body
  full of entity or percent escapes, which are decoded before the rules run, on the order of 100-200 ms per megabyte
  (depending on CPU and Node version); urlencoded parameters cost about the same per megabyte as the body parser's
  own parsing. `slowInspectionWarnMillis` (default 250) logs slow requests. `maxInspectionMillis` (Node only, off by
  default) stops inspection of a JSON body or of the parameters after that many milliseconds with
  `REQUEST_LIMIT_EXCEEDED` and rule `inspection-budget`: a 400 in ENFORCE, a logged incomplete inspection in REPORT.

## Content rules

| rule | finds |
|---|---|
| R1 | `<` followed by a letter, `/`, `!` or `?` (the start of markup) |
| R2 | a value that starts with a denied URL scheme (`javascript:`, `vbscript:`), after ignoring leading spaces, controls, tabs and line breaks and case, or a `data:` URL with a denied media type |
| R3 | an event-handler attribute (`onclick`, `onerror`, … from the HTML event-handler list) followed by `=` |
| R4 | a disallowed control character (NUL by default) |

Every rule also runs on decoded variants of the value: HTML entities (decoded as jsoup decodes them) and percent
escapes, up to `decodeRounds` rounds, and NFKC normalization when `normalizeNfkc` is on. Character classes follow
Java 8. Legitimate content can match these rules (HTML in rich-text fields, `a < b` in prose with a letter after
`<`); run REPORT first and use `skipPaths` with a `reason` for fields that are meant to carry markup.

## Service integration

Install the same version in every service. The changes below are the whole integration; the service's routes and
handlers do not change.

### project-factory (Express 4, TypeScript)

New file `src/server/utils/requestValidation.ts`:

```ts
import { createRequestValidation } from "@egovernments/request-validation";
import { logger } from "./logger";

export const requestValidation = createRequestValidation({
  logger: { warn: (line: string) => { logger.warn(line); }, info: (line: string) => { logger.info(line); } },
  excludePaths: [{ path: "/tracing", reason: "Jaeger UI reverse proxy (http-proxy-middleware), not an API handler" }],
});
```

`src/server/app.ts`:

- import: `import { requestValidation } from "./utils/requestValidation";`
- first statement of `initializeMiddlewares()`: `this.app.use(requestValidation.beforeParsers);`
- `bodyParser.json({ limit: config.app.incomingRequestPayloadLimit, verify: requestValidation.jsonVerify })`
- `bodyParser.urlencoded({ ..., verify: requestValidation.formVerify })`
- after the urlencoded parser, before `tracingMiddleware`: `this.app.use(requestValidation.afterParsers);`

`src/server/utils/gzipHandler.ts` (required: the service parses `application/gzip` JSON itself, so without this a
compressed body would not be inspected). Import `requestValidation`, and replace the decompress-and-parse block with:

```ts
const gzipBuffer = Buffer.concat(buffers as Uint8Array[]);
let raw: Buffer;
try {
    raw = await new Promise<Buffer>((resolve, reject) =>
        zlib.gunzip(gzipBuffer as Uint8Array, (err, result) => (err ? reject(err) : resolve(result))));
} catch (err: any) {
    throw new Error(`Failed to process Gzip data: ${err.message}`);
}
requestValidation.inspectJsonBuffer(req, raw); // ENFORCE: throws a 400 RequestValidationError
try {
    req.body = JSON.parse(raw.toString());
} catch (parseErr) {
    throw new Error("Failed to process Gzip data: Invalid JSON format in decompressed data");
}
```

The error thrown in ENFORCE carries `status: 400`, `code` and `message`; the request middleware's catch hands it to
`errorResponder`, which renders the same `{ResponseInfo, Errors}` body. `/health` is served outside Express and is
not affected; `/tracing` is excluded.

### boundary-management (Express 4, TypeScript)

The same `src/server/utils/requestValidation.ts` (`logger` from `./logger`). In `src/server/app.ts`: import it, add
`this.app.use(requestValidation.beforeParsers);` as the first middleware, `verify: requestValidation.jsonVerify` and
`verify: requestValidation.formVerify` on the two body parsers, and `this.app.use(requestValidation.afterParsers);`
after the urlencoded parser, before `requestMiddleware`. `/tracing` is excluded as above. The service accepts
`application/gzip` at its content-type check but never decompresses it, so it needs no `inspectJsonBuffer` call.

### pdf-service (Express 4, Babel)

`src/index.js`:

```js
import { createRequestValidation } from "@egovernments/request-validation";

const requestValidation = createRequestValidation({
  logger: { warn: (line) => logger.warn(line), info: (line) => logger.info(line) },
});

// after app.use(express.static(...)), so static files stay uninspected
app.use(requestValidation.beforeParsers);
app.use(bodyParser.json({ limit: "200mb", extended: true, verify: requestValidation.jsonVerify }));
app.use(bodyParser.urlencoded({ limit: "200mb", extended: true, parameterLimit: 50000, verify: requestValidation.formVerify }));
app.use(requestValidation.afterParsers);
```

The Kafka consumer path is not HTTP and is not inspected. The parser accepts 200 MB while `maxBodyBytes` defaults to
10 MiB: size `EGOV_REQUEST_VALIDATION_LIMITS_MAX_BODY_BYTES` from the `rule=max-body-bytes` lines REPORT produces
before switching to ENFORCE. Urlencoded bodies above 2 MiB are inspected after the parser has read them, within the
same scalar budget (`maxBodyBytes` characters, `maxTokens` strings; `rule=scalar-limit` when exceeded); consider
`maxInspectionMillis` as well.

### Other Express services

The same four touch points, on each `bodyParser.json` / `express.json` and `bodyParser.urlencoded` /
`express.urlencoded` parser. Services that only proxy requests (for example to Kibana) do not need the package. Use
`respond` only if a service must keep its own error envelope.

### ES modules

```js
import { createRequestValidation } from '@egovernments/request-validation';
```

### TypeScript

Types ship with the package (`index.d.ts`). The middlewares are typed with Node's `IncomingMessage` and
`ServerResponse`, and can be passed to `app.use` and to body-parser's `verify` with `@types/express` under `strict`.

### NestJS 11

Turn off Nest's own body parsers and mount the package's touch points with the hooked parsers, before
`app.listen()`:

```ts
import { NestFactory } from "@nestjs/core";
import { NestExpressApplication } from "@nestjs/platform-express";
import { json, urlencoded } from "express";
import { AppModule } from "./app.module";
import { requestValidation } from "./requestValidation";

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule, { bodyParser: false });
  app.use(requestValidation.beforeParsers);
  app.use(json({ limit: "1mb", verify: requestValidation.jsonVerify }));
  app.use(urlencoded({ extended: true, verify: requestValidation.formVerify }));
  app.use(requestValidation.afterParsers);
  await app.listen(3000);
}
bootstrap();
```

- `app.useBodyParser("json", { verify: requestValidation.jsonVerify })` works as well, but not together with
  `rawBody: true`: Nest then replaces `verify` with its own. A service that needs `req.rawBody` keeps the `app.use`
  form and sets it in the hook: `verify: (req, res, buf, enc) => { (req as any).rawBody = buf; requestValidation.jsonVerify(req, res, buf, enc); }`.
- Routes with path parameters: apply `pathParams` as a middleware for them, for example in the module's
  `configure(consumer)`: `consumer.apply(requestValidation.pathParams).forRoutes({ path: "items/:id", method: RequestMethod.GET })`.
- Nest's exception filters and 404 handling are unchanged.
- Multipart text fields: `FileInterceptor` and the other multer interceptors parse the body inside the route, after
  every middleware, so run `multipartFields` in an interceptor listed after them. In ENFORCE a rejected request gets
  the package's 400 response and the handler does not run.

```ts
import { CallHandler, ExecutionContext, Injectable, NestInterceptor } from "@nestjs/common";
import { Observable } from "rxjs";
import { requestValidation } from "./requestValidation";

@Injectable()
export class RequestValidationMultipart implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const http = context.switchToHttp();
    return new Observable((subscriber) => {
      requestValidation.multipartFields(http.getRequest(), http.getResponse(), (err?: unknown) => {
        if (err) {
          subscriber.error(err);
          return;
        }
        next.handle().subscribe(subscriber);
      });
    });
  }
}

// @Post("upload") @UseInterceptors(FileInterceptor("file"), RequestValidationMultipart)
```

### Express 5

No change: Express 5 and body-parser 2 pass the same `verify` arguments. The package never assigns `req.query` (a
read-only getter in Express 5); it reads it only when the query string has a parameter with an empty name, which
Express 5's default query parser keeps. It handles `req.body` being `undefined` when no parser ran.

### Helm values

```yaml
EGOV_REQUEST_VALIDATION_ENABLED: "true"
EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT: "true"
EGOV_REQUEST_VALIDATION_ACTIVATION: "ALL"
EGOV_REQUEST_VALIDATION_MODE: "REPORT"
```

Leave a variable out rather than setting it to an empty value, except for a list that should be empty. The package
treats an empty value as not set; a Spring Boot 2.2 service fails to start on an empty boolean. One exception:
`EGOV_REQUEST_VALIDATION_ENABLED`, when present, decides on its own, so an empty value turns the package **off** even
when the code passes `enabled: true`. Never ship it empty; set it to `true` or leave it out.

## Rollout

1. Deploy with `MODE=REPORT` in a non-production environment with real traffic, for at least two weeks.
2. Review every `request_validation` line. A true positive stays. A false positive gets a route `skipPaths` entry
   with a `reason`.
3. Switch to `ENFORCE` per service, or per route with `routes[].mode`.

A code change only takes effect once the service image is rebuilt with the package and deployed.

## Differences from the Java library

The decisions (status and error code) and the log lines are the Java library's for every request both stacks deliver
to the application. Where Node and the servlet stack differ, the package inspects what the Node application
receives, so it is stricter, or the difference is the host's:

| topic | Java service | Node package |
|---|---|---|
| Character data | the running JVM's (Java 17: U+180E is not whitespace) | Java 8 tables, so U+180E-prefixed schemes count as R2 |
| Malformed query or form pairs (invalid `%` escapes) | the servlet container drops the pair | the text Express's parsers (qs, querystring) hand the application is inspected as well |
| Parameters with an empty name (`?=value`) | dropped by the container | inspected when the application's parser keeps them (Node's querystring: Express 5's query parser, body-parser 1 with `extended: false`) |
| Pairs that qs splits at `]=` (`a[x=y]=value`) | one parameter `a[x` with value `y]=value` | the qs name and value are inspected as well |
| More than 10,000 parameters, or a urlencoded body over 2 MiB | the container ignores the parameters past its limit | inspected once the body parser has accepted the body |
| Charset names `utf8`, `unicode-1-1-utf-8` | treated as UTF-8 | body-parser answers 415 before inspection |
| Unknown charset names | Spring answers 400 `UnsupportedMediaType` | 400 `REQUEST_JSON_MALFORMED` |
| `Content-Encoding: gzip` JSON | rejected as malformed (not inflated) | inflated by body-parser and inspected |
| Declared `Content-Length` above the limit | answered after the container's read timeout | answered at once |
| Body parser `limit` reached first | — | the parser's 413 |
| Raw `<` or bytes ≥ 0x80 in the request line | rejected by the container | `<` is delivered by Node and inspected; bytes ≥ 0x80 are rejected by Node 20 and later, and delivered and inspected on older runtimes |
| Requests Spring rejects before its handler (unknown method or route, `consumes` mismatch, unparsable `Content-Type`) | host error, not inspected | Express parses and inspects before routing, so the package may answer 400 first |
| Bodies of GET requests | not read by GET handlers | inspected when a body parser parses them |
| Path parameters | inspected before the body | inspected after the body (route middleware) |
| Multipart form fields | text fields are in the servlet parameter map and inspected | inspected by `multipartFields` after the application's multipart parser; a multipart body that no parser reads is not inspected |
| Upper-case form media type (`APPLICATION/X-WWW-FORM-URLENCODED`) | the container does not parse the body | body-parser parses it, and it is inspected |
| `;` parameters in a path segment (`/items/a;x=1`) | removed before route matching | kept in `req.params` by Express, and inspected |
| Unparsable JSON `Content-Type` (`application/json;;=`) | Spring answers 400 `UnsupportedMediaType` | body-parser 1 skips the body; body-parser 2 parses it, and it is inspected |
| `Content-Encoding: br` JSON | rejected as malformed | body-parser 1 answers 415; body-parser 2 inflates it, and it is inspected |
| Urlencoded body with `charset=ISO-8859-1` | parsed | body-parser 1 answers 415 |
| An empty boolean environment variable | Spring Boot 2.2 fails to start | treated as not set, except `EGOV_REQUEST_VALIDATION_ENABLED`: empty turns the package off |
| Log `handler` | `Class#method` | route `name`, or the request path with unsafe segments as `*` |
| Internal error of the library | propagates | REPORT lets the request through, ENFORCE answers 500 |
| Activation | `@ValidateRequest` annotations | the route table; `ALL` also inspects unmatched paths |
| Node-only settings | — | `maxInspectionMillis`, `slowInspectionWarnMillis`, `excludePaths` |
| A parser without the verify hook | — | the parsed object is checked with reduced accuracy, with a warning |

## API

```js
const {
  createRequestValidation,   // (options?) => RequestValidation
  RequestValidationError,    // class: status, statusCode, expose, code, message
  isRequestValidationError,  // (err) => boolean
  VIOLATION_MESSAGES,        // { [code]: message }
  CONFORMS_TO                // { jarSha256, jsoup, jacksonCore, javaCharacterData }: the Java reference this release matches
} = require('@egovernments/request-validation');
```

`RequestValidation`:

| member | |
|---|---|
| `enabled` | whether the package is on |
| `config` | the effective configuration |
| `beforeParsers`, `jsonVerify`, `formVerify`, `afterParsers`, `pathParams`, `multipartFields` | the touch points |
| `inspectJsonBuffer(req, buffer)` | inspect JSON your code read itself |
| `detect(value)` | `'R1'`-`'R4'` or `null` for one string, with the service's rules |
| `inspectJson(buffer, options?)` | `{ findings, failure }` for one JSON document; never logs or throws for findings |

The core classes (`ContentDetector`, `ContentPolicy`, `InspectionLimits`, `JsonDocumentInspector`, `SkipPathMatcher`,
`Violation`, `ViolationCode`, `InspectionError`, `SafeLocationFormatter`) are exported, with types, for tests and
tools.

## License

MIT. Third-party notices (jsoup, Jackson, Unicode data) are in `NOTICE`.
