'use strict';

// Configuration: precedence (environment > options > Java defaults), the enabled switch, validation when enabled,
// route matching and policy merge, startup coverage lines, and the environment results Java
// produces for the same variables (fixture from Spring Boot 2.2.13 + the jar's EnvironmentVariableFallback).

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var resolve = require(path.join(h.ROOT, 'lib', 'config', 'resolve'));
var defaults = require(path.join(h.ROOT, 'lib', 'config', 'defaults'));
var mini = require('./support/miniapp');

var BASE = { EGOV_REQUEST_VALIDATION_ENABLED: 'true', EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT: 'true' };

function env(extra) {
  return Object.assign({}, BASE, extra || {});
}

function quiet() {
  return { warn: function () {}, info: function () {} };
}

function create(options) {
  return rv.createRequestValidation(Object.assign({ logger: quiet() }, options));
}

test('the 23 Java settings keep their names, environment variables and defaults; 3 more are Node-only', function () {
  var java = defaults.SETTINGS.filter(function (s) {
    return !s.nodeOnly;
  });
  assert.strictEqual(java.length, 23);
  var reference = h.readJsonFixture('java-reference.json');
  assert.deepStrictEqual(java.map(function (s) {
    return s.env;
  }), ['EGOV_REQUEST_VALIDATION_ENABLED', 'EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT', 'EGOV_REQUEST_VALIDATION_ACTIVATION',
    'EGOV_REQUEST_VALIDATION_MODE', 'EGOV_REQUEST_VALIDATION_INSPECT_CONTENT_TYPE_HEADER',
    'EGOV_REQUEST_VALIDATION_REJECT_DUPLICATE_KEYS', 'EGOV_REQUEST_VALIDATION_REJECT_DUAL_REQUEST_INFO',
    'EGOV_REQUEST_VALIDATION_LOG_REPORT_SAMPLE_RATE', 'EGOV_REQUEST_VALIDATION_LIMITS_MAX_BODY_BYTES',
    'EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH', 'EGOV_REQUEST_VALIDATION_LIMITS_MAX_STRING_LENGTH',
    'EGOV_REQUEST_VALIDATION_LIMITS_MAX_NAME_LENGTH', 'EGOV_REQUEST_VALIDATION_LIMITS_MAX_TOKENS',
    'EGOV_REQUEST_VALIDATION_LIMITS_MAX_NUMBER_LENGTH', 'EGOV_REQUEST_VALIDATION_LIMITS_MAX_SCALAR_LENGTH',
    'EGOV_REQUEST_VALIDATION_RULES_MARKUP_START', 'EGOV_REQUEST_VALIDATION_RULES_URL_SCHEME',
    'EGOV_REQUEST_VALIDATION_RULES_EVENT_HANDLER', 'EGOV_REQUEST_VALIDATION_RULES_DISALLOWED_CONTROLS',
    'EGOV_REQUEST_VALIDATION_RULES_DENIED_SCHEMES', 'EGOV_REQUEST_VALIDATION_RULES_DENIED_DATA_MEDIA_TYPES',
    'EGOV_REQUEST_VALIDATION_RULES_DECODE_ROUNDS', 'EGOV_REQUEST_VALIDATION_RULES_NORMALIZE_NFKC']);
  assert.deepStrictEqual(defaults.SETTINGS.filter(function (s) {
    return s.nodeOnly;
  }).map(function (s) {
    return s.env;
  }), ['EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS', 'EGOV_REQUEST_VALIDATION_NODE_MAX_INSPECTION_MILLIS',
    'EGOV_REQUEST_VALIDATION_NODE_SLOW_INSPECTION_WARN_MILLIS']);
  var c = create({ env: env() }).config;
  assert.deepStrictEqual(c.limits, reference.inspectionLimits.defaults);
  assert.deepStrictEqual(c.rules.deniedSchemes, ['javascript', 'vbscript']);
  assert.deepStrictEqual(c.rules.deniedDataMediaTypes, ['text/html', 'application/xhtml+xml', 'image/svg+xml']);
  assert.deepStrictEqual(c.rules.disallowedControls, [0]);
  assert.strictEqual(c.rules.decodeRounds, 2);
  assert.strictEqual(c.activation, 'ANNOTATED');
  assert.strictEqual(c.mode, 'REPORT');
  assert.strictEqual(c.reportSampleRate, 1);
  assert.strictEqual(c.maxInspectionMillis, null);
  assert.strictEqual(c.slowInspectionWarnMillis, 250);
});

test('environment results equal Spring Boot 2.2.13 for the same variables (Java-produced fixture)', function () {
  var fixture = h.readJsonFixture('spring-tomcat-web.json').environment;
  var checked = 0;
  fixture.forEach(function (c) {
    var label = JSON.stringify(c.env);
    var enabled;
    try {
      enabled = resolve.isEnabled({ env: c.env });
    } catch (e) {
      enabled = 'ERR';
    }
    if (c.env.EGOV_REQUESTVALIDATION_ENABLED !== undefined) {
      // Boot 2 also binds the spelling without underscores between words; the Node package does not
      assert.strictEqual(enabled, false, label);
      return;
    }
    if (c.java === 'DISABLED') {
      assert.strictEqual(enabled, false, label);
      checked++;
      return;
    }
    if (c.java === 'ERR-COND') {
      assert.throws(function () {
        create({ env: c.env });
      }, RangeError, label);
      checked++;
      return;
    }
    assert.strictEqual(enabled, true, label);
    if (c.java === 'ERR') {
      assert.throws(function () {
        create({ env: c.env });
      }, function (e) {
        return e instanceof RangeError || e instanceof TypeError;
      }, label);
      checked++;
      return;
    }
    var cfg = create({ env: c.env }).config;
    var l = cfg.limits;
    var r = cfg.rules;
    assert.strictEqual(String(cfg.structuredDefault), c.structured, label);
    assert.strictEqual(cfg.activation, c.activation, label);
    assert.strictEqual(cfg.mode, c.mode, label);
    assert.strictEqual(String(cfg.inspectContentTypeHeader), c.ct, label);
    assert.strictEqual(String(cfg.rejectDuplicateKeys), c.dup, label);
    assert.strictEqual(String(cfg.rejectDualRequestInfo), c.dual, label);
    assert.ok(Object.is(cfg.reportSampleRate, c.rate), label);
    assert.deepStrictEqual([l.maxBodyBytes, l.maxDepth, l.maxStringLength, l.maxNameLength, l.maxTokens, l.maxNumberLength,
      l.maxScalarLength], c.limits, label);
    assert.deepStrictEqual([r.markupStart, r.urlScheme, r.eventHandler, r.decodeRounds, r.normalizeNfkc], c.rules, label);
    assert.deepStrictEqual(r.disallowedControls.slice(), c.controls, label);
    assert.deepStrictEqual(r.deniedSchemes.slice(), c.schemes, label);
    assert.deepStrictEqual(r.deniedDataMediaTypes.slice(), c.media, label);
    checked++;
  });
  assert.ok(checked >= 50, 'checked ' + checked);
});

test('precedence: environment over options over defaults, field by field; env: false ignores the environment', function () {
  var c = create({
    env: env({ EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH: '3', EGOV_REQUEST_VALIDATION_MODE: 'ENFORCE' }),
    mode: 'REPORT', limits: { maxDepth: 5, maxBodyBytes: 100 }, rules: { deniedSchemes: ['livescript'] }
  }).config;
  assert.strictEqual(c.mode, 'ENFORCE');
  assert.strictEqual(c.limits.maxDepth, 3);
  assert.strictEqual(c.limits.maxBodyBytes, 100);
  assert.deepStrictEqual(c.rules.deniedSchemes, ['livescript']);
  var off = create({ env: false, enabled: true, structuredDefault: false, mode: 'ENFORCE' });
  assert.strictEqual(off.enabled, true);
  assert.strictEqual(off.config.mode, 'ENFORCE');
  assert.strictEqual(off.config.structuredDefault, false);
  var processEnv = rv.createRequestValidation({ logger: quiet() });
  assert.strictEqual(typeof processEnv.enabled, 'boolean');
});

test('enabled: only "true" (any case) enables; the variable decides when present; disabled never validates', function () {
  assert.strictEqual(create({ env: { EGOV_REQUEST_VALIDATION_ENABLED: 'TRUE', EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT: 'true' } }).enabled, true);
  ['yes', '1', 'on', ' true', 'false', ''].forEach(function (v) {
    assert.strictEqual(create({ env: { EGOV_REQUEST_VALIDATION_ENABLED: v }, enabled: true, structuredDefault: true }).enabled,
        false, JSON.stringify(v));
  });
  assert.strictEqual(create({ env: {}, enabled: true, structuredDefault: true }).enabled, true);
  assert.strictEqual(create({ env: {}, enabled: 'True', structuredDefault: true }).enabled, true);
  assert.strictEqual(create({ env: {} }).enabled, false);
  assert.strictEqual(create({ env: { EGOV_REQUESTVALIDATION_ENABLED: 'true' } }).enabled, false);
  // disabled: invalid values are never looked at
  var disabled = rv.createRequestValidation({ env: { EGOV_REQUEST_VALIDATION_LIMITS_MAX_DEPTH: 'nonsense' },
    limits: { maxDepth: -1 }, mode: 'bogus', routes: 'not-an-array', unknownOption: 1, logger: 5 });
  assert.strictEqual(disabled.enabled, false);
  assert.strictEqual(disabled.config.enabled, false);
  assert.strictEqual(rv.createRequestValidation('not an object').enabled, false);
});

test('enabled without structured-default fails startup naming structured-default', function () {
  [{ env: { EGOV_REQUEST_VALIDATION_ENABLED: 'true' } }, { env: false, enabled: true },
    { env: { EGOV_REQUEST_VALIDATION_ENABLED: 'true', EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT: ' ' } }].forEach(function (o) {
    assert.throws(function () {
      create(o);
    }, /structured-default/);
  });
});

test('invalid settings fail startup when enabled', function () {
  var bad = [
    { limits: { maxDepth: 0 } }, { limits: { maxDepth: 257 } }, { limits: { maxTokens: 2147483648 } }, { limits: { bogus: 1 } },
    { rules: { disallowedControls: [128] } }, { rules: { disallowedControls: [9] } }, { rules: { decodeRounds: 4 } },
    { rules: { deniedSchemes: [' '] } }, { mode: 'DEFAULT' }, { mode: 'bogus' }, { activation: 'SOME' }, { log: { reportSampleRate: 2 } },
    { log: { reportSampleRate: NaN } }, { maxInspectionMillis: 0 }, { slowInspectionWarnMillis: -1 }, { unknown: true },
    { routes: [{ path: 'no-slash' }] }, { routes: [{ path: '/a', limits: { maxDepth: 0 } }] }, { routes: [{ path: '/a', mode: 'x' }] },
    { routes: [{ path: '/a', bogus: 1 }] }, { routes: [{ path: '/a' }, { path: '/A/' }] }, { excludePaths: ['relative'] },
    { routes: [{ path: '/a', skipPaths: ['missing-slash'] }] }, { routes: [{ path: '/a', skipPaths: ['/bad~2escape'] }] },
    { routes: [{ path: '/a', skipPaths: ['/prefix*'] }] }, { logger: {} }, { respond: 'x' }, { env: 'x' }
  ];
  bad.forEach(function (extra) {
    assert.throws(function () {
      create(Object.assign({ env: false, enabled: true, structuredDefault: true }, extra));
    }, function (e) {
      return (e instanceof RangeError || e instanceof TypeError) && e.message.indexOf('request-validation: ') === 0;
    }, JSON.stringify(extra));
  });
  assert.throws(function () {
    create({ env: false, enabled: true, structuredDefault: true, routes: [{ path: '/bad', name: 'BadSkipEndpoint#bad', skipPaths: [''], reason: 'test' }] });
  }, /BadSkipEndpoint#bad.*Empty skip path/);
});

test('NFKC: enabling normalization runs the start-up self-test', function () {
  var c = create({ env: false, enabled: true, structuredDefault: true, rules: { normalizeNfkc: true } });
  assert.strictEqual(c.config.rules.normalizeNfkc, true);
  assert.strictEqual(c.detect('＜script'), 'R1');
});

function policyOf(options, method, url) {
  var o = Object.assign({ env: false, enabled: true, structuredDefault: true, logger: quiet() }, options);
  var r = resolve.resolveConfig(o);
  return r.resolver.resolve(method, url);
}

test('activation ALL inspects every path except disabled routes and exclusions; ANNOTATED only matched routes', function () {
  var opts = { activation: 'ALL', routes: [{ path: '/off', enabled: false, reason: 'r' }], excludePaths: [{ path: '/tracing', reason: 'proxy' }] };
  assert.strictEqual(policyOf(opts, 'GET', '/anything').enabled, true);
  assert.strictEqual(policyOf(opts, 'GET', '/off').enabled, false);
  assert.strictEqual(policyOf(opts, 'GET', '/off/sub').enabled, true, 'exact entries do not cover sub-paths');
  assert.strictEqual(policyOf(opts, 'GET', '/tracing').enabled, false);
  assert.strictEqual(policyOf(opts, 'GET', '/tracing/api/traces').enabled, false);
  assert.strictEqual(policyOf(opts, 'GET', '/TRACING/').enabled, false);
  assert.strictEqual(policyOf(opts, 'GET', '/tracingx').enabled, true);
  var ann = { activation: 'ANNOTATED', routes: [{ path: '/a', method: 'POST' }, { path: '/p', prefix: true }] };
  assert.strictEqual(policyOf(ann, 'POST', '/a').enabled, true);
  assert.strictEqual(policyOf(ann, 'GET', '/a').enabled, false);
  assert.strictEqual(policyOf(ann, 'GET', '/p/x').enabled, true);
  assert.strictEqual(policyOf(ann, 'GET', '/other').enabled, false);
  assert.strictEqual(policyOf({ activation: 'ANNOTATED' }, 'GET', '/x').enabled, false);
});

test('route matching: case-insensitive, optional trailing slash, segment-boundary prefixes, method rules', function () {
  var opts = { activation: 'ANNOTATED', routes: [{ path: '/api/items', method: 'GET', name: 'list' }, { path: '/api', prefix: true, name: 'api' }] };
  assert.strictEqual(policyOf(opts, 'GET', '/API/Items/').name, 'list');
  assert.strictEqual(policyOf(opts, 'HEAD', '/api/items').name, 'list', 'a GET entry covers HEAD');
  assert.strictEqual(policyOf(opts, 'POST', '/api/items').name, 'api');
  assert.strictEqual(policyOf(opts, 'GET', '/api/items//').name, 'api', 'two trailing slashes are another path');
  assert.strictEqual(policyOf(opts, 'GET', '/api/%69tems').name, 'api', 'matching is on the raw, undecoded path');
  assert.strictEqual(policyOf(opts, 'GET', '/api%2Fitems').enabled, false, 'an encoded slash is not a segment boundary');
  assert.strictEqual(policyOf(opts, 'GET', '/apix').enabled, false);
  var both = { routes: [{ path: '/a', prefix: true, mode: 'REPORT', name: 'any' }, { path: '/a', prefix: true, method: 'POST', mode: 'ENFORCE', name: 'post' }] };
  assert.strictEqual(policyOf(both, 'POST', '/a/b').name, 'post', 'at equal length a method-specific entry wins');
  assert.strictEqual(policyOf(both, 'GET', '/a/b').name, 'any');
  var longest = { routes: [{ path: '/a', prefix: true, name: 'short' }, { path: '/a/b', prefix: true, name: 'long' }] };
  assert.strictEqual(policyOf(longest, 'GET', '/a/b/c').name, 'long');
  assert.strictEqual(policyOf(longest, 'GET', '/a/c').name, 'short');
  var exact = { routes: [{ path: '/x', name: 'any' }, { path: '/x', method: 'PUT', name: 'put' }] };
  assert.strictEqual(policyOf(exact, 'PUT', '/x').name, 'put');
  assert.strictEqual(policyOf(exact, 'DELETE', '/x').name, 'any');
  assert.strictEqual(policyOf({}, 'GET', '/x').name, null);
});

test('policy merge: global, class (prefix), method (exact); skip paths unioned; limits inherit per field', function () {
  var opts = {
    structuredDefault: false, mode: 'REPORT', limits: { maxDepth: 10, maxScalarLength: 100 },
    routes: [
      { path: '/svc', prefix: true, skipPaths: ['/class'], reason: 'class markup', limits: { maxDepth: 7 } },
      { path: '/svc/op', structured: true, mode: 'ENFORCE', skipPaths: ['/method', '/class'], reason: 'method markup', limits: { maxScalarLength: 20 } }
    ]
  };
  var p = policyOf(opts, 'POST', '/svc/op');
  assert.strictEqual(p.enabled, true);
  assert.strictEqual(p.structured, true);
  assert.strictEqual(p.mode, 'ENFORCE');
  assert.deepStrictEqual(p.skipPaths.slice(), ['/class', '/method']);
  assert.strictEqual(p.limits.maxDepth, 7);
  assert.strictEqual(p.limits.maxScalarLength, 20);
  assert.strictEqual(p.limits.maxBodyBytes, 10485760);
  assert.ok(p.matcher.matches(['method', 'child']));
  assert.ok(p.matcher.matches(['class']));
  assert.ok(!p.matcher.matches(['other']));
  var q = policyOf(opts, 'POST', '/svc/other');
  assert.strictEqual(q.structured, false);
  assert.strictEqual(q.mode, 'REPORT');
  assert.strictEqual(q.limits.maxScalarLength, 100);
  var r = policyOf({ routes: [{ path: '/svc', prefix: true, enabled: false, reason: 'x' }, { path: '/svc/op', enabled: true }] }, 'GET', '/svc/op');
  assert.strictEqual(r.enabled, true, 'the method-level entry decides enabled last');
});

test('the policy memo is bounded (10,000 entries) and memoises by method and normalised path', function () {
  var r = resolve.resolveConfig({ env: false, enabled: true, structuredDefault: true, activation: 'ALL', logger: quiet() });
  var first = r.resolver.resolve('GET', '/a/');
  assert.strictEqual(r.resolver.resolve('get', '/A'), first);
  for (var i = 0; i < 25000; i++) {
    r.resolver.resolve('GET', '/random/' + i);
  }
  assert.ok(r.resolver.memo.size <= 10000, 'memo size ' + r.resolver.memo.size);
});

test('startup lines: configuration, per-route coverage, exclusion reasons and missing reasons', function () {
  var logger = mini.recordingLogger();
  rv.createRequestValidation({
    env: env({ EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS: '/proxy' }), logger: logger, activation: 'ALL', mode: 'ENFORCE',
    routes: [
      { path: '/skippath', method: 'POST', name: 'Endpoints#skippath', skipPaths: ['/note'], reason: 'rich text' },
      { path: '/bytes', method: 'POST', name: 'Endpoints#bytes' },
      { path: '/off', enabled: false },
      { path: '/raw', structured: false, reason: '  ' }
    ]
  });
  var lines = logger.lines.map(function (l) {
    return l.level + ' ' + l.line;
  });
  assert.deepStrictEqual(lines, [
    'info request_validation_config enabled=true activation=ALL mode=ENFORCE structuredDefault=true jar_sha256=' + rv.CONFORMS_TO.jarSha256,
    'info request_validation_coverage handler=Endpoints#skippath enabled=true mode=ENFORCE',
    'info request_validation_exclusion handler=Endpoints#skippath reason=rich text',
    'info request_validation_coverage handler=Endpoints#bytes enabled=true mode=ENFORCE',
    'info request_validation_coverage handler=* /off enabled=false mode=ENFORCE',
    'warn request_validation_exclusion_missing_reason handler=* /off',
    'info request_validation_coverage handler=* /raw enabled=true mode=ENFORCE',
    'warn request_validation_exclusion_missing_reason handler=* /raw',
    'info request_validation_coverage handler=* /proxy enabled=false mode=ENFORCE',
    'warn request_validation_exclusion_missing_reason handler=* /proxy'
  ]);
  var annotated = mini.recordingLogger();
  rv.createRequestValidation({ env: env(), logger: annotated, routes: [{ path: '/x', enabled: false, reason: 'off' }] });
  assert.ok(annotated.lines.some(function (l) {
    return l.level === 'warn' && l.line === 'request_validation_nothing_validated';
  }));
  var unsafe = mini.recordingLogger();
  rv.createRequestValidation({ env: env(), logger: unsafe, routes: [{ path: '/x', name: 'n<script>\u0000', skipPaths: ['/a'], reason: 'why <b>\n' }] });
  assert.ok(unsafe.lines.every(function (l) {
    return !/[<>\u0000\n]/.test(l.line);
  }));
});

test('exclusions: options.excludePaths, the environment list replacing it, and the effective configuration', function () {
  var c = create({ env: env({ EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS: ' /a , /b/c ' }), excludePaths: [{ path: '/code', reason: 'r' }] }).config;
  assert.deepStrictEqual(c.excludePaths.map(function (e) {
    return e.path;
  }), ['/a', '/b/c']);
  var d = create({ env: env(), excludePaths: ['/x', { path: '/y', reason: 'proxy' }], routes: [{ path: '/r', method: 'post', limits: { maxDepth: 3 } }] }).config;
  assert.deepStrictEqual(JSON.parse(JSON.stringify(d.excludePaths)), [{ path: '/x' }, { path: '/y', reason: 'proxy' }]);
  assert.deepStrictEqual(JSON.parse(JSON.stringify(d.routes)), [{ path: '/r', prefix: false, method: 'POST', name: 'POST /r', skipPaths: [], limits: { maxDepth: 3 } }]);
  assert.ok(Object.isFrozen(d) && Object.isFrozen(d.routes) && Object.isFrozen(d.routes[0]) && Object.isFrozen(d.limits));
  assert.throws(function () {
    create({ env: env({ EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS: '/a,,/b' }) });
  }, /NODE_EXCLUDE_PATHS/);
  assert.strictEqual(create({ env: env({ EGOV_REQUEST_VALIDATION_NODE_EXCLUDE_PATHS: '' }), excludePaths: ['/x'] }).config.excludePaths.length, 0);
  assert.strictEqual(create({ env: env({ EGOV_REQUEST_VALIDATION_NODE_MAX_INSPECTION_MILLIS: '200', EGOV_REQUEST_VALIDATION_NODE_SLOW_INSPECTION_WARN_MILLIS: '0' }) }).config.maxInspectionMillis, 200);
});
