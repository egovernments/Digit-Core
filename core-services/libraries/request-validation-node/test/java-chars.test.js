'use strict';

var assert = require('assert');
var fs = require('fs');
var path = require('path');
var h = require('./harness');
var test = h.test;

var chars = h.lib('jdk/javaChars');

test('data files match data/TABLES.sha256 and every data file is listed', function () {
  var dataDir = path.join(h.ROOT, 'data');
  var listed = {};
  fs.readFileSync(path.join(dataDir, 'TABLES.sha256'), 'utf8').split('\n').forEach(function (line) {
    if (line.length === 0 || line.charAt(0) === '#') {
      return;
    }
    var m = /^([0-9a-f]{64}) {2}(data\/[^/]+)$/.exec(line);
    assert.ok(m, 'well-formed line: ' + line);
    listed[m[2]] = m[1];
  });
  var files = fs.readdirSync(dataDir).filter(function (f) {
    return f !== 'TABLES.sha256';
  }).map(function (f) {
    return 'data/' + f;
  }).sort();
  assert.deepStrictEqual(Object.keys(listed).sort(), files);
  files.forEach(function (f) {
    assert.strictEqual(h.sha256(fs.readFileSync(path.join(h.ROOT, f))), listed[f], f);
  });
  var tables = fs.readFileSync(path.join(dataDir, 'TABLES.sha256'), 'utf8');
  assert.ok(tables.indexOf('c3b55d47f25d4b027d0109c25dd61b30dda78339a3cb627613ce07004c7e85b4') !== -1, 'jar sha256 recorded');
  assert.ok(tables.indexOf('1e75b08d7019546a954f1e359477f916f537a34d') !== -1, 'jsoup jar sha1 recorded');
});

test('java8-chars.json: generated on Java 8u504 with the table sizes the port relies on', function () {
  var data = JSON.parse(fs.readFileSync(path.join(h.ROOT, 'data', 'java8-chars.json'), 'utf8'));
  assert.strictEqual(data.javaVersion, '1.8.0_504');
  assert.strictEqual(chars.JAVA_VERSION, '1.8.0_504');
  assert.strictEqual(data.whitespace.length, 26);
  assert.strictEqual(data.lowerChar.length, 1003);
  assert.strictEqual(data.lowerRoot.length, 1043);
  assert.deepStrictEqual(data.nfkcOpaque, [0x32FF]);
  assert.deepStrictEqual(data.toUpperCaseIsX, [0x58, 0x78]);
});

test('isWhitespace: the 26 Java 8 units (U+180E included; NBSP, U+2007, U+202F, U+FEFF excluded)', function () {
  var expected = [0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x1680, 0x180E, 0x2000, 0x2001, 0x2002,
    0x2003, 0x2004, 0x2005, 0x2006, 0x2008, 0x2009, 0x200A, 0x2028, 0x2029, 0x205F, 0x3000];
  var found = [];
  for (var c = 0; c <= 0xFFFF; c++) {
    if (chars.isWhitespace(c)) {
      found.push(c);
    }
  }
  assert.deepStrictEqual(found, expected);
});

test('isLetter, isJavaIdentifierPart, isISOControl, isDefined: spot checks from the Java 8 runtime', function () {
  assert.strictEqual(chars.isLetter(0x41), true);
  assert.strictEqual(chars.isLetter(0x30), false);
  assert.strictEqual(chars.isLetter(0xE9), true);
  assert.strictEqual(chars.isLetter(0x0928), true);
  assert.strictEqual(chars.isLetter(0xD800), false, 'a surrogate unit is not a letter');
  assert.strictEqual(chars.isLetter(0x9FCD), true, 'Corretto 8u504 reports U+9FCD as a letter');
  assert.strictEqual(chars.isJavaIdentifierPart(0x9FCD), false, 'but not as an identifier part');
  assert.strictEqual(chars.isJavaIdentifierPart(0x20BD), false);
  assert.strictEqual(chars.isJavaIdentifierPart(0x24), true);
  assert.strictEqual(chars.isJavaIdentifierPart(0x5F), true);
  assert.strictEqual(chars.isJavaIdentifierPart(0x00), true, 'identifier-ignorable controls are identifier parts');
  assert.strictEqual(chars.isJavaIdentifierPart(0x0A), false);
  assert.strictEqual(chars.isJavaIdentifierPart(0x20AC), true);
  assert.strictEqual(chars.isJavaIdentifierPart(0x2D), false);
  for (var c = 0; c <= 0xFFFF; c++) {
    assert.strictEqual(chars.isISOControl(c), c <= 0x1F || (c >= 0x7F && c <= 0x9F));
  }
  assert.strictEqual(chars.isDefined(0x0377), true);
  assert.strictEqual(chars.isDefined(0x0378), false);
  assert.strictEqual(chars.isDefined(0x32FF), true);
  assert.strictEqual(chars.isDefined(0xD800), true);
  assert.strictEqual(chars.isDefined(0xA7F2), false, 'U+A7F2 was assigned after Unicode 6.2');
  assert.strictEqual(chars.isDefined(0x1CCE8), false);
  assert.strictEqual(chars.isDefined(0x10FFFF), false);
  assert.strictEqual(chars.isDefined(0x110000), false);
  assert.strictEqual(chars.isNfkcOpaque(0x32FF), true);
  assert.strictEqual(chars.isUpperCaseX(0x78) && chars.isUpperCaseX(0x58) && !chars.isUpperCaseX(0x79), true);
});

test('toLowerCaseChar / lowerRootCodePoint / toLowerCaseRoot: Java case mappings, not JavaScript ones', function () {
  assert.strictEqual(chars.toLowerCaseChar(0x41), 0x61);
  assert.strictEqual(chars.toLowerCaseChar(0x0130), 0x69, 'Character.toLowerCase(char) maps U+0130 to i');
  assert.strictEqual(chars.toLowerCaseChar(0x03A3), 0x03C3);
  assert.strictEqual(chars.toLowerCaseChar(0x212A), 0x6B);
  assert.strictEqual(chars.toLowerCaseChar(0xD801), 0xD801);
  assert.strictEqual(chars.lowerRootCodePoint(0x0130), 'i\u0307');
  assert.strictEqual(chars.lowerRootCodePoint(0x10400), '\uD801\uDC28', 'supplementary DESERET CAPITAL LONG I');
  assert.strictEqual(chars.lowerRootCodePoint(0x61), 'a');
  assert.strictEqual(chars.toLowerCaseRoot('JAVA\u0130\uD801\uDC00x\uD801'), 'javai\u0307\uD801\uDC28x\uD801');
  assert.strictEqual(chars.toLowerCaseRoot('abc'), 'abc');
  assert.strictEqual(chars.javaTrim(' \u0000a b\u001f '), 'a b');
  assert.strictEqual(chars.javaTrim('\u00A0a\u00A0'), '\u00A0a\u00A0');
  assert.strictEqual(chars.javaTrim(''), '');
  assert.strictEqual(chars.codePointToString(0x1F600), '\uD83D\uDE00');
  assert.strictEqual(chars.codePointToString(0xD800), '\uD800');
});
