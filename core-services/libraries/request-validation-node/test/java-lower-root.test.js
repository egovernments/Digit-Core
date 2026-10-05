'use strict';

// String.toLowerCase(Locale.ROOT) with the final-sigma rule (configured schemes and media types are lower-cased this
// way), and the other Java 8 tables added for it and for configuration: general categories, the isCased test of the
// Final_Cased condition, and Character.digit.

var assert = require('assert');
var path = require('path');
var h = require('./harness');
var test = h.test;
var chars = h.lib('jdk/javaChars');
var rv = require(path.join(h.ROOT, 'index.js'));

function fromHex(text) {
  if (text.length === 0) {
    return '';
  }
  return text.split('.').map(function (u) {
    return String.fromCharCode(parseInt(u, 16));
  }).join('');
}

test('toLowerCaseRoot equals Java 8 on 3,014 strings with U+03A3 in every word-boundary context', function () {
  var lines = h.readFixture('java8-lower-root-sigma.tsv').split('\n').filter(function (line) {
    return line.length > 0 && line.charAt(0) !== '#';
  });
  assert.ok(lines.length >= 3000);
  var finals = 0;
  var mismatches = [];
  lines.forEach(function (line) {
    var cols = line.split('\t');
    var input = fromHex(cols[0]);
    var java = fromHex(cols[1]);
    if (java.indexOf('\u03C2') !== -1) {
      finals++;
    }
    var node = chars.toLowerCaseRoot(input);
    if (node !== java && mismatches.length < 5) {
      mismatches.push(cols[0] + ' java ' + cols[1] + ' node ' + h.unitsHex(node));
    }
  });
  assert.deepStrictEqual(mismatches, []);
  assert.ok(finals > 1000, finals + ' strings with a final sigma');
});

test('final sigma: word boundaries of Java 8 (letters, marks, mid-word marks, U+00AD, surrogate pairs)', function () {
  // Expected values printed by Java 8 for the same inputs.
  var cases = [
    ['\u0391\u03A3', '\u03B1\u03C2'], ['\u03A3', '\u03C3'], ['\u03A3x', '\u03C3x'], ['a\u03A3b', 'a\u03C3b'],
    ['a\u03A3.', 'a\u03C2.'], ['a\u03A3-b', 'a\u03C3-b'], ['1\u03A3', '1\u03C3'], ['a\u03A3\u0301', 'a\u03C2\u0301'],
    ['\u0386\u03A3', '\u03AC\u03C2'], ['text/html\u03A3', 'text/html\u03C2'],
    ['a\u00AD\u03A3', 'a\u00AD\u03C2'], ['a\u00AD_\u03A3', 'a\u00AD_\u03C3'], ['a\u200B\u03A3', 'a\u200B\u03C2'],
    ['\uD801\uDC00\u03A3', '\uD801\uDC28\u03C2'], ['x\uD835\uDC00\u03A3', 'x\uD835\uDC00\u03C3'],
    ['\u03A3\u0391\u03A3', '\u03C3\u03B1\u03C2']
  ];
  cases.forEach(function (c) {
    assert.strictEqual(h.unitsHex(chars.toLowerCaseRoot(c[0])), h.unitsHex(c[1]), h.unitsHex(c[0]));
  });
});

test('configured schemes and media types are lower-cased as Java does (ORACLE verdicts)', function () {
  var detector = new rv.ContentDetector(new rv.ContentPolicy({ deniedSchemes: ['\u0391\u03A3'] }));
  assert.deepStrictEqual(detector.policy.deniedSchemes.slice(), ['\u03B1\u03C2']);
  assert.strictEqual(detector.detect('\u03B1\u03C2:x'), 'R2');
  assert.strictEqual(detector.detect('\u03B1\u03C3:x'), null);
  var media = new rv.ContentDetector(new rv.ContentPolicy({ deniedDataMediaTypes: ['text/html\u03A3'] }));
  assert.strictEqual(media.detect('data:text/html\u03C2,x'), 'R2');
  var inspected = rv.createRequestValidation({ env: false }).inspectJson(
      Buffer.from('{"a":"\u03B1\u03C2:x","b":"\u03B1\u03C3:x"}'), { rules: { deniedSchemes: ['\u0391\u03A3'] } });
  assert.deepStrictEqual(inspected.findings.map(String), ['REQUEST_CONTENT_NOT_ALLOWED|R2|/a|4']);
});

test('generalCategory, isCased and digit: Java 8 values', function () {
  assert.strictEqual(chars.generalCategory(0x41), 1, 'Lu');
  assert.strictEqual(chars.generalCategory(0x61), 2, 'Ll');
  assert.strictEqual(chars.generalCategory(0x0301), 6, 'Mn');
  assert.strictEqual(chars.generalCategory(0x00AD), 16, 'Cf');
  assert.strictEqual(chars.generalCategory(0x1D400), 1, 'supplementary Lu');
  assert.strictEqual(chars.generalCategory(0x0378), 0, 'unassigned');
  assert.strictEqual(chars.generalCategory(0x10FFFF), 0);
  assert.ok(chars.isCased(0x41) && chars.isCased(0x01C5) && chars.isCased(0x2160) && chars.isCased(0x24B6)
      && chars.isCased(0x0345) && chars.isCased(0x10400));
  assert.ok(!chars.isCased(0x05D0) && !chars.isCased(0x30) && !chars.isCased(0x0301) && !chars.isCased(0x00AA));
  assert.strictEqual(chars.digit(0x37, 10), 7);
  assert.strictEqual(chars.digit(0x0663, 10), 3, 'ARABIC-INDIC DIGIT THREE');
  assert.strictEqual(chars.digit(0xFF13, 10), 3, 'FULLWIDTH DIGIT THREE');
  assert.strictEqual(chars.digit(0xFF21, 16), 10, 'FULLWIDTH LATIN CAPITAL A');
  assert.strictEqual(chars.digit(0xFF21, 10), -1);
  assert.strictEqual(chars.digit(0x00B2, 10), -1, 'superscript two is not a decimal digit');
  assert.strictEqual(chars.digit(0x66, 16), 15);
  assert.strictEqual(chars.digit(0x67, 16), -1);
});
