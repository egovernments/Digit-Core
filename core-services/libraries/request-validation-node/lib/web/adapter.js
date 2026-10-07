'use strict';

// The Express/Node adapter. The touch points of a service:
//   app.use(rv.beforeParsers)                                    before the first body parser
//   bodyParser.json({ verify: rv.jsonVerify }), bodyParser.urlencoded({ verify: rv.formVerify })
//   app.use(rv.afterParsers)                                     directly after the last parser: [finisher, errorArm]
//   router.METHOD('/x/:id', rv.pathParams, handler)              only on routes with path parameters
//   router.METHOD('/x', multipartParser, rv.multipartFields, h)  after a multipart parser (multer, busboy)
// The JSON body is inspected from the exact bytes body-parser hands to its verify hook, before JSON.parse; scalars
// (the Content-Type header, query and form parameters, multipart text fields, path parameters) follow the servlet
// parameter map. The adapter never assigns to req.body, req.query, req.params, req.headers or req.url and never
// reads the request stream.

var ViolationCode = require('../core/ViolationCode');
var Violation = require('../core/Violation');
var InspectionError = require('../core/InspectionError');
var SafeLocationFormatter = require('../core/SafeLocationFormatter');
var FlaggedValue = require('../core/FlaggedValue');
var mediaType = require('./mediaType');
var parameters = require('./parameters');
var errorResponse = require('./errorResponse');
var safeText = require('./auditLogger').safeText;

var CONTENT = ViolationCode.REQUEST_CONTENT_NOT_ALLOWED;
var MALFORMED = ViolationCode.REQUEST_JSON_MALFORMED;
var LIMIT = ViolationCode.REQUEST_LIMIT_EXCEEDED;
var RequestValidationError = errorResponse.RequestValidationError;
var isRequestValidationError = errorResponse.isRequestValidationError;

var BUDGET_SCALARS = 256;
var BUDGET_LONG_TEXT = 4096;
var EMPTY_NAME_LOCATION = SafeLocationFormatter.scalar('');

function nowMillis() {
  var t = process.hrtime();
  return t[0] * 1e3 + t[1] / 1e6;
}

// Thrown internally when the scalar time budget runs out.
function BudgetExpired(location) {
  this.location = location;
}

/** Spring calls the body advice only for a non-empty body: a positive Content-Length, or a chunked body. */
function jsonBodyPresent(req) {
  var headers = req.headers;
  if (headers['transfer-encoding'] !== undefined) {
    return true;
  }
  var length = Number(headers['content-length']);
  return isFinite(length) && length > 0;
}

function declaredLength(req) {
  var value = req.headers['content-length'];
  if (value === undefined) {
    return -1;
  }
  var length = Number(value);
  return isFinite(length) ? length : -1;
}

/** Whether a body parser consumed the body: body-parser 1.x sets req._body; 2.x leaves req.body set and the
 * request stream ended. */
function bodyParsed(req) {
  if (req._body === true) {
    return true;
  }
  return req.body !== undefined && req.readableEnded === true;
}

/** Bytes from a raw parser (a Buffer or another binary view): not a parsed document, never walked. */
function isBinaryBody(body) {
  return body !== null && typeof body === 'object' && (ArrayBuffer.isView(body) || body instanceof ArrayBuffer);
}

/** A Content-Type of the multipart/ family (any case), the bodies multipart parsers read. */
function isMultipart(req) {
  var value = (req.headers || {})['content-type'];
  if (typeof value !== 'string') {
    return false;
  }
  var i = 0;
  while (i < value.length && value.charCodeAt(i) <= 0x20) {
    i++;
  }
  var prefix = 'multipart/';
  for (var k = 0; k < prefix.length; k++) {
    var c = value.charCodeAt(i + k);
    if ((c >= 0x41 && c <= 0x5A ? c + 0x20 : c) !== prefix.charCodeAt(k)) {
      return false;
    }
  }
  return true;
}

function hasOwn(object, key) {
  return Object.prototype.hasOwnProperty.call(object, key);
}

/**
 * The strings an application received under the empty parameter name in a parsed query or form object: [] when the
 * object has no such key, null when there is no parsed object to look at.
 */
function receivedEmptyName(container) {
  if (container === null || typeof container !== 'object' || isBinaryBody(container)) {
    return null;
  }
  if (!hasOwn(container, '')) {
    return [];
  }
  var out = [];
  var stack = [container['']];
  var seen = 0;
  while (stack.length > 0 && seen < 100000) {
    var value = stack.pop();
    seen++;
    if (typeof value === 'string') {
      out.push(value);
    } else if (value !== null && typeof value === 'object' && !isBinaryBody(value)) {
      var keys = Object.keys(value);
      for (var i = keys.length - 1; i >= 0; i--) {
        stack.push(value[keys[i]]);
        if (!Array.isArray(value)) {
          stack.push(keys[i]);
        }
      }
    }
  }
  return out;
}

/**
 * @param {object} deps
 *   cfg            internal configuration (resolve.js)
 *   store          StateStore
 *   detector       ContentDetector for the configured rules
 *   inspector      JsonDocumentInspector over that detector
 *   reporter       Reporter
 *   audit          AuditLogger
 *   respond        optional (req, res, { status, code, message }) => void
 */
function createAdapter(deps) {
  var cfg = deps.cfg;
  var store = deps.store;
  var detector = deps.detector;
  var inspector = deps.inspector;
  var reporter = deps.reporter;
  var audit = deps.audit;
  var respondHook = typeof deps.respond === 'function' ? deps.respond : null;
  var slowMillis = cfg.slowInspectionWarnMillis;
  var maxMillis = cfg.maxInspectionMillis;
  var hookMissingLogged = {};

  // -------------------------------------------------------------------------------------------------------------
  // Responses

  function render(req, res, err) {
    var status = err.status === 500 ? 500 : 400;
    var code = err.code;
    if (respondHook !== null) {
      try {
        respondHook(req, res, { status: status, code: code, message: errorResponse.messageFor(code) });
        return;
      } catch (e) {
        if (res.headersSent) {
          return;
        }
      }
    }
    errorResponse.write(res, status, code);
  }

  // Ends a middleware with the rejection; when the response already started, hands the error to Express.
  function finishWith(req, res, next, err) {
    if (res.headersSent) {
      next(err);
      return;
    }
    render(req, res, err);
  }

  // -------------------------------------------------------------------------------------------------------------
  // Phases: state, timing, internal errors

  function contextOf(req, state) {
    return { req: req, state: state, handler: state.handler, method: state.method, bytes: 0 };
  }

  function internalError(phase, req, state, e) {
    if (!state || !state.internalErrorLogged) {
      if (state) {
        state.internalErrorLogged = true;
      }
      var name = e !== null && typeof e === 'object' && e.constructor && typeof e.constructor.name === 'string'
        ? e.constructor.name : typeof e;
      audit.error('request_validation_internal_error phase=' + phase + ' error=' + safeText(name));
    }
    var mode = state && state.policy ? state.policy.mode : cfg.mode;
    return mode === 'ENFORCE' ? new RequestValidationError(errorResponse.INTERNAL_CODE) : null;
  }

  function slowCheck(phase, started, bytes, ctx) {
    if (slowMillis <= 0) {
      return;
    }
    var ms = nowMillis() - started;
    if (ms > slowMillis) {
      audit.warn('request_validation_slow_inspection phase=' + phase + ' ms=' + Math.floor(ms) + ' bytes=' + bytes
          + ' handler=' + safeText(ctx.handler));
    }
  }

  /**
   * Runs one inspection phase for an active request. `body(ctx)` adds the bytes it looks at to ctx.bytes (for the
   * slow-inspection log) and throws a RequestValidationError to reject. Returns the rejection or null.
   * `beforeActive(state)` runs first, also for inactive requests.
   */
  function runPhase(phase, req, body, beforeActive) {
    var state = null;
    var ctx = null;
    var started = 0;
    try {
      state = store.ensure(req);
      if (beforeActive) {
        beforeActive(state);
      }
      if (!state.active) {
        return null;
      }
      ctx = contextOf(req, state);
      started = nowMillis();
      body(ctx);
      slowCheck(phase, started, ctx.bytes, ctx);
      return null;
    } catch (e) {
      if (ctx !== null) {
        slowCheck(phase, started, ctx.bytes, ctx);
      }
      if (isRequestValidationError(e)) {
        return e;
      }
      return internalError(phase, req, state, e);
    }
  }

  // -------------------------------------------------------------------------------------------------------------
  // Scalars (ScalarParameterInterceptor)

  function newClock() {
    return maxMillis === null ? null : { deadline: nowMillis() + maxMillis, countdown: BUDGET_SCALARS };
  }

  // A function for the parsers' loops: stops the work once the time budget is spent.
  function tickOf(clock) {
    if (clock === null) {
      return null;
    }
    return function () {
      if (nowMillis() > clock.deadline) {
        throw new BudgetExpired(EMPTY_NAME_LOCATION);
      }
    };
  }

  /** One scalar: budget, then detection. Returns false when REPORT stops scalar inspection at a limit. */
  function inspectScalar(ctx, value, location, kind, clock) {
    var state = ctx.state;
    var policy = state.policy;
    var limits = policy.limits;
    state.budget[0] += 1;
    state.budget[1] += value.length;
    if (value.length > limits.maxScalarLength || state.budget[0] > limits.maxTokens
        || state.budget[1] > limits.maxBodyBytes) {
      var rejection = reporter.structural(ctx, policy.mode, new Violation(LIMIT, 'scalar-limit', location, value.length),
          kind);
      if (rejection) {
        throw rejection;
      }
      state.scalarStopped = true;
      return false;
    }
    if (clock !== null && (--clock.countdown <= 0 || value.length > BUDGET_LONG_TEXT)) {
      clock.countdown = BUDGET_SCALARS;
      if (nowMillis() > clock.deadline) {
        throw new BudgetExpired(location);
      }
    }
    var rule = detector.detect(value);
    if (rule !== null) {
      var found = reporter.content(ctx, policy.mode, new Violation(CONTENT, rule, location, value.length), kind);
      if (found) {
        throw found;
      }
    }
    return true;
  }

  /**
   * Runs `work(clock)` for scalars of `kind`; when the time budget runs out, records `inspection-budget` (REPORT stops
   * scalar inspection, ENFORCE rejects).
   */
  function withScalarClock(ctx, kind, work) {
    var state = ctx.state;
    if (state.scalarStopped) {
      return;
    }
    var clock = newClock();
    try {
      work(clock);
    } catch (e) {
      if (!(e instanceof BudgetExpired)) {
        throw e;
      }
      var rejection = reporter.structural(ctx, state.policy.mode,
          new Violation(LIMIT, 'inspection-budget', e.location, 0), kind);
      if (rejection) {
        throw rejection;
      }
      state.scalarStopped = true;
    }
  }

  // Inspects strings received under the empty parameter name; false when a REPORT limit stopped scalars.
  function inspectEmptyNameValues(ctx, values, clock) {
    for (var i = 0; i < values.length; i++) {
      if (!inspectScalar(ctx, values[i], EMPTY_NAME_LOCATION, 'query/form', clock)) {
        return false;
      }
    }
    return true;
  }

  // The query object the application's parser produced, or null (no Express, or its parser failed).
  function receivedQuery(req) {
    try {
      var query = req.query;
      return query !== null && typeof query === 'object' ? query : null;
    } catch (e) {
      return null;
    }
  }

  /**
   * The Content-Type header (unless `formOnly`), then the parameter map of the query (unless `formOnly`) and the
   * form body, then the extras (the strings the application's parsers produce that the map does not hold). Body
   * pairs Tomcat does not keep, and empty-name form pairs, are left to `pendingFormWork`. Stops at the first REPORT
   * limit; throws the ENFORCE rejection.
   */
  function runScalars(ctx, form, formEncoding, formOnly) {
    var state = ctx.state;
    withScalarClock(ctx, 'query/form', function (clock) {
      var headers = ctx.req.headers || {};
      if (!formOnly && cfg.inspectContentTypeHeader && headers['content-type'] !== undefined) {
        if (!inspectScalar(ctx, String(headers['content-type']), '/Content-Type', 'header', clock)) {
          return;
        }
      }
      var query = formOnly ? null : parameters.rawQuery(state.target);
      ctx.bytes += (query === null ? 0 : query.length) + (form ? form.length : 0);
      var map = parameters.parameterMap(query, form, formEncoding,
          { appQuery: formOnly ? null : state.appQuery, tick: tickOf(clock) });
      if (map.deferred !== null) {
        state.pendingForm = map.deferred;
      }
      if (map.formEmptyNames.length > 0) {
        state.formEmptyNames = map.formEmptyNames;
      }
      if (state.scalarNames === null) {
        state.scalarNames = new Set();
      }
      for (var i = 0; i < map.entries.length; i++) {
        var name = map.entries[i][0];
        var values = map.entries[i][1];
        var location = SafeLocationFormatter.scalar(name);
        state.scalarNames.add(name);
        if (!inspectScalar(ctx, name, location, 'query/form', clock)) {
          return;
        }
        for (var v = 0; v < values.length; v++) {
          if (!inspectScalar(ctx, values[v], location, 'query/form', clock)) {
            return;
          }
        }
      }
      for (var x = 0; x < map.extras.length; x++) {
        var extra = map.extras[x];
        if (!inspectScalar(ctx, extra.value, SafeLocationFormatter.scalar(extra.name), 'query/form', clock)) {
          return;
        }
      }
      if (map.queryEmptyNames.length > 0) {
        // Only Node's querystring keeps a pair with an empty name: inspect what the application's query parser
        // produced under that name (all modelled values when there is no parsed query to look at).
        var query2 = receivedQuery(ctx.req);
        var received = query2 === null ? null : receivedEmptyName(query2);
        inspectEmptyNameValues(ctx, received === null ? map.queryEmptyNames : received, clock);
      }
    });
  }

  /**
   * After the body parser accepted a urlencoded body: the extras of the body pairs Tomcat does not keep, then the
   * strings the parsed body holds under the empty name.
   */
  function pendingFormWork(ctx) {
    var state = ctx.state;
    var deferred = state.pendingForm;
    var emptyNames = state.formEmptyNames;
    if (deferred === null && emptyNames === null) {
      return;
    }
    state.pendingForm = null;
    state.formEmptyNames = null;
    withScalarClock(ctx, 'query/form', function (clock) {
      var modelled = emptyNames === null ? [] : emptyNames.slice();
      var stopped = false;
      if (deferred !== null) {
        ctx.bytes += deferred.form.length - deferred.start;
        parameters.deferredExtras(deferred, tickOf(clock), function (name, value) {
          stopped = !inspectScalar(ctx, value, SafeLocationFormatter.scalar(name), 'query/form', clock);
          return !stopped;
        }, function (value) {
          modelled.push(value);
        });
      }
      if (stopped || modelled.length === 0) {
        return;
      }
      var received = receivedEmptyName(ctx.req.body);
      inspectEmptyNameValues(ctx, received === null ? modelled : received, clock);
    });
  }

  /**
   * The form text of a urlencoded body that a parser without the hook turned into a string (a text parser), as
   * bytes, or null.
   */
  function hooklessFormBytes(ctx) {
    var req = ctx.req;
    var state = ctx.state;
    if (state.formHookSeen || typeof req.body !== 'string' || !bodyParsed(req)) {
      return null;
    }
    var type = mediaType.parse((req.headers || {})['content-type']);
    if (!mediaType.isForm(type)) {
      return null;
    }
    logHookMissing('urlencoded');
    var latin1 = type.charset !== undefined && mediaType.unquote(type.charset).toLowerCase() === 'iso-8859-1';
    return { bytes: Buffer.from(req.body, latin1 ? 'latin1' : 'utf8'), encoding: latin1 ? 'iso-8859-1' : 'utf-8' };
  }

  /** Work every later touch point does first: the scalars when no earlier one did, then the pending form work. */
  function catchUp(ctx) {
    var state = ctx.state;
    if (!state.scalarsDone) {
      state.scalarsDone = true;
      var hookless = hooklessFormBytes(ctx);
      if (hookless !== null) {
        state.formDone = true;
      }
      runScalars(ctx, hookless === null ? null : hookless.bytes, hookless === null ? null : hookless.encoding, false);
    } else if (!state.formDone) {
      var late = hooklessFormBytes(ctx);
      if (late !== null) {
        state.formDone = true;
        runScalars(ctx, late.bytes, late.encoding, true);
      }
    }
    pendingFormWork(ctx);
  }

  // -------------------------------------------------------------------------------------------------------------
  // Parsed fields: multipart text fields, and the safety net for a parser without its hook

  /**
   * Each top-level field of a parsed object, as the parameter map would hold it: the name, then every string under
   * it (values, and the keys of nested objects), with the field name as the location. Names in `skipNames` were
   * already inspected as parameter names. Returns false when a REPORT limit stopped scalars.
   */
  function walkFields(ctx, root, clock, skipNames) {
    var limits = ctx.state.policy.limits;
    var keys = Object.keys(root);
    for (var i = 0; i < keys.length; i++) {
      var location = SafeLocationFormatter.scalar(keys[i]);
      var named = skipNames !== null && skipNames.has(keys[i]);
      if (!named && !inspectScalar(ctx, keys[i], location, 'query/form', clock)) {
        return false;
      }
      var stack = [{ value: root[keys[i]], depth: 0 }];
      var seen = new Set();
      while (stack.length > 0) {
        var item = stack.pop();
        if (typeof item.value === 'string') {
          if (!inspectScalar(ctx, item.value, location, 'query/form', clock)) {
            return false;
          }
        } else if (item.value !== null && typeof item.value === 'object' && !isBinaryBody(item.value)
            && !seen.has(item.value) && item.depth < limits.maxDepth) {
          seen.add(item.value);
          var childKeys = Object.keys(item.value);
          for (var c = childKeys.length - 1; c >= 0; c--) {
            stack.push({ value: item.value[childKeys[c]], depth: item.depth + 1 });
            if (!Array.isArray(item.value)) {
              stack.push({ value: childKeys[c], depth: item.depth + 1 });
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * The text fields a multipart parser put on req.body (Java inspects them through the parameter map): once per
   * request, as soon as the parsed body holds fields.
   */
  function inspectMultipart(ctx) {
    var state = ctx.state;
    var body = ctx.req.body;
    if (state.multipartDone || state.scalarStopped || !isMultipart(ctx.req)) {
      return;
    }
    if (body === null || typeof body !== 'object' || isBinaryBody(body) || Object.keys(body).length === 0) {
      return;
    }
    state.multipartDone = true;
    withScalarClock(ctx, 'query/form', function (clock) {
      walkFields(ctx, body, clock, state.scalarNames);
    });
  }

  function logHookMissing(parser) {
    if (!hookMissingLogged[parser]) {
      hookMissingLogged[parser] = true;
      audit.warn('request_validation_hook_missing parser=' + parser);
    }
  }

  function walkJson(ctx, root) {
    var state = ctx.state;
    var policy = state.policy;
    var limits = policy.limits;
    // With an exemption, findings wait until the walk is complete, as in JsonDocumentInspector.
    var deferred = policy.exemption ? { strings: new Map(), findings: [] } : null;
    // Depth-first in document order: each property name, then its value.
    var stack = [{ value: root, path: [], name: false }];
    var seen = new Set();
    var nodes = 0;
    while (stack.length > 0) {
      var item = stack.pop();
      if (++nodes > limits.maxTokens) {
        reportDeferred(ctx, deferred, null);
        return;
      }
      var value = item.value;
      if (item.name || typeof value === 'string') {
        if (deferred !== null && !item.name) {
          deferred.strings.set(FlaggedValue.format(item.path), value);
        }
        if (!policy.matcher.matches(item.path)) {
          if (deferred === null) {
            reportWalk(ctx, value, SafeLocationFormatter.format(item.path), 'body');
          } else {
            var rule = detector.detect(value);
            if (rule !== null) {
              deferred.findings.push({ violation: new Violation(CONTENT, rule, SafeLocationFormatter.format(item.path),
                  value.length), path: item.name ? null : item.path, value: value });
            }
          }
        }
        continue;
      }
      if (value === null || typeof value !== 'object' || seen.has(value) || item.path.length >= limits.maxDepth) {
        continue;
      }
      seen.add(value);
      var isArray = Array.isArray(value);
      var keys = Object.keys(value);
      for (var k = keys.length - 1; k >= 0; k--) {
        var childPath = item.path.concat(isArray ? [String(k)] : [keys[k]]);
        stack.push({ value: value[keys[k]], path: childPath, name: false });
        if (!isArray) {
          stack.push({ value: keys[k], path: childPath, name: true });
        }
      }
    }
    reportDeferred(ctx, deferred, policy.exemption);
  }

  // Reports held-back walk findings in order; exemption null (an incomplete walk) consults nothing.
  function reportDeferred(ctx, deferred, exemption) {
    if (deferred === null) {
      return;
    }
    deferred.findings.forEach(function (finding) {
      if (exemption !== null && finding.path !== null) {
        var accepted = false;
        try {
          accepted = exemption(new FlaggedValue(finding.path, finding.value, finding.violation.ruleId,
              deferred.strings)) === true;
        } catch (e) {
          accepted = false; // fails closed, as in JsonDocumentInspector
        }
        if (accepted) {
          return;
        }
      }
      var rejection = reporter.content(ctx, ctx.state.policy.mode, finding.violation, 'body');
      if (rejection) {
        throw rejection;
      }
    });
  }

  function reportWalk(ctx, text, location, kind) {
    var rule = detector.detect(text);
    if (rule !== null) {
      var rejection = reporter.content(ctx, ctx.state.policy.mode, new Violation(CONTENT, rule, location, text.length), kind);
      if (rejection) {
        throw rejection;
      }
    }
  }

  // A body parser parsed the body but the matching verify hook never ran (reduced parity).
  function safetyNet(ctx) {
    var req = ctx.req;
    var state = ctx.state;
    if (!bodyParsed(req)) {
      return;
    }
    var type = mediaType.parse((req.headers || {})['content-type']);
    var parser = null;
    if (mediaType.isJson(type) && !state.jsonHookSeen) {
      parser = 'json';
    } else if (mediaType.isForm(type) && !state.formHookSeen) {
      parser = 'urlencoded';
    }
    if (parser === null) {
      return;
    }
    logHookMissing(parser);
    // Bytes left by a raw parser are not a parsed document: there is nothing to walk (walking a Buffer's indexes
    // could never find anything and would cost time and memory in proportion to the body).
    if (isBinaryBody(req.body)) {
      return;
    }
    if (parser === 'json') {
      if (state.policy.structured && !state.bodyDone) {
        state.bodyDone = true;
        walkJson(ctx, req.body);
      }
    } else if (!state.formDone && !state.scalarStopped && req.body !== null && typeof req.body === 'object') {
      state.formDone = true;
      withScalarClock(ctx, 'query/form', function (clock) {
        walkFields(ctx, req.body, clock, null);
      });
    }
  }

  // -------------------------------------------------------------------------------------------------------------
  // JSON body (StructuredBodyAdvice)

  function inspectJsonBody(ctx, buf) {
    var policy = ctx.state.policy;
    var mode = policy.mode;
    var options;
    if (maxMillis !== null) {
      options = { deadline: nowMillis() + maxMillis, now: nowMillis };
    }
    if (policy.exemption) {
      options = options || {};
      options.exemption = policy.exemption;
    }
    try {
      inspector.inspect(buf, policy.limits, policy.matcher, cfg.rejectDuplicateKeys, cfg.rejectDualRequestInfo,
          function (violation) {
            var rejection = reporter.content(ctx, mode, violation, 'body');
            if (rejection) {
              throw rejection;
            }
          }, options);
    } catch (e) {
      if (!(e instanceof InspectionError)) {
        throw e;
      }
      var structural = reporter.structural(ctx, mode, e.violation, 'body');
      if (structural) {
        throw structural;
      }
    }
  }

  // The bounded body checks every JSON entry point shares: charset, size, then the document.
  function jsonBody(ctx, buf, encoding) {
    var state = ctx.state;
    var policy = state.policy;
    if (!policy.structured || state.bodyDone || buf.length === 0) {
      return;
    }
    state.bodyDone = true;
    ctx.bytes += buf.length;
    var finding = null;
    if (typeof encoding === 'string' && !mediaType.isUtf8Encoding(encoding)) {
      finding = new Violation(MALFORMED, 'charset', '/', 0);
    } else if (buf.length > policy.limits.maxBodyBytes) {
      finding = new Violation(LIMIT, 'max-body-bytes', '/', 0);
    }
    if (finding !== null) {
      var rejection = reporter.structural(ctx, policy.mode, finding, 'body');
      if (rejection) {
        throw rejection;
      }
      return;
    }
    inspectJsonBody(ctx, buf);
  }

  // -------------------------------------------------------------------------------------------------------------
  // The touch points

  function beforeParsers(req, res, next) {
    var rejection = runPhase('before', req, function (ctx) {
      var state = ctx.state;
      var headers = req.headers || {};
      var type = mediaType.parse(headers['content-type']);
      // A form body waits for formVerify (or afterParsers), so query and form parameters are inspected grouped,
      // in the servlet parameter map order.
      if (!(mediaType.isForm(type) && mediaType.hasBody(req)) && !state.scalarsDone) {
        state.scalarsDone = true;
        runScalars(ctx, null, null, false);
      }
      var policy = state.policy;
      if (policy.structured && !state.bodyDone && mediaType.isJson(type) && jsonBodyPresent(req)) {
        var finding = null;
        if (type.charset !== undefined && !mediaType.isUtf8Charset(type.charset)) {
          finding = new Violation(MALFORMED, 'charset', '/', 0);
        } else if (declaredLength(req) > policy.limits.maxBodyBytes) {
          finding = new Violation(LIMIT, 'max-body-bytes', '/', 0);
        }
        if (finding !== null) {
          state.bodyDone = true;
          var found = reporter.structural(ctx, policy.mode, finding, 'body');
          if (found) {
            throw found;
          }
        }
      }
    });
    if (rejection) {
      finishWith(req, res, next, rejection);
      return;
    }
    next();
  }

  function jsonVerify(req, res, buf, encoding) {
    var rejection = runPhase('json', req, function (ctx) {
      jsonBody(ctx, buf, encoding);
    }, function (state) {
      state.jsonHookSeen = true;
    });
    if (rejection) {
      throw rejection;
    }
  }

  function formVerify(req, res, buf, encoding) {
    var rejection = runPhase('form', req, function (ctx) {
      var state = ctx.state;
      if (!state.scalarsDone) {
        state.scalarsDone = true;
        state.formDone = true;
        runScalars(ctx, buf, encoding, false);
      } else if (!state.formDone) {
        // The parser ran for a body that beforeParsers did not take for a form: inspect it now, after the query.
        state.formDone = true;
        runScalars(ctx, buf, encoding, true);
      }
    }, function (state) {
      state.formHookSeen = true;
    });
    if (rejection) {
      throw rejection;
    }
  }

  function finisher(req, res, next) {
    var rejection = runPhase('after', req, function (ctx) {
      catchUp(ctx);
      inspectMultipart(ctx);
      safetyNet(ctx);
    });
    if (rejection) {
      finishWith(req, res, next, rejection);
      return;
    }
    next();
  }

  function errorArm(err, req, res, next) {
    if (isRequestValidationError(err)) {
      if (res.headersSent) {
        next(err);
        return;
      }
      render(req, res, err);
      return;
    }
    var rejection = runPhase('after', req, function (ctx) {
      var state = ctx.state;
      if (!state.scalarsDone) {
        // e.g. a form body that body-parser rejected (413, 415, aborted) before formVerify ran
        state.scalarsDone = true;
        runScalars(ctx, null, null, false);
      }
      // body pairs left for after the parser: the parser rejected the body, so the application never gets them
      state.pendingForm = null;
      state.formEmptyNames = null;
      var type = err !== null && typeof err === 'object' ? err.type : undefined;
      if ((type === 'request.aborted' || type === 'request.size.invalid') && state.policy.structured && !state.bodyDone
          && mediaType.isJson(mediaType.parse((req.headers || {})['content-type']))) {
        state.bodyDone = true;
        if (state.policy.mode === 'ENFORCE') {
          throw reporter.rejected(ctx, new Violation(MALFORMED, 'body-read', '/', 0), 'body');
        }
        reporter.incomplete(ctx, MALFORMED, 'transport');
      }
    });
    if (rejection) {
      finishWith(req, res, next, rejection);
      return;
    }
    next(err);
  }

  function pathParams(req, res, next) {
    var rejection = runPhase('path', req, function (ctx) {
      var state = ctx.state;
      catchUp(ctx);
      var params = req.params;
      if (state.scalarStopped || params === null || typeof params !== 'object') {
        return;
      }
      withScalarClock(ctx, 'path', function (clock) {
        var names = Object.keys(params);
        if (state.pathSeen === null) {
          state.pathSeen = new Set();
        }
        for (var i = 0; i < names.length; i++) {
          var name = names[i];
          var value = params[name];
          var values = typeof value === 'string' ? [value] : Array.isArray(value) ? value.filter(function (v) {
            return typeof v === 'string';
          }) : [];
          if (values.length === 0) {
            continue;
          }
          var key = name + '\u0000' + values.join('\u0000');
          if (state.pathSeen.has(key)) {
            continue;
          }
          state.pathSeen.add(key);
          var location = SafeLocationFormatter.scalar(name);
          if (!inspectScalar(ctx, name, location, 'path', clock)) {
            return;
          }
          for (var v = 0; v < values.length; v++) {
            if (!inspectScalar(ctx, values[v], location, 'path', clock)) {
              return;
            }
          }
        }
      });
    });
    if (rejection) {
      finishWith(req, res, next, rejection);
      return;
    }
    next();
  }

  function multipartFields(req, res, next) {
    var rejection = runPhase('multipart', req, function (ctx) {
      catchUp(ctx);
      inspectMultipart(ctx);
    });
    if (rejection) {
      finishWith(req, res, next, rejection);
      return;
    }
    next();
  }

  function inspectJsonBuffer(req, body) {
    if (!Buffer.isBuffer(body) && !(body instanceof Uint8Array)) {
      throw new TypeError('body must be a Buffer');
    }
    var buf = Buffer.isBuffer(body) ? body : Buffer.from(body.buffer, body.byteOffset, body.byteLength);
    var rejection = runPhase('buffer', req, function (ctx) {
      jsonBody(ctx, buf, 'utf-8');
    }, function (state) {
      state.jsonHookSeen = true;
    });
    if (rejection) {
      throw rejection;
    }
  }

  return {
    beforeParsers: beforeParsers,
    jsonVerify: jsonVerify,
    formVerify: formVerify,
    afterParsers: [finisher, errorArm],
    pathParams: pathParams,
    multipartFields: multipartFields,
    inspectJsonBuffer: inspectJsonBuffer
  };
}

/** No-op touch points of the same arity, for a disabled instance. */
function createDisabledAdapter() {
  return {
    beforeParsers: function (req, res, next) {
      next();
    },
    jsonVerify: function (req, res, buf, encoding) { // eslint-disable-line no-unused-vars
    },
    formVerify: function (req, res, buf, encoding) { // eslint-disable-line no-unused-vars
    },
    afterParsers: [function (req, res, next) {
      next();
    }, function (err, req, res, next) {
      next(err);
    }],
    pathParams: function (req, res, next) {
      next();
    },
    multipartFields: function (req, res, next) {
      next();
    },
    inspectJsonBuffer: function (req, body) { // eslint-disable-line no-unused-vars
    }
  };
}

module.exports = {
  createAdapter: createAdapter,
  createDisabledAdapter: createDisabledAdapter
};
