'use strict';

// Every inspection limit at exactly the limit (accepted) and one past it (rejected), with the reference jar's exact
// result, plus the limit rules that are easy to get wrong: which check fires first, and Jackson's own limits.

var assert = require('assert');
var h = require('./harness');
var test = h.test;
var probes = require('./support/jsonProbes');

var JsonDocumentInspector = h.lib('JsonDocumentInspector');
var ContentDetector = h.lib('ContentDetector');
var ContentPolicy = h.lib('ContentPolicy');
var InspectionLimits = h.lib('InspectionLimits');
var SkipPathMatcher = h.lib('SkipPathMatcher');
var InspectionError = h.lib('InspectionError');

var inspector = new JsonDocumentInspector(new ContentDetector(ContentPolicy.defaults()));
var NONE = new SkipPathMatcher([]);
var EXPECTED = h.readJsonFixture('json-oracle.json').limits;

function limits(overrides) {
  return InspectionLimits.fromObject(overrides || {});
}

function result(json, lim, dup, dual) {
  var body = Buffer.isBuffer(json) ? json : Buffer.from(json, 'utf8');
  var found = [];
  try {
    inspector.inspect(body, lim || InspectionLimits.defaults(), NONE, dup !== false, dual !== false, function (v) {
      found.push(String(v));
    });
  } catch (e) {
    if (!(e instanceof InspectionError)) {
      throw e;
    }
    found.push(String(e.violation));
  }
  return found.length === 0 ? 'PASS' : found.join(';');
}

test('each limit exactly at the limit and one past it: the jar results', function () {
  var rows = probes.limitBoundaries();
  assert.strictEqual(rows.length, EXPECTED.count);
  assert.strictEqual(h.sha256(probes.tsv(rows)), EXPECTED.inputSha256);
  rows.forEach(function (r) {
    var c = probes.config(r);
    var values = {};
    InspectionLimits.FIELDS.forEach(function (f) {
      values[f] = c[f];
    });
    assert.strictEqual(result(r.body, InspectionLimits.fromObject(values)), EXPECTED.verdicts[r.id], r.id);
  });
  // every "-exact" row passes; every "-plus1" row is rejected
  rows.forEach(function (r) {
    if (/-exact$/.test(r.id)) {
      assert.strictEqual(EXPECTED.verdicts[r.id], 'PASS', r.id);
    } else if (/-plus1/.test(r.id)) {
      assert.notStrictEqual(EXPECTED.verdicts[r.id], 'PASS', r.id);
    }
  });
});

test('maxBodyBytes is checked before parsing; an empty body is malformed, never over a limit', function () {
  assert.strictEqual(result('{"a":', limits({ maxBodyBytes: 4 })), 'REQUEST_LIMIT_EXCEEDED|max-body-bytes|/|5');
  assert.strictEqual(result(Buffer.alloc(0), limits({ maxBodyBytes: 1 })), 'REQUEST_JSON_MALFORMED|json|/|0');
  assert.strictEqual(result('\uFEFF{}', limits({ maxBodyBytes: 5 })), 'PASS'); // the BOM counts as body bytes
  assert.strictEqual(result('\uFEFF{}', limits({ maxBodyBytes: 4 })), 'REQUEST_LIMIT_EXCEEDED|max-body-bytes|/|5');
});

test('depth, string length and number digits are enforced by the parser (parser-limit), before the library checks', function () {
  assert.strictEqual(result('[[1]]', limits({ maxDepth: 1 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/|0');
  assert.strictEqual(result('{"a":{"b":1}}', limits({ maxDepth: 1 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/a|0');
  assert.strictEqual(result('["abc"]', limits({ maxStringLength: 2 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/0|0');
  // digits after a name are parsed with the name: the failure names the previous location
  assert.strictEqual(result('{"x":1,"n":123456}', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/x|0');
  // the library's own check counts the whole text (sign, '.', exponent)
  assert.strictEqual(result('{"n":-12345}', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|max-number-length|/n|6');
});

test('float digit counts: a missing fraction or exponent counts -1 on the slow path (leading zero)', function () {
  // fast path: 1 + 5 fraction digits = 6 > 5; slow path (leading zero): 1 + 5 - 1 = 5, then the text length 7 > 5
  assert.strictEqual(result('[1.12345]', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/|0');
  assert.strictEqual(result('[0.12345]', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|max-number-length|/0|7');
  assert.strictEqual(result('[1e1234]', limits({ maxNumberLength: 4 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/|0');
  assert.strictEqual(result('[0e1234]', limits({ maxNumberLength: 4 })), 'REQUEST_LIMIT_EXCEEDED|max-number-length|/0|6');
  // a number that reaches the end of a 4,000-unit read takes the slow path too
  var pad = 'a'.repeat(4000 - 2 - 4);
  assert.strictEqual(result('["' + pad + '",1.12345]', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|max-number-length|/1|7');
  assert.strictEqual(result('["' + pad.slice(10) + '",1.12345]', limits({ maxNumberLength: 5 })), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/0|0');
});

test('names: Jackson rejects a new name over 50,000 units even when maxNameLength is larger', function () {
  var big = limits({ maxNameLength: 1000000 });
  assert.strictEqual(result('{"' + 'n'.repeat(50000) + '":1}', big), 'PASS');
  assert.strictEqual(result('{"' + 'n'.repeat(50001) + '":1}', big), 'REQUEST_LIMIT_EXCEEDED|parser-limit|/|0');
  assert.strictEqual(result('{"' + 'n'.repeat(257) + '":1}'), 'REQUEST_LIMIT_EXCEEDED|max-name-length|/*|257');
});

test('maxTokens counts every token (names and end markers too) and is checked before trailing content', function () {
  assert.strictEqual(result('{} []', limits({ maxTokens: 2 })), 'REQUEST_LIMIT_EXCEEDED|max-tokens|/|3');
  assert.strictEqual(result('{} []', limits({ maxTokens: 3 })), 'REQUEST_JSON_MALFORMED|trailing-content|/|0');
  assert.strictEqual(result('{"a":1}', limits({ maxTokens: 4 })), 'PASS');
  assert.strictEqual(result('{"a":1}', limits({ maxTokens: 3 })), 'REQUEST_LIMIT_EXCEEDED|max-tokens|/a|4');
  assert.strictEqual(result('{"a":1}', limits({ maxTokens: 2 })), 'REQUEST_LIMIT_EXCEEDED|max-tokens|/a|3');
});

test('maxScalarLength never applies to a JSON body', function () {
  assert.strictEqual(result('{"k":"' + 'v'.repeat(5000) + '"}', limits({ maxScalarLength: 1 })), 'PASS');
});
