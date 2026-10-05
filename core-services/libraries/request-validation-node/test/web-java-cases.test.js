'use strict';

// Every config, annotation and http case of the Java tests (fixture java-web-cases.json, taken verbatim from
// the Java library's test suite), each run through its Node equivalent: @ValidateRequest on a class is a prefix
// route, on a method an exact route; application properties are options; HTTP requests go through the in-process
// Express-like stack. A case without a handler fails the suite.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var rv = require(path.join(h.ROOT, 'index.js'));
var defaults = require(path.join(h.ROOT, 'lib', 'config', 'defaults'));
var resolve = require(path.join(h.ROOT, 'lib', 'config', 'resolve'));
var Violation = require(path.join(h.ROOT, 'lib', 'core', 'Violation'));
var Reporter = require(path.join(h.ROOT, 'lib', 'web', 'reporter'));
var logging = require(path.join(h.ROOT, 'lib', 'web', 'auditLogger'));
var mini = require('./support/miniapp');

var cases = h.readJsonFixture('java-web-cases.json').cases;
var ON = { EGOV_REQUEST_VALIDATION_ENABLED: 'true', EGOV_REQUEST_VALIDATION_STRUCTURED_DEFAULT: 'true' };
var JSON_CT = { 'content-type': 'application/json' };
var LIMIT_FIELDS = ['maxBodyBytes', 'maxDepth', 'maxStringLength', 'maxNameLength', 'maxTokens', 'maxNumberLength', 'maxScalarLength'];

function make(options) {
  var logger = mini.recordingLogger();
  var instance = rv.createRequestValidation(Object.assign({ env: false, logger: logger }, options));
  return { rv: instance, logger: logger, app: mini.serviceApp(instance, options && options.rawJson ? { rawJson: true } : undefined) };
}

function service(mode, extra) {
  var o = Object.assign({ enabled: true, structuredDefault: true, activation: 'ALL', mode: mode }, extra || {});
  var raw = o.rawJson;
  delete o.rawJson;
  var t = make(o);
  if (raw) {
    t.app = mini.serviceApp(t.rv, { rawJson: true });
  }
  t.logger.clear();
  return t;
}

function post(t, url, body, headers) {
  return mini.request(t.app, { method: 'POST', url: url, headers: headers || JSON_CT, body: body });
}

function queryOf(q) {
  return '?' + Object.keys(q).map(function (k) {
    return encodeURIComponent(k) + '=' + encodeURIComponent(q[k]);
  }).join('&');
}

function codeOf(r) {
  return JSON.parse(r.body).Errors[0].code;
}

function rules(t) {
  return t.logger.findings().map(function (f) {
    return f.rule;
  });
}

function throwsWith(fn, pattern) {
  assert.throws(fn, function (e) {
    return (e instanceof RangeError || e instanceof TypeError) && (!pattern || pattern.test(e.message));
  });
}

function limitsFromArray(values) {
  var limits = {};
  values.forEach(function (v, i) {
    limits[LIMIT_FIELDS[i]] = v === 'MAX_TOKENS_CEILING+1' ? 2147483648 : v;
  });
  return limits;
}

var handlers = {
  // ---------------------------------------------------------------------------------------------- config
  'ContentDetectorTest#policyRejectsNonC0AndExemptWhitespaceControls': function (c) {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, rules: c.input });
    }, /disallowedControls/);
  },
  'InspectionLimitsTest#defaultsMatchThePublishedStartingLimits': function (c) {
    assert.deepStrictEqual(make({ enabled: true, structuredDefault: true }).rv.config.limits, c.expected);
  },
  'InspectionLimitsTest#rejectsZeroAndUnsafeCeilingsAtConstructionTime': function (c) {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, limits: limitsFromArray(c.input) });
    });
  },
  'SkipPathMatcherTest#emptyPointerIsRejectedAndSlashIsTheEmptyNameProperty': function (c) {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, routes: [{ path: '/x', skipPaths: c.input, reason: 'r' }] });
    }, /Empty skip path/);
  },
  'SkipPathMatcherTest#rejectsInvalidPointerEscapesAndPartialWildcards': function (c) {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, routes: [{ path: '/x', skipPaths: c.input, reason: 'r' }] });
    });
  },
  'ValidationPolicyResolverTest#requiresExplicitStructuredDefault': function () {
    throwsWith(function () {
      make({ enabled: true });
    }, /structured-default/);
  },
  'ValidationPolicyResolverTest#propertiesBoundWhileDisabledNeverFailButEnabledRequiresTheDefault': function (c) {
    if (c.input.enabled) {
      throwsWith(function () {
        make(c.input);
      }, /structured-default/);
    } else {
      assert.strictEqual(make(Object.assign({ limits: { maxDepth: 0 } }, c.input)).rv.enabled, false);
    }
  },
  'EnvironmentVariableFallbackTest#knowsEverySetting': function (c) {
    assert.strictEqual(defaults.SETTINGS.filter(function (s) {
      return !s.nodeOnly;
    }).length, c.expected.settingCount);
  },
  'EnvironmentVariableFallbackTest#declaresAVariableNoOtherSourceMentionsDirectlyAfterTheEnvironment': function (c) {
    var t = make({ env: c.input.env, enabled: c.input.applicationProperties['egov.request-validation.enabled'] === 'true',
      structuredDefault: true });
    assert.strictEqual(t.rv.config.limits.maxDepth, 9);
    assert.strictEqual(t.rv.config.mode, 'ENFORCE');
  },
  'EnvironmentVariableFallbackTest#leavesASettingThatAnotherSourceSpellsInAnyFormToSpring': function () {
    // Node has one other source (options); the environment still takes precedence over it
    var t = make({ env: Object.assign({ EGOV_REQUEST_VALIDATION_MODE: 'ENFORCE' }, ON), mode: 'REPORT',
      rules: { deniedSchemes: ['sysscheme'] } });
    assert.strictEqual(t.rv.config.mode, 'ENFORCE');
    assert.deepStrictEqual(t.rv.config.rules.deniedSchemes, ['sysscheme']);
  },
  'EnvironmentVariableFallbackTest#declaresAnEmptyListButNoOtherBlankSetting': function (c) {
    var env = Object.assign({}, c.input.env, ON);
    throwsWith(function () {
      make({ env: env });
    }, /DECODE_ROUNDS/);
    delete env.EGOV_REQUEST_VALIDATION_RULES_DECODE_ROUNDS;
    var cfg = make({ env: env }).rv.config;
    assert.strictEqual(cfg.activation, 'ANNOTATED');
    assert.strictEqual(cfg.limits.maxDepth, 64);
    assert.strictEqual(cfg.rules.urlScheme, true);
    assert.strictEqual(cfg.reportSampleRate, 1);
    assert.deepStrictEqual(cfg.rules.deniedSchemes, []);
    assert.deepStrictEqual(cfg.rules.deniedDataMediaTypes, ['text/html', 'application/xhtml+xml', 'image/svg+xml']);
    assert.strictEqual(cfg.mode, 'ENFORCE');
  },
  'EnvironmentVariablesTest#environmentVariablesAlone': function (c) {
    var cfg = make({ env: c.input.env }).rv.config;
    assert.strictEqual('mode=' + cfg.mode + ' depth=' + cfg.limits.maxDepth + ' schemes=[' + cfg.rules.deniedSchemes.join(', ')
        + '] media=[' + cfg.rules.deniedDataMediaTypes.join(', ') + ']', c.expected);
  },
  'EnvironmentVariablesTest#valuesAreConvertedAsSpringConvertsThem': function (c) {
    var cfg = make({ env: Object.assign({}, c.input.env, ON) }).rv.config;
    assert.strictEqual('mode=' + cfg.mode + ' depth=' + cfg.limits.maxDepth + ' schemes=[' + cfg.rules.deniedSchemes.join(', ')
        + '] media=[' + cfg.rules.deniedDataMediaTypes.join(', ') + ']', c.expected);
  },
  'EnvironmentVariablesTest#listsReplaceTheDefaults': function (c) {
    assert.strictEqual('schemes=[' + make({ env: Object.assign({}, c.input.env, ON) }).rv.config.rules.deniedSchemes.join(', ') + ']',
        c.expected);
  },
  'EnvironmentVariablesTest#theKillSwitchAndNothingConfigured': function (c) {
    var t = make({ env: c.input.env, enabled: true, structuredDefault: true });
    assert.strictEqual(t.rv.enabled, false);
  },
  'RequestValidationIntegrationTest#globalKillSwitchDoesNotRequireBodyDefault': function (c) {
    assert.strictEqual(make(c.input).rv.enabled, false);
  },
  'RequestValidationIntegrationTest#enabledWithoutStructuredDefaultFailsStartup': function (c) {
    throwsWith(function () {
      make(c.input);
    }, /structured-default/);
  },
  'Boot1RequestValidationIntegrationTest#theBoot2EnvironmentVariableFormIsIgnoredOnBoot15': function (c) {
    // Node supports only the EGOV_REQUEST_VALIDATION_* spelling so this form is ignored as on Boot 1.5
    assert.strictEqual(make({ env: c.input.env }).rv.enabled, false);
  },

  // ---------------------------------------------------------------------------------------------- annotation
  'ValidationPolicyResolverTest#parameterOverridesModeAndBodySwitchWhilePathsCombine': function (c) {
    var base = { env: false, enabled: true, structuredDefault: c.config.structuredDefault, logger: { warn: function () {} } };
    var classRoute = { path: '/c', prefix: true, skipPaths: ['/class'], reason: 'class markup' };
    var handler = resolve.resolveConfig(Object.assign({ routes: [classRoute,
      { path: '/c/m', structured: true, skipPaths: ['/method'], reason: 'method markup' }] }, base)).resolver.resolve('POST', '/c/m');
    assert.strictEqual(handler.structured, c.expected.handlerPolicy.structured);
    // the parameter level folds into the method-level route in Node: its body settings are the route's
    var param = resolve.resolveConfig(Object.assign({ routes: [classRoute,
      { path: '/c/m', structured: false, mode: 'ENFORCE', skipPaths: ['/method', '/parameter'], reason: 'body disabled' }] }, base))
        .resolver.resolve('POST', '/c/m');
    var e = c.expected.parameterPolicy;
    assert.strictEqual(param.enabled, e.enabled);
    assert.strictEqual(param.structured, e.structured);
    assert.strictEqual(param.mode, e.mode);
    assert.deepStrictEqual(param.skipPaths.slice(), e.skipPaths);
    assert.ok(param.matcher.matches(e.matcherMatches));
  },
  'ValidationPolicyResolverTest#parameterAnnotationDoesNotActivateAnUnannotatedHandler': function (c) {
    var r = resolve.resolveConfig({ env: false, enabled: true, structuredDefault: c.config.structuredDefault, activation: c.config.activation,
      logger: { warn: function () {} } });
    assert.strictEqual(r.resolver.resolve('POST', '/plain').enabled, c.expected.enabled);
  },
  'ValidationPolicyResolverTest#rejectsInvalidHandlerLimits': function () {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, routes: [{ path: '/m', limits: { maxDepth: 0 } }] });
    }, /maxDepth/);
  },
  'RequestValidationIntegrationTest#emptySkipPathFailsStartupNamingTheHandler': function (c) {
    throwsWith(function () {
      make({ enabled: true, structuredDefault: true, routes: [{ path: '/bad', name: 'BadSkipEndpoint#bad', skipPaths: [''], reason: 'test' }] });
    }, new RegExp(c.expected.startupFailsWithMessageContaining.map(function (s) {
      return s.replace(/[#]/g, '\\$&');
    }).join('.*')));
  },

  // ---------------------------------------------------------------------------------------------- http
  'EnvironmentVariablesTest#aSpringApplication': function (c) {
    var t = make({ env: Object.assign({ EGOV_REQUEST_VALIDATION_ACTIVATION: 'ALL' }, c.input.env) });
    return c.input.cases.reduce(function (p, k) {
      return p.then(function () {
        return post(t, c.input.path, k.body).then(function (r) {
          assert.strictEqual(r.status, k.expectedStatus, k.body);
        });
      });
    }, Promise.resolve());
  },
  'RequestValidationIntegrationTest#checksUnknownFieldsAndWrongTypedValuesBeforeBinding': function (c) {
    var t = service(c.config.mode);
    return post(t, c.input.path, c.input.body).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
      assert.strictEqual(codeOf(r), c.expected.body_jsonpath['$.Errors[0].code']);
      assert.strictEqual(r.handled, false);
      if (c.expected.body_excludes) {
        assert.strictEqual(r.body.indexOf(c.expected.body_excludes), -1);
      }
    });
  },
  'RequestValidationIntegrationTest#acceptedBytesAndHttpEntityBytesAreUnchanged': function (c) {
    var t = service(c.config.mode, { rawJson: true });
    return post(t, c.input.path, c.input.body, { 'content-type': c.input.contentType }).then(function (r) {
      assert.strictEqual(r.status, 200);
      assert.strictEqual(Buffer.from(JSON.parse(r.body).body, 'base64').toString('utf8'), c.input.body);
    });
  },
  'RequestValidationIntegrationTest#disabledBodyStillChecksScalarTextBeforeNumericConversion': function (c) {
    var t = service(c.config.mode, { routes: [{ path: '/skip', structured: false, reason: 'raw' }] });
    return post(t, '/skip' + queryOf(c.input.query), c.input.body).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
      if (c.expected.status === 400) {
        assert.strictEqual(codeOf(r), c.expected.body_jsonpath['$.Errors[0].code']);
      }
    });
  },
  'RequestValidationIntegrationTest#reportModePreservesBytesAndRegistersAdviceOnce': function (c) {
    var t = service(c.config.mode, { rawJson: true });
    return post(t, c.input.path, c.input.body).then(function (r) {
      assert.strictEqual(r.status, 200);
      assert.strictEqual(Buffer.from(JSON.parse(r.body).body, 'base64').toString('utf8'), c.input.body);
      if (c.expected.loggedObservationCount !== undefined) {
        assert.strictEqual(t.logger.findings().length, c.expected.loggedObservationCount);
      }
    });
  },
  'RequestValidationIntegrationTest#rejectsCharsetDisagreementAndDoesNotActivatePlainControllers': function (c) {
    var annotated = c.input.path === '/plain';
    var t = service(c.config.mode, annotated ? { activation: 'ANNOTATED', routes: [{ path: '/bytes', method: 'POST' }] } : {});
    return post(t, c.input.path, c.input.body, { 'content-type': c.input.contentType }).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
      if (c.expected.status === 400) {
        assert.strictEqual(codeOf(r), c.expected.body_jsonpath['$.Errors[0].code']);
      }
    });
  },
  'RequestValidationIntegrationTest#dependencyOnlyStartupIsInert': function (c) {
    var t = make({});
    return post(t, c.input.path, c.input.body).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
    });
  },
  'RequestValidationIntegrationTest#reportLogsStructuralFindingsAndLeavesTheBodyToTheConverter': function (c) {
    var extra = { rawJson: true };
    if (c.annotation && c.annotation.maxBodyBytes) {
      extra.routes = [{ path: c.input.path, limits: { maxBodyBytes: c.annotation.maxBodyBytes } }];
    }
    var t = service(c.config.mode, extra);
    var body = c.input.body_latin1 !== undefined ? Buffer.from(c.input.body_latin1, 'latin1') : c.input.body;
    return post(t, c.input.path, body, { 'content-type': c.input.contentType }).then(function (r) {
      // the host decides: here a body-parser-like JSON parser (415 for ISO-8859-1, where Spring decodes it)
      assert.ok(r.status === 200 || (r.status === 415 && /ISO-8859-1/.test(c.input.contentType)), String(r.status));
      if (r.status === 200) {
        assert.strictEqual(Buffer.from(JSON.parse(r.body).body, 'base64').toString('latin1'), Buffer.from(body).toString('latin1'));
      }
      var logged = rules(t);
      if (c.expected.loggedLineCount !== undefined) {
        assert.strictEqual(logged.length, c.expected.loggedLineCount);
      }
      if (c.expected.loggedLastRuleId) {
        assert.strictEqual(logged[logged.length - 1], c.expected.loggedLastRuleId);
      }
      if (c.expected.loggedRuleIds) {
        assert.deepStrictEqual(logged, c.expected.loggedRuleIds);
      }
    });
  },
  'RequestValidationIntegrationTest#bodyLimitsInBothModesReadAtMostLimitPlusOneFromOneStream': function (c) {
    // Stream mechanics have no Node equivalent (D10): body-parser owns the stream. Mapped to the decisions: with a
    // declared length the finding comes before the body is read; ENFORCE rejects either way.
    var t = service(c.config.mode, { routes: [{ path: '/small', limits: { maxBodyBytes: c.annotation.maxBodyBytes } }] });
    var headers = c.input.declaresContentLength === false ? { 'content-type': 'application/json', 'transfer-encoding': 'chunked' } : JSON_CT;
    return post(t, '/small', c.input.body, headers).then(function (r) {
      if (c.config.mode === 'ENFORCE') {
        assert.strictEqual(r.status, 400);
        assert.strictEqual(codeOf(r), 'REQUEST_LIMIT_EXCEEDED');
      } else {
        assert.strictEqual(r.status, 200);
        assert.deepStrictEqual(rules(t), ['max-body-bytes', 'inspection-incomplete']);
      }
    });
  },
  'RequestValidationIntegrationTest#transportFailurePropagatesInReportAndIsFixedTextInEnforce': function (c) {
    var t = service(c.config.mode);
    return mini.request(t.app, { method: 'POST', url: '/json', headers: JSON_CT, body: '{"a":"connection reset"}', abort: true })
        .then(function (r) {
          if (c.config.mode === 'ENFORCE') {
            assert.strictEqual(r.status, 400);
            assert.strictEqual(JSON.parse(r.body).Errors[0].message, c.expected.message);
            assert.strictEqual(r.body.indexOf('reset'), -1);
          } else {
            assert.strictEqual(r.error.type, 'request.aborted');
            assert.deepStrictEqual(rules(t), c.expected.loggedRuleIds);
            assert.strictEqual(t.logger.findings()[0].kind, c.expected.loggedKind);
          }
        });
  },
  'RequestValidationIntegrationTest#overflowReplayPassesZeroByteReadsAndLeavesClosingToTheConverter': function (c) {
    // D10: the host already holds the whole body; REPORT hands it on unchanged
    var t = service(c.config.mode, { rawJson: true, routes: [{ path: '/small', limits: { maxBodyBytes: c.annotation.maxBodyBytes } }] });
    return post(t, '/small', c.input.body, { 'content-type': 'application/json', 'transfer-encoding': 'chunked' }).then(function (r) {
      assert.strictEqual(r.status, 200);
      assert.strictEqual(Buffer.from(JSON.parse(r.body).body, 'base64').toString('utf8'), c.input.body);
    });
  },
  'RequestValidationIntegrationTest#enforceRejectionIsLoggedEvenAfterReportFindingsFillTheCap': function (c) {
    // A route has one mode for scalars and body in Node; the mixed-mode cap rule is the reporter's, checked there.
    var logger = mini.recordingLogger();
    var reporter = new Reporter(new logging.AuditLogger(logger, 1));
    var ctx = { req: {}, state: { count: 0, incomplete: false }, handler: '/mixed', method: 'POST' };
    Object.keys(c.input.query).forEach(function (name) {
      reporter.content(ctx, 'REPORT', new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', name, 3), 'query/form');
    });
    var rejection = reporter.content(ctx, 'ENFORCE', new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', '/', 8), 'body');
    assert.strictEqual(rejection.status, c.expected.status);
    var f = logger.findings();
    assert.strictEqual(f.length, c.expected.loggedObservationCount);
    assert.ok(f.slice(0, 9).every(function (x) {
      return x.mode === c.expected.loggedModesFirst9;
    }));
    assert.strictEqual(f[9].mode, c.expected.loggedMode10th);
    assert.strictEqual(f[9].kind, c.expected.loggedKind10th);
  },
  'RequestValidationIntegrationTest#reportStopsScalarInspectionAtALimitButStillInspectsTheBody': function (c) {
    var t = service(c.config.mode, { limits: { maxScalarLength: c.annotation.maxScalarLength } });
    return post(t, '/scalars' + queryOf(c.input.query), c.input.body).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
      if (c.expected.loggedRuleIds) {
        assert.deepStrictEqual(rules(t), c.expected.loggedRuleIds);
      } else {
        assert.strictEqual(codeOf(r), c.expected.body_jsonpath['$.Errors[0].code']);
      }
    });
  },
  'RequestValidationIntegrationTest#reportKeepsTheLastLogSlotForIncompleteInspection': function (c) {
    var t = service(c.config.mode, { rawJson: true });
    return post(t, c.input.path, c.input.body).then(function (r) {
      assert.strictEqual(r.status, c.expected.status);
      var logged = rules(t);
      assert.strictEqual(logged.length, c.expected.loggedLineCount);
      assert.ok(logged.slice(0, 9).every(function (x) {
        return x === c.expected.loggedRuleIdsFirst9;
      }));
      assert.strictEqual(logged[9], c.expected.loggedRuleId10th);
    });
  },
  'RequestValidationIntegrationTest#coverageShowsSkipPathsInBracketsAndSkipsOnlyThatField': function (c) {
    var t = make({ enabled: true, structuredDefault: true, activation: 'ALL', mode: c.config.mode,
      routes: [{ path: '/skippath', method: 'POST', name: 'Endpoints#skippath', skipPaths: ['/note'], reason: 'rich text' }] });
    var startup = t.logger.lines.map(function (l) {
      return l.line;
    });
    assert.ok(startup.indexOf('request_validation_coverage handler=Endpoints#skippath enabled=true mode=ENFORCE') !== -1);
    assert.ok(startup.indexOf('request_validation_exclusion handler=Endpoints#skippath reason=rich text') !== -1);
    return post(t, '/skippath', c.expected.status200ForBody).then(function (r) {
      assert.strictEqual(r.status, 200);
      return post(t, '/skippath', c.expected.status400ForBody);
    }).then(function (r) {
      assert.strictEqual(r.status, 400);
    });
  }
};

test('every config, annotation and http case of the Java tests has a Node equivalent here', function () {
  var counts = { config: 0, annotation: 0, http: 0 };
  cases.forEach(function (c) {
    assert.ok(typeof handlers[c.source] === 'function', 'no handler for ' + c.source);
    counts[c.kind]++;
  });
  assert.deepStrictEqual(counts, { config: 24, annotation: 4, http: 28 });
});

cases.forEach(function (c, index) {
  test('java case ' + index + ' (' + c.kind + ') ' + c.source, function () {
    return handlers[c.source](c);
  });
});
