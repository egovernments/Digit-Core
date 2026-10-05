'use strict';

var assert = require('assert');
var fs = require('fs');
var path = require('path');
var h = require('./harness');
var test = h.test;

var ContentPolicy = h.lib('ContentPolicy');
var EventHandlerNameList = h.lib('EventHandlerNameList');
var REF = h.readJsonFixture('java-reference.json');

// --- ContentPolicy ------------------------------------------------------------------------------------------------

test('ContentPolicy: builder defaults equal the Java constants', function () {
  var p = ContentPolicy.defaults();
  var cd = REF.contentDetection;
  assert.strictEqual(p.markupStart, true);
  assert.strictEqual(p.urlScheme, true);
  assert.strictEqual(p.eventHandler, true);
  assert.deepStrictEqual(p.disallowedControls.slice(), cd.disallowedControls);
  assert.deepStrictEqual(p.deniedSchemes.slice(), cd.deniedSchemes);
  assert.deepStrictEqual(p.deniedDataMediaTypes.slice(), cd.deniedDataMediaTypes);
  assert.strictEqual(p.decodeRounds, cd.decodeRounds);
  assert.strictEqual(p.normalizeNfkc, cd.normalizeNfkc);
  assert.ok(Object.isFrozen(p) && Object.isFrozen(p.deniedSchemes) && Object.isFrozen(p.disallowedControls));
  assert.deepStrictEqual(new ContentPolicy().toJSON(), p.toJSON());
  assert.deepStrictEqual(new ContentPolicy({}).toJSON(), p.toJSON());
});

test('ContentPolicy: Java test cases reject non-C0 and exempt whitespace controls', function () {
  REF.cases.filter(function (c) {
    return c.source.indexOf('ContentDetectorTest#policyRejectsNonC0AndExemptWhitespaceControls') === 0;
  }).forEach(function (c) {
    assert.throws(function () { new ContentPolicy(c.input); }, RangeError, JSON.stringify(c.input));
  });
  [[9], [10], [13], [32], [-1], [128], [0.5], ['0'], [null]].forEach(function (bad) {
    assert.throws(function () { new ContentPolicy({ disallowedControls: bad }); },
        /^RangeError: disallowedControls must contain only C0 controls other than TAB, LF and CR$/, JSON.stringify(bad));
  });
  var ok = [];
  for (var c = 0; c <= 31; c++) {
    if (c !== 9 && c !== 10 && c !== 13) {
      ok.push(c);
    }
  }
  assert.deepStrictEqual(new ContentPolicy({ disallowedControls: ok.concat([0, 1]) }).disallowedControls.slice(), ok);
  assert.deepStrictEqual(new ContentPolicy({ disallowedControls: new Set([31, 0]) }).disallowedControls.slice(), [31, 0]);
  assert.deepStrictEqual(new ContentPolicy({ disallowedControls: [] }).disallowedControls.slice(), []);
});

test('ContentPolicy: copies mutable input sets (policyCopiesMutableInputSets)', function () {
  var schemes = new Set(['custom']);
  var p = new ContentPolicy({ deniedSchemes: schemes });
  schemes.add('later');
  assert.deepStrictEqual(p.deniedSchemes.slice(), ['custom']);
  var arr = ['a'];
  var q = new ContentPolicy({ deniedDataMediaTypes: arr });
  arr.push('b');
  assert.deepStrictEqual(q.deniedDataMediaTypes.slice(), ['a']);
});

test('ContentPolicy: schemes and media types are String.trim()med and lower-cased with Locale.ROOT', function () {
  var p = new ContentPolicy({
    deniedSchemes: [' JavaScript ', '\u000bVBSCRIPT\u001f', 'JAVASCRIPT', '\u212Ascript', '\u0130x', '\u00A0x\u00A0'],
    deniedDataMediaTypes: ['TEXT/HTML', ' Image/SVG+XML\t']
  });
  // U+212A KELVIN SIGN lower-cases to 'k'; U+0130 to "i" + U+0307; NBSP (> U+0020) is not trimmed.
  assert.deepStrictEqual(p.deniedSchemes.slice(), ['javascript', 'vbscript', 'kscript', 'i\u0307x', '\u00A0x\u00A0']);
  assert.deepStrictEqual(p.deniedDataMediaTypes.slice(), ['text/html', 'image/svg+xml']);
});

test('ContentPolicy: blank values, bad types, decodeRounds range and unknown keys', function () {
  ['', ' ', '\t\n', '\u0000 '].forEach(function (blank) {
    assert.throws(function () { new ContentPolicy({ deniedSchemes: ['ok', blank] }); }, /^RangeError: deniedSchemes contains a blank value$/);
    assert.throws(function () { new ContentPolicy({ deniedDataMediaTypes: [blank] }); },
        /^RangeError: deniedDataMediaTypes contains a blank value$/);
  });
  assert.throws(function () { new ContentPolicy({ deniedSchemes: [null] }); }, /blank value/);
  assert.throws(function () { new ContentPolicy({ deniedSchemes: [1] }); }, TypeError);
  assert.throws(function () { new ContentPolicy({ deniedSchemes: 'javascript' }); }, TypeError);
  assert.throws(function () { new ContentPolicy({ deniedSchemes: null }); }, TypeError);
  [0, 1, 2, 3].forEach(function (r) {
    assert.strictEqual(new ContentPolicy({ decodeRounds: r }).decodeRounds, r);
  });
  [-1, 4].forEach(function (r) {
    assert.throws(function () { new ContentPolicy({ decodeRounds: r }); }, /^RangeError: decodeRounds must be between 0 and 3$/);
  });
  assert.throws(function () { new ContentPolicy({ decodeRounds: 1.5 }); }, TypeError);
  assert.throws(function () { new ContentPolicy({ decodeRounds: '2' }); }, TypeError);
  assert.throws(function () { new ContentPolicy({ markupStart: 'yes' }); }, TypeError);
  assert.throws(function () { new ContentPolicy({ normalizeNFKC: true }); }, /unknown ContentPolicy option/);
  assert.throws(function () { new ContentPolicy(null); }, TypeError);
  assert.throws(function () { new ContentPolicy([]); }, TypeError);
  // Java validation order: controls before schemes before media types before decode rounds.
  assert.throws(function () {
    new ContentPolicy({ disallowedControls: [9], deniedSchemes: [''], decodeRounds: 9 });
  }, /disallowedControls/);
  assert.throws(function () {
    new ContentPolicy({ deniedSchemes: [''], deniedDataMediaTypes: [''], decodeRounds: 9 });
  }, /deniedSchemes/);
  assert.throws(function () { new ContentPolicy({ deniedDataMediaTypes: [''], decodeRounds: 9 }); }, /deniedDataMediaTypes/);
});

// --- EventHandlerNameList -----------------------------------------------------------------------------------------

test('EventHandlerNameList: loads exactly the 124 Java names in resource order', function () {
  var names = EventHandlerNameList.load();
  assert.strictEqual(names.length, 124);
  assert.deepStrictEqual(names.slice(), REF.contentDetection.eventHandlerNames);
  assert.ok(Object.isFrozen(names));
  assert.strictEqual(EventHandlerNameList.load(), names);
});

test('EventHandlerNameList: Java parser rules', function () {
  var parse = EventHandlerNameList.parse;
  assert.deepStrictEqual(parse('# c\n\n  OnClick \r\nonload\ronclick\n').slice(), ['onclick', 'onload']);
  assert.deepStrictEqual(parse('on\u212Aeydown').slice(), ['onkeydown'], 'Locale.ROOT lower-casing maps U+212A to k');
  assert.deepStrictEqual(parse('\u000b onabort \u001f').slice(), ['onabort'], 'String.trim strips units <= U+0020');
  assert.deepStrictEqual(parse('#onfoo\non1').slice(), ['on1']);
  ['on', 'o', 'onclick!', 'on_click', 'on-click', 'x', '\u00A0onclick', 'onclick\u00A0', 'on\u0130x', ' #x\nonclick x'].forEach(function (bad) {
    assert.throws(function () { parse(bad); }, /^Error: Event-handler resource contains an invalid name$/, JSON.stringify(bad));
  });
  ['', '\n\n', '# only\n#comments', '   \r\n\t'].forEach(function (empty) {
    assert.throws(function () { parse(empty); }, /^Error: Event-handler name resource is empty$/, JSON.stringify(empty));
  });
  // A BOM is not stripped by the Java reader, so a leading BOM line is invalid.
  assert.throws(function () { parse('\uFEFFonclick'); }, /invalid name/);
});

test('EventHandlerNameList: the shipped file is byte-identical to the jar resource recorded in TABLES.sha256', function () {
  var file = path.join(h.ROOT, 'data', 'event-handler-names.txt');
  var line = fs.readFileSync(path.join(h.ROOT, 'data', 'TABLES.sha256'), 'utf8').split('\n').filter(function (l) {
    return / {2}data\/event-handler-names\.txt$/.test(l);
  })[0];
  assert.ok(line, 'hash line present');
  assert.strictEqual(h.sha256(fs.readFileSync(file)), line.split(' ')[0]);
  var lines = fs.readFileSync(file, 'utf8').split('\n');
  assert.strictEqual(lines.filter(function (l) { return l.charAt(0) === '#'; }).length, 4);
});
