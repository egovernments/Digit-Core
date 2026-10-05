'use strict';

// The kind=json cases of the Java test suite (JsonDocumentInspectorTest), with the Java tests' own expectations.

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var JsonDocumentInspector = h.lib('JsonDocumentInspector');
var ContentDetector = h.lib('ContentDetector');
var ContentPolicy = h.lib('ContentPolicy');
var InspectionLimits = h.lib('InspectionLimits');
var SkipPathMatcher = h.lib('SkipPathMatcher');
var InspectionError = h.lib('InspectionError');
var ViolationCode = h.lib('ViolationCode');

var DATA = h.readJsonFixture('java-json-cases.json');
// JsonDocumentInspectorTest.GENEROUS: the limits every case without its own limits uses
var GENEROUS = new InspectionLimits(100000, 64, 10000, 256, 10000, 1000, 4096);
var inspector = new JsonDocumentInspector(new ContentDetector(ContentPolicy.defaults()));

function bodyOf(c) {
  if (c.input_bytes_hex !== undefined) {
    return Buffer.from(c.input_bytes_hex, 'hex');
  }
  var bytes = Buffer.from(c.input, c.input_encoding === 'UTF-16LE' ? 'utf16le' : 'utf8');
  if (c.input_bom) {
    bytes = Buffer.concat([Buffer.from([0xEF, 0xBB, 0xBF]), bytes]);
  }
  return bytes;
}

function limitsOf(c, body) {
  var given = c.config && c.config.limits;
  if (!given) {
    return GENEROUS;
  }
  var values = {};
  Object.keys(given).forEach(function (key) {
    values[key] = given[key] === 'exact byte length of input' ? body.length : given[key];
  });
  return InspectionLimits.fromObject(values);
}

function run(c) {
  var body = bodyOf(c);
  var config = c.config || {};
  var findings = [];
  var consumer = config.consumerThrowsOnFirstViolation
    ? function (v) {
      throw new InspectionError(v);
    }
    : function (v) {
      findings.push(v);
    };
  var error = null;
  try {
    inspector.inspect(body, limitsOf(c, body), new SkipPathMatcher(config.skipPaths || []),
        config.rejectDuplicateKeys === undefined ? true : config.rejectDuplicateKeys,
        config.rejectDualRequestInfo === undefined ? true : config.rejectDualRequestInfo, consumer);
  } catch (e) {
    error = e;
  }
  return { findings: findings, error: error };
}

test('all 35 kind=json Java test cases are present', function () {
  assert.strictEqual(DATA.cases.length, 35);
  DATA.cases.forEach(function (c) {
    assert.strictEqual(c.kind, 'json');
  });
});

DATA.cases.forEach(function (c, index) {
  var label = 'java case ' + index + ': ' + c.source + ' ' + JSON.stringify(c.input !== undefined ? c.input : c.input_bytes_hex).slice(0, 60);
  test(label, function () {
    var r = run(c);
    var e = c.expected;
    if (e.violations !== undefined || e.locations !== undefined) {
      assert.strictEqual(r.error, null, String(r.error && r.error.violation));
      if (e.violations !== undefined) {
        assert.strictEqual(r.findings.length, e.violations.length);
        e.violations.forEach(function (v, i) {
          if (v.ruleId !== undefined) {
            assert.strictEqual(r.findings[i].ruleId, v.ruleId);
          }
          if (v.location !== undefined) {
            assert.strictEqual(r.findings[i].location, v.location);
          }
          assert.strictEqual(r.findings[i].code, ViolationCode.REQUEST_CONTENT_NOT_ALLOWED);
        });
      }
      if (e.locations !== undefined) {
        assert.deepStrictEqual(r.findings.map(function (v) {
          return v.location;
        }), e.locations);
      }
      return;
    }
    if (e.accepted) {
      assert.strictEqual(r.error, null, String(r.error && r.error.violation));
      return;
    }
    assert.ok(r.error instanceof InspectionError, 'expected an InspectionError');
    var v = r.error.violation;
    assert.strictEqual(v.code, e.code);
    if (e.ruleId !== undefined) {
      assert.strictEqual(v.ruleId, e.ruleId);
    }
    if (e.location !== undefined) {
      assert.strictEqual(v.location, e.location);
    }
    if (Object.prototype.hasOwnProperty.call(e, 'cause')) {
      assert.strictEqual(r.error.cause, undefined);
      assert.strictEqual(r.error.message, ViolationCode.getMessage(e.code));
    }
  });
});

test('Java failureLocationsMatchRelease100 with the test source limits (depth3, tokens4)', function () {
  var depth3 = new InspectionLimits(1000, 3, 100, 100, 1000, 100, 100);
  var tokens4 = new InspectionLimits(1000, 64, 100, 100, 4, 100, 100);
  function failure(json, limits) {
    try {
      inspector.inspect(Buffer.from(json, 'utf8'), limits, new SkipPathMatcher([]), true, true, function () {});
    } catch (e) {
      return e.violation.code + '|' + e.violation.ruleId + '|' + e.violation.location;
    }
    return 'PASS';
  }
  assert.strictEqual(failure('{"x":{"y":{"z":{}}}}', depth3), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/x/y/z');
  assert.strictEqual(failure('{"a":1,"b":2}', tokens4), 'REQUEST_LIMIT_EXCEEDED|max-tokens|/b');
});

test('Java acceptsLimitsExactlyAndRejectsLimitPlusOne with the test source limits', function () {
  var none = new SkipPathMatcher([]);
  var accepted = Buffer.from('{"abc":"xyz","n":123}', 'utf8');
  inspector.inspect(accepted, new InspectionLimits(accepted.length, 1, 3, 3, 6, 3, 1), none, true, true, function () {});
  [
    ['{"a":"four"}', new InspectionLimits(100, 2, 3, 10, 10, 10, 1)],
    ['{"four":1}', new InspectionLimits(100, 2, 10, 3, 10, 10, 1)],
    ['{"a":1234}', new InspectionLimits(100, 2, 10, 10, 10, 3, 1)],
    ['{"a":{}}', new InspectionLimits(100, 1, 10, 10, 10, 10, 1)],
    ['[1]', new InspectionLimits(100, 2, 10, 10, 2, 10, 1)],
    ['{}', new InspectionLimits(1, 2, 10, 10, 10, 10, 1)]
  ].forEach(function (pair) {
    assert.throws(function () {
      inspector.inspect(Buffer.from(pair[0], 'utf8'), pair[1], none, true, true, function () {});
    }, function (e) {
      return e instanceof InspectionError && e.violation.code === ViolationCode.REQUEST_LIMIT_EXCEEDED;
    }, pair[0]);
  });
});
