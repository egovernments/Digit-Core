'use strict';

// Public API (index.js / index.d.ts names), middleware arities, the disabled instance, detect/inspectJson, the
// RequestValidationError contract and the exact ENFORCE response bytes.

var assert = require('assert');
var fs = require('fs');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var errorResponse = require(path.join(h.ROOT, 'lib', 'web', 'errorResponse'));
var mini = require('./support/miniapp');

function quiet() {
  return { warn: function () {}, info: function () {} };
}

function enabled(extra) {
  return rv.createRequestValidation(Object.assign({ env: false, enabled: true, structuredDefault: true, activation: 'ALL',
    mode: 'ENFORCE', logger: quiet() }, extra || {}));
}

test('exports: the index.d.ts names plus the core classes; no default export', function () {
  ['createRequestValidation', 'RequestValidationError', 'isRequestValidationError', 'VIOLATION_MESSAGES', 'CONFORMS_TO']
      .forEach(function (name) {
        assert.ok(rv[name] !== undefined, name);
      });
  ['ViolationCode', 'Violation', 'InspectionError', 'InspectionLimits', 'ContentPolicy', 'ContentDetector', 'SkipPathMatcher',
    'SafeLocationFormatter', 'JsonDocumentInspector'].forEach(function (name) {
    assert.ok(rv[name] !== undefined, name);
  });
  assert.strictEqual(rv.default, undefined);
  assert.deepStrictEqual(Object.keys(rv.VIOLATION_MESSAGES), ['REQUEST_CONTENT_NOT_ALLOWED', 'REQUEST_JSON_MALFORMED',
    'REQUEST_LIMIT_EXCEEDED', 'REQUEST_JSON_DUPLICATE_KEY']);
  var pkg = JSON.parse(fs.readFileSync(path.join(h.ROOT, 'package.json'), 'utf8'));
  assert.deepStrictEqual(JSON.parse(JSON.stringify(rv.CONFORMS_TO)), pkg.egovRequestValidation.conformsTo);
  assert.ok(Object.isFrozen(rv.CONFORMS_TO) && Object.isFrozen(rv.VIOLATION_MESSAGES));
  var mjs = fs.readFileSync(path.join(h.ROOT, 'index.mjs'), 'utf8');
  Object.keys(rv).forEach(function (name) {
    assert.ok(mjs.indexOf('export const ' + name + ' = cjs.' + name + ';') !== -1, 'index.mjs exports ' + name);
  });
  assert.strictEqual(mjs.indexOf('export default'), -1);
  var dts = fs.readFileSync(path.join(h.ROOT, 'index.d.ts'), 'utf8');
  ['createRequestValidation', 'RequestValidationError', 'isRequestValidationError', 'VIOLATION_MESSAGES', 'CONFORMS_TO']
      .forEach(function (name) {
        assert.ok(dts.indexOf(name) !== -1, 'index.d.ts declares ' + name);
      });
});

test('arities: beforeParsers, pathParams and afterParsers[0] take 3; afterParsers[1] and the verify hooks take 4', function () {
  [enabled(), rv.createRequestValidation({ env: false })].forEach(function (instance) {
    assert.strictEqual(instance.beforeParsers.length, 3);
    assert.strictEqual(instance.pathParams.length, 3);
    assert.strictEqual(instance.afterParsers.length, 2);
    assert.strictEqual(instance.afterParsers[0].length, 3);
    assert.strictEqual(instance.afterParsers[1].length, 4);
    assert.strictEqual(instance.jsonVerify.length, 4);
    assert.strictEqual(instance.formVerify.length, 4);
    assert.strictEqual(instance.inspectJsonBuffer.length, 2);
    assert.ok(Object.isFrozen(instance.afterParsers));
    assert.throws(function () {
      'use strict';
      instance.beforeParsers = null;
    }, TypeError);
  });
});

test('disabled instance: no-ops of the same arity; detect and inspectJson use the Java defaults', function () {
  var off = rv.createRequestValidation({ env: false, mode: 'ENFORCE', structuredDefault: true });
  assert.strictEqual(off.enabled, false);
  assert.strictEqual(off.config.enabled, false);
  assert.strictEqual(off.config.mode, 'REPORT');
  var app = mini.serviceApp(off);
  return mini.request(app, { method: 'POST', url: '/json?q=%3Cb%3E', headers: { 'content-type': 'application/json' },
    body: '{"a":"<script>"}' }).then(function (r) {
    assert.strictEqual(r.status, 200);
    assert.ok(r.handled);
    assert.strictEqual(off.detect('javascript:alert(1)'), 'R2');
    assert.strictEqual(off.detect('plain'), null);
    var res = off.inspectJson(Buffer.from('{"a":"<b>","a":1}'));
    assert.deepStrictEqual(res.findings.map(String), ['REQUEST_CONTENT_NOT_ALLOWED|R1|/a|3']);
    assert.strictEqual(String(res.failure), 'REQUEST_JSON_DUPLICATE_KEY|duplicate-key|/a|1');
    var err = new Error('x');
    var passed = null;
    off.afterParsers[1](err, {}, {}, function (e) {
      passed = e;
    });
    assert.strictEqual(passed, err);
    off.jsonVerify({}, {}, Buffer.from('<'), 'utf-8');
    off.inspectJsonBuffer({}, Buffer.from('<'));
  });
});

test('detect and inspectJson: effective rules and limits, per-call overrides, never log or throw for findings', function () {
  var logger = mini.recordingLogger();
  var on = enabled({ logger: logger, rules: { deniedSchemes: ['livescript'] }, limits: { maxDepth: 3 } });
  logger.clear();
  assert.strictEqual(on.detect('javascript:x'), null);
  assert.strictEqual(on.detect('livescript:x'), 'R2');
  var deep = on.inspectJson(Buffer.from('{"a":{"b":{"c":{}}}}'));
  assert.strictEqual(deep.failure.ruleId, 'parser-limit');
  assert.strictEqual(on.inspectJson(Buffer.from('{"a":{"b":{"c":{}}}}'), { limits: { maxDepth: 4 } }).failure, null);
  var skipped = on.inspectJson(Buffer.from('{"note":"<b>","x":"<b>"}'), { skipPaths: ['/note'] });
  assert.deepStrictEqual(skipped.findings.map(String), ['REQUEST_CONTENT_NOT_ALLOWED|R1|/x|3']);
  assert.deepStrictEqual(on.inspectJson(Buffer.from('{"u":"javascript:1"}'), { rules: { deniedSchemes: ['javascript'] } })
      .findings.map(String), ['REQUEST_CONTENT_NOT_ALLOWED|R2|/u|12']);
  assert.strictEqual(on.inspectJson(Buffer.from('{"a":1,"a":2}'), { rejectDuplicateKeys: false }).failure, null);
  assert.strictEqual(on.inspectJson(Buffer.from('')).failure.ruleId, 'json');
  assert.strictEqual(logger.lines.length, 0);
  assert.throws(function () {
    on.detect(42);
  }, TypeError);
});

test('RequestValidationError: fixed code and message, http-errors fields, brand; survives body-parser wrapping', function () {
  var e = new rv.RequestValidationError('REQUEST_JSON_MALFORMED');
  assert.ok(e instanceof Error);
  assert.strictEqual(e.name, 'RequestValidationError');
  assert.strictEqual(e.message, 'Request body is not valid JSON');
  assert.strictEqual(e.status, 400);
  assert.strictEqual(e.statusCode, 400);
  assert.strictEqual(e.expose, true);
  assert.strictEqual(e.code, 'REQUEST_JSON_MALFORMED');
  assert.ok(rv.isRequestValidationError(e));
  var internal = new rv.RequestValidationError('INTERNAL_SERVER_ERROR');
  assert.strictEqual(internal.status, 500);
  assert.strictEqual(internal.expose, false);
  assert.strictEqual(internal.message, 'Internal server error');
  assert.throws(function () {
    return new rv.RequestValidationError('SOMETHING');
  }, TypeError);
  [null, undefined, {}, new Error('x'), { code: 'REQUEST_JSON_MALFORMED', status: 400 }, 'REQUEST_JSON_MALFORMED'].forEach(function (x) {
    assert.strictEqual(rv.isRequestValidationError(x), false);
  });
  // brand is shared with other copies of the package (Symbol.for), not tied to this class
  var foreign = {};
  foreign[Symbol.for('@egovernments/request-validation.error')] = true;
  assert.ok(rv.isRequestValidationError(foreign));
  // http-errors createError(403, err, { body, type }) assigns these properties on the thrown error
  e.expose = e.status < 500;
  e.status = e.statusCode = 400;
  e.body = Buffer.from('raw');
  e.type = 'entity.verify.failed';
  assert.ok(rv.isRequestValidationError(e));
  assert.strictEqual(JSON.stringify(Object.keys(new rv.RequestValidationError('REQUEST_LIMIT_EXCEEDED')).sort()),
      JSON.stringify(['code', 'expose', 'status', 'statusCode']));
});

test('ENFORCE response: exact bytes per code, status 400, Content-Type application/json, Connection: close', function () {
  var expected = {
    REQUEST_CONTENT_NOT_ALLOWED: 'Request contains content that is not allowed',
    REQUEST_JSON_MALFORMED: 'Request body is not valid JSON',
    REQUEST_LIMIT_EXCEEDED: 'Request exceeds an allowed size limit',
    REQUEST_JSON_DUPLICATE_KEY: 'Request body contains a duplicate property'
  };
  Object.keys(expected).forEach(function (code) {
    var res = mini.createResponse(function () {});
    assert.ok(errorResponse.write(res, 400, code));
    assert.strictEqual(res.statusCode, 400);
    assert.deepStrictEqual(res.headers, { 'content-type': 'application/json', connection: 'close' });
    assert.strictEqual(res.body.toString('utf8'), '{"ResponseInfo":null,"Errors":[{"code":"' + code + '","message":"'
        + expected[code] + '","description":null,"params":null}]}');
  });
  var r500 = mini.createResponse(function () {});
  errorResponse.write(r500, 500, 'INTERNAL_SERVER_ERROR');
  assert.strictEqual(r500.statusCode, 500);
  assert.strictEqual(r500.body.toString(), '{"ResponseInfo":null,"Errors":[{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error","description":null,"params":null}]}');
  var sent = mini.createResponse(function () {});
  sent.headersSent = true;
  assert.strictEqual(errorResponse.write(sent, 400, 'REQUEST_JSON_MALFORMED'), false);
  assert.strictEqual(Buffer.byteLength(errorResponse.body('REQUEST_CONTENT_NOT_ALLOWED')), 161);
});

test('respond hook: replaces the default renderer; a throwing hook falls back to the default bytes', function () {
  var seen = [];
  var custom = enabled({ respond: function (req, res, error) {
    seen.push(error);
    res.statusCode = error.status;
    res.end('custom ' + error.code);
  } });
  var broken = enabled({ respond: function () {
    throw new Error('hook failed');
  } });
  return mini.request(mini.serviceApp(custom), { url: '/q?x=%3Cb%3E' }).then(function (r) {
    assert.strictEqual(r.status, 400);
    assert.strictEqual(r.body, 'custom REQUEST_CONTENT_NOT_ALLOWED');
    assert.deepStrictEqual(seen, [{ status: 400, code: 'REQUEST_CONTENT_NOT_ALLOWED', message: 'Request contains content that is not allowed' }]);
    return mini.request(mini.serviceApp(broken), { url: '/q?x=%3Cb%3E' });
  }).then(function (r) {
    assert.strictEqual(r.status, 400);
    assert.strictEqual(r.headers.connection, 'close');
    assert.ok(r.body.indexOf('"code":"REQUEST_CONTENT_NOT_ALLOWED"') !== -1);
  });
});
