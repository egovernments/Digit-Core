'use strict';

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var ViolationCode = h.lib('ViolationCode');
var Violation = h.lib('Violation');
var InspectionError = h.lib('InspectionError');
var SafeLocationFormatter = h.lib('SafeLocationFormatter');
var InspectionLimits = h.lib('InspectionLimits');
var JavaRandom = require('./support/javaRandom');
var REF = h.readJsonFixture('java-reference.json');

function casesFor(prefix) {
  return REF.cases.filter(function (c) {
    return c.source.indexOf(prefix) === 0;
  });
}

// --- ViolationCode ------------------------------------------------------------------------------------------------

test('ViolationCode: exactly the four Java codes with their fixed messages, in declaration order', function () {
  assert.deepStrictEqual(Object.keys(ViolationCode), Object.keys(REF.violationCodes));
  assert.deepStrictEqual(ViolationCode.VALUES.slice(), Object.keys(REF.violationCodes));
  Object.keys(REF.violationCodes).forEach(function (code) {
    assert.strictEqual(ViolationCode[code], code);
    assert.strictEqual(ViolationCode.getMessage(code), REF.violationCodes[code]);
    assert.strictEqual(ViolationCode.VIOLATION_MESSAGES[code], REF.violationCodes[code]);
  });
  assert.ok(Object.isFrozen(ViolationCode) && Object.isFrozen(ViolationCode.VIOLATION_MESSAGES));
  assert.strictEqual(ViolationCode.isViolationCode('R1'), false);
  assert.strictEqual(ViolationCode.isViolationCode('toString'), false);
  assert.throws(function () { ViolationCode.getMessage('NOPE'); }, TypeError);
});

// --- Violation ----------------------------------------------------------------------------------------------------

test('Violation: fields, freezing and toString', function () {
  var v = new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', '/a/0/b', 7);
  assert.strictEqual(v.code, 'REQUEST_CONTENT_NOT_ALLOWED');
  assert.strictEqual(v.ruleId, 'R1');
  assert.strictEqual(v.location, '/a/0/b');
  assert.strictEqual(v.length, 7);
  assert.ok(Object.isFrozen(v));
  assert.strictEqual(String(v), 'REQUEST_CONTENT_NOT_ALLOWED|R1|/a/0/b|7');
  assert.deepStrictEqual(JSON.parse(JSON.stringify(v)), { code: 'REQUEST_CONTENT_NOT_ALLOWED', ruleId: 'R1', location: '/a/0/b', length: 7 });
});

test('Violation: constructor sanitizes an externally supplied location (SafeLocationFormatterTest)', function () {
  casesFor('SafeLocationFormatterTest#violationConstructorSanitizesExternallySuppliedLocation').forEach(function (c) {
    assert.strictEqual(new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R1', c.input, 9).location, c.expected);
  });
  assert.strictEqual(new Violation('REQUEST_JSON_MALFORMED', 'json', '/', 0).location, '/');
  assert.strictEqual(new Violation('REQUEST_JSON_MALFORMED', 'json', null, 0).location, '*');
  assert.strictEqual(new Violation('REQUEST_JSON_MALFORMED', 'json', '', 0).location, '*');
  assert.strictEqual(new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R2', 'q', 1).location, 'q');
  assert.strictEqual(new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R2', 'a b', 1).location, '*');
  assert.strictEqual(new Violation('REQUEST_CONTENT_NOT_ALLOWED', 'R2', '/Content-Type', 1).location, '/Content-Type');
});

test('Violation: rule id must match [A-Za-z0-9_.-]{1,64}; code and length are validated (Java order)', function () {
  ['R1', 'max-body-bytes', 'parser-limit', 'inspection-incomplete', 'a.b_c-D9', new Array(65).join('x')].forEach(function (id) {
    assert.strictEqual(new Violation('REQUEST_LIMIT_EXCEEDED', id, '/', 0).ruleId, id);
  });
  ['', ' ', 'R1 ', 'a/b', 'r\u00e9', '<b>', new Array(66).join('x')].forEach(function (id) {
    assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', id, '/', 0); }, RangeError, JSON.stringify(id));
  });
  assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', null, '/', 0); }, TypeError);
  assert.throws(function () { new Violation('NOT_A_CODE', 'R1', '/', 0); }, TypeError);
  assert.throws(function () { new Violation(undefined, 'R1', '/', 0); }, TypeError);
  assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', 'R1', '/', -1); }, /length must not be negative/);
  assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', 'R1', '/', 1.5); }, TypeError);
  assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', 'R1', '/', '3'); }, TypeError);
  // ruleId is checked before the length, as in Java.
  assert.throws(function () { new Violation('REQUEST_LIMIT_EXCEEDED', 'bad id', '/', -1); }, /ruleId is not safe/);
});

test('Violation: rule-id check equals the regex definition over 20,000 random strings', function () {
  var regex = /^[A-Za-z0-9_.-]{1,64}$/;
  var alphabet = 'aZ09_.-/ *~<>=:%&\u00e9\u0928\u0000\uFF1C';
  var random = new JavaRandom(5);
  for (var run = 0; run < 20000; run++) {
    var length = random.nextInt(70);
    var value = '';
    for (var i = 0; i < length; i++) {
      value += alphabet.charAt(random.nextInt(alphabet.length));
    }
    var ok = true;
    try {
      new Violation('REQUEST_CONTENT_NOT_ALLOWED', value, '/', 0);
    } catch (e) {
      ok = false;
    }
    assert.strictEqual(ok, regex.test(value), JSON.stringify(value));
  }
});

// --- InspectionError ----------------------------------------------------------------------------------------------

test('InspectionError: carries the violation, fixed message per code, no cause', function () {
  Object.keys(REF.violationCodes).forEach(function (code) {
    var v = new Violation(code, 'json', '/x', 0);
    var e = new InspectionError(v);
    assert.ok(e instanceof Error && e instanceof InspectionError);
    assert.strictEqual(e.violation, v);
    assert.strictEqual(e.message, REF.violationCodes[code]);
    assert.strictEqual(e.name, 'InspectionError');
    assert.strictEqual(e.cause, undefined);
    assert.ok(String(e.stack).indexOf(REF.violationCodes[code]) !== -1);
  });
  assert.throws(function () { new InspectionError({ code: 'REQUEST_JSON_MALFORMED' }); }, TypeError);
  assert.throws(function () { new InspectionError(); }, TypeError);
});

// --- SafeLocationFormatter ----------------------------------------------------------------------------------------

test('SafeLocationFormatter: Java test cases (format, scalar, 64-unit boundary, null)', function () {
  casesFor('SafeLocationFormatterTest#').forEach(function (c) {
    if (c.api === 'SafeLocationFormatter.format') {
      assert.strictEqual(SafeLocationFormatter.format(c.input), c.expected, c.source);
    } else if (c.api === 'SafeLocationFormatter.scalar') {
      assert.strictEqual(SafeLocationFormatter.scalar(c.input), c.expected, c.source);
    }
  });
  assert.strictEqual(REF.maxSafeSegment, SafeLocationFormatter.MAX_SAFE_SEGMENT);
});

test('SafeLocationFormatter: character scan equals the regex definition (SafeLocationFormatterTest, seed 7)', function () {
  // Replays Java's characterScanMatchesThePreviousRegexDefinition with java.util.Random(7) and its alphabet.
  var previous = /^[A-Za-z0-9_.-]{1,64}$/;
  var alphabet = 'aZ09_.-/ *~<>=:%&\u00e9\u0928\u0000\uFF1C';
  var random = new JavaRandom(7);
  var kept = 0;
  for (var run = 0; run < 20000; run++) {
    var length = random.nextInt(70);
    var segment = '';
    for (var i = 0; i < length; i++) {
      segment += alphabet.charAt(random.nextInt(alphabet.length));
    }
    var expected = previous.test(segment) ? segment : '*';
    if (expected !== '*') {
      kept++;
    }
    assert.strictEqual(SafeLocationFormatter.scalar(segment), expected, JSON.stringify(segment));
  }
  assert.ok(kept > 0, 'both outcomes exercised');
});

test('SafeLocationFormatter: sanitizeLocation keeps "/" and empty parts as "*"', function () {
  var s = SafeLocationFormatter.sanitizeLocation;
  assert.strictEqual(s('/'), '/');
  assert.strictEqual(s('//'), '/*/*');
  assert.strictEqual(s('/a//b'), '/a/*/b');
  assert.strictEqual(s('/a/'), '/a/*');
  assert.strictEqual(s('/onload='), '/*');
  assert.strictEqual(s('value'), 'value');
  assert.strictEqual(s('va lue'), '*');
  assert.strictEqual(s(undefined), '*');
  assert.strictEqual(s(42), '*');
  assert.strictEqual(SafeLocationFormatter.format(['a', '0', 'b c', '']), '/a/0/*/*');
  assert.strictEqual(SafeLocationFormatter.scalar(0), '*', 'indexes must be passed as strings');
  assert.throws(function () { SafeLocationFormatter.format('a'); }, TypeError);
});

// --- InspectionLimits ---------------------------------------------------------------------------------------------

test('InspectionLimits: defaults and ceilings match the Java constants', function () {
  var d = InspectionLimits.defaults();
  assert.deepStrictEqual(d.toJSON(), REF.inspectionLimits.defaults);
  assert.deepStrictEqual(Object.assign({}, InspectionLimits.CEILINGS), REF.inspectionLimits.ceilings);
  assert.ok(Object.isFrozen(d));
  assert.strictEqual(InspectionLimits.defaults(), d);
  casesFor('InspectionLimitsTest#defaultsMatchThePublishedStartingLimits').forEach(function (c) {
    assert.deepStrictEqual(d.toJSON(), c.expected);
  });
});

test('InspectionLimits: Java test cases reject zero and unsafe ceilings', function () {
  casesFor('InspectionLimitsTest#rejectsZeroAndUnsafeCeilingsAtConstructionTime').forEach(function (c) {
    var args = c.input.map(function (v) {
      return v === 'MAX_TOKENS_CEILING+1' ? InspectionLimits.MAX_TOKENS_CEILING + 1 : v;
    });
    assert.throws(function () {
      new InspectionLimits(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
    }, RangeError, JSON.stringify(c.input));
  });
});

test('InspectionLimits: every limit accepts 1 and its ceiling and rejects 0 and ceiling + 1', function () {
  var fields = InspectionLimits.FIELDS;
  fields.forEach(function (field, index) {
    var ceiling = InspectionLimits.CEILINGS[field];
    [1, ceiling].forEach(function (ok) {
      var values = fields.map(function () { return 1; });
      values[index] = ok;
      assert.strictEqual(new (Function.prototype.bind.apply(InspectionLimits, [null].concat(values)))()[field], ok);
    });
    [0, -1, ceiling + 1].forEach(function (bad) {
      var values = fields.map(function () { return 1; });
      values[index] = bad;
      assert.throws(function () {
        new (Function.prototype.bind.apply(InspectionLimits, [null].concat(values)))();
      }, new RegExp('^RangeError: ' + field + ' must be between 1 and ' + ceiling + '$'));
    });
    [1.5, NaN, Infinity, '5', null, undefined].forEach(function (bad) {
      var values = fields.map(function () { return 1; });
      values[index] = bad;
      assert.throws(function () {
        new (Function.prototype.bind.apply(InspectionLimits, [null].concat(values)))();
      }, TypeError);
    });
  });
});

test('InspectionLimits.fromObject: fields inherit independently from a base; unknown keys rejected', function () {
  var l = InspectionLimits.fromObject({ maxDepth: 3 });
  assert.strictEqual(l.maxDepth, 3);
  assert.strictEqual(l.maxBodyBytes, REF.inspectionLimits.defaults.maxBodyBytes);
  var l2 = InspectionLimits.fromObject({ maxTokens: 9 }, l);
  assert.strictEqual(l2.maxDepth, 3);
  assert.strictEqual(l2.maxTokens, 9);
  assert.throws(function () { InspectionLimits.fromObject({ maxDepth: 2 }, null); }, /is required/);
  assert.throws(function () { InspectionLimits.fromObject({ maxdepth: 2 }); }, /unknown limit/);
  assert.throws(function () { InspectionLimits.fromObject({ maxDepth: 257 }); }, RangeError);
  assert.throws(function () { InspectionLimits.fromObject(null); }, TypeError);
});
