'use strict';

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var ContentDetector = h.lib('ContentDetector');
var ContentPolicy = h.lib('ContentPolicy');
var cases = require('./support/cases');
var REF = h.readJsonFixture('java-reference.json');
var ORACLE = h.readJsonFixture('detect-oracle.json');

test('every Java ContentDetectorTest text case (java-test-cases.json kind=text)', function () {
  var textCases = REF.cases.filter(function (c) {
    return c.kind === 'text' && c.api === undefined;
  });
  assert.strictEqual(textCases.length, 29);
  textCases.forEach(function (c) {
    var detector = new ContentDetector(c.config || {});
    assert.strictEqual(detector.detect(c.input), c.expected.rule, c.source + ' ' + JSON.stringify(c.input));
  });
});

test('detect equals the Java jar on 16,371 trap probes across 17 policies', function () {
  // Expected rule ids were produced by the request-validation 1.0.1-SNAPSHOT jar for exactly these values and
  // policies: Java whitespace and case-mapping traps, jsoup entity quirks, percent runs with invalid UTF-8, NFKC on
  // and off, decode rounds 0-3, rules switched off, custom schemes and media types, and long values.
  var detectors = ORACLE.policies.map(function (p) {
    return new ContentDetector(p);
  });
  var expanded = new Map();
  var mismatches = [];
  var seen = { R1: 0, R2: 0, R3: 0, R4: 0, PASS: 0 };
  ORACLE.cases.forEach(function (c) {
    var key = typeof c[0] === 'string' ? c[0] : JSON.stringify(c[0]);
    var value = expanded.get(key);
    if (value === undefined) {
      value = cases.expand(c[0]);
      expanded.set(key, value);
    }
    var got = detectors[c[1]].detect(value);
    seen[c[2] === null ? 'PASS' : c[2]]++;
    if (got !== c[2]) {
      mismatches.push(JSON.stringify(value).slice(0, 60) + ' policy ' + c[1] + ': java ' + c[2] + ', node ' + got);
    }
  });
  assert.strictEqual(ORACLE.cases.length, 16371);
  assert.deepStrictEqual(mismatches.slice(0, 10), []);
  Object.keys(seen).forEach(function (k) {
    assert.ok(seen[k] > 100, k + ' exercised');
  });
});

test('rule precedence and gates', function () {
  var d = new ContentDetector(ContentPolicy.defaults());
  assert.strictEqual(d.detect('<a onclick=1 javascript:'), 'R1');
  assert.strictEqual(d.detect('javascript:x onclick=1\u0000'), 'R2');
  assert.strictEqual(d.detect('x onclick=1\u0000'), 'R3');
  assert.strictEqual(new ContentDetector({ markupStart: false }).detect('<a onclick=1'), 'R3');
  assert.strictEqual(new ContentDetector({ markupStart: false, eventHandler: false }).detect('<a onclick=1\u0000'), 'R4');
  assert.strictEqual(new ContentDetector({ disallowedControls: [] }).detect('a\u0000'), null);
  assert.strictEqual(new ContentDetector({ deniedSchemes: [], deniedDataMediaTypes: [] }).detect('javascript:x'), null);
  assert.strictEqual(d.detect('plain text without sentinels'), null);
  assert.strictEqual(d.detect(''), null);
  assert.throws(function () { d.detect(null); }, TypeError);
  assert.throws(function () { new ContentDetector(); }, TypeError);
  assert.throws(function () { new ContentDetector({ decodeRounds: 9 }); }, RangeError);
});

test('variants: Java order, 40-variant cap and NFKC-only special case', function () {
  var d = new ContentDetector({ decodeRounds: 3 });
  assert.deepStrictEqual(d.variants('plain'), ['plain']);
  assert.deepStrictEqual(d.variants('%2526lt;b'), ['%2526lt;b', '%26lt;b', '&lt;b', '<b']);
  assert.deepStrictEqual(new ContentDetector({}).variants('%2526lt;b'), ['%2526lt;b', '%26lt;b', '&lt;b']);
  var n0 = new ContentDetector({ decodeRounds: 0, normalizeNfkc: true });
  assert.deepStrictEqual(n0.variants('\uFF1C%3C'), ['\uFF1C%3C', '<%3C']);
  assert.deepStrictEqual(n0.variants('abc'), ['abc']);
  // A value whose decodings branch at every round stops at 40 variants.
  var branching = '%25' + cases.repeat('&amp;%26%2526&#37;', 6) + '3C';
  var many = new ContentDetector({ decodeRounds: 3, normalizeNfkc: true }).variants(branching);
  assert.ok(many.length <= ContentDetector.MAX_VARIANTS);
  assert.strictEqual(new Set(many).size, many.length);
  assert.strictEqual(many[0], branching);
});

test('URL canonicalization follows Java (Character.toLowerCase per unit, then Locale.ROOT per code point)', function () {
  var c = ContentDetector.canonicalizeUrlPrefix;
  assert.strictEqual(c(' \t\u180E JAVA\nSCR\u0130PT:x'), 'javascript:x');
  assert.strictEqual(c('\uD801\t\uDC00X'), '\uD801\uDC28x', 'TAB removal joins a surrogate pair before Locale.ROOT');
  assert.strictEqual(c('\u00A0javascript:'), '\u00A0javascript:', 'NBSP is not leading space in Java');
  assert.strictEqual(c(''), '');
  var d = new ContentDetector({ deniedSchemes: ['\uD801\uDC00x', 'kscript'] });
  assert.strictEqual(d.detect('\uD801\t\uDC00X:1'), 'R2');
  assert.strictEqual(d.detect('\u212Ascript:1'), 'R2');
  assert.strictEqual(new ContentDetector({}).detect('JAVASCR\u0130PT:x'), 'R2');
  assert.strictEqual(new ContentDetector({}).detect('javascr\u0131pt:x'), null);
  assert.strictEqual(new ContentDetector({}).detect('data: Text/HTML ;x'), 'R2');
  assert.strictEqual(new ContentDetector({}).detect('data:text/html\u00A0,x'), null);
});

test('detector instances are immutable and independent of later policy input changes', function () {
  var schemes = ['custom'];
  var d = new ContentDetector({ deniedSchemes: schemes });
  schemes.push('javascript');
  assert.strictEqual(d.detect('javascript:x'), null);
  assert.strictEqual(d.detect('custom:x'), 'R2');
  assert.ok(Object.isFrozen(d) && Object.isFrozen(d.policy));
});
