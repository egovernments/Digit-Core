'use strict';

var assert = require('assert');
var fs = require('fs');
var path = require('path');
var crypto = require('crypto');
var h = require('./harness');
var test = h.test;

var jsoup = h.lib('jsoup/unescapeEntities');
var decoder = h.lib('EntityAndPercentDecoder');
var cases = require('./support/cases');
var JavaRandom = require('./support/javaRandom');
var FIXTURE = h.readJsonFixture('jsoup-oracle.json');

// --- jsoup entity decoding ----------------------------------------------------------------------------------------

test('jsoup tables: 106 base and 2,125 full names, 93 with two code points, from jsoup 1.17.2', function () {
  var data = JSON.parse(fs.readFileSync(path.join(h.ROOT, 'data', 'jsoup-entities.json'), 'utf8'));
  assert.strictEqual(data.jsoup, '1.17.2');
  assert.strictEqual(Object.keys(data.base).length, 106);
  assert.strictEqual(Object.keys(data.full).length, 2125);
  assert.strictEqual(jsoup.BASE_COUNT, 106);
  assert.strictEqual(jsoup.FULL_COUNT, 2125);
  assert.strictEqual(Object.keys(data.full).filter(function (n) { return data.full[n].length === 2; }).length, 93);
  Object.keys(data.base).forEach(function (n) {
    assert.deepStrictEqual(data.base[n], data.full[n], n);
  });
  assert.strictEqual(jsoup.isBaseNamedEntity('lt'), true);
  assert.strictEqual(jsoup.isBaseNamedEntity('notin'), false);
  assert.strictEqual(jsoup.isNamedEntity('notin'), true);
  assert.strictEqual(jsoup.isNamedEntity('toString'), false);
});

test('unescapeEntities: hand-picked jsoup behaviour', function () {
  var u = jsoup.unescapeEntities;
  assert.strictEqual(u('&lt;b&gt;'), '<b>');
  assert.strictEqual(u('&lt=b'), '<=b', 'base names need no semicolon');
  assert.strictEqual(u('&ltb'), '&ltb', 'the whole letter run must be a name');
  assert.strictEqual(u('&notin'), '&notin', 'full-only names need the semicolon');
  assert.strictEqual(u('&notin;'), '\u2209');
  assert.strictEqual(u('&notit;'), '&notit;', 'whole-run lookup, no longest-prefix match');
  assert.strictEqual(u('&lt1'), '&lt1');
  assert.strictEqual(u('&lt\u00E9'), '&lt\u00E9', 'non-ASCII letters extend the name');
  assert.strictEqual(u('&#60;&#x3C;&#X3c&#0060'), '<<<<');
  assert.strictEqual(u('&#128;&#x9D;'), '\u20AC\u009D', 'Windows-1252 table for 0x80-0x9F');
  assert.strictEqual(u('&#x110000;&#2147483648;&#99999999999;'), '\uFFFD\uFFFD\uFFFD');
  assert.strictEqual(u('&#0;&#xD800;'), '\u0000\uD800', 'NUL and lone surrogates are emitted as is');
  assert.strictEqual(u('&#x1F600;'), '\uD83D\uDE00');
  assert.strictEqual(u('&#;&#x;&#xg'), '&#;&#x;&#xg');
  assert.strictEqual(u('& lt;&\tlt;&<'), '& lt;&\tlt;&<');
  assert.strictEqual(u('&nvlt;'), '<\u20D2');
  assert.strictEqual(u('&AMP;&amp;lt;'), '&&lt;');
  assert.strictEqual(u(''), '');
  assert.strictEqual(u('&'), '&');
  assert.throws(function () { u(null); }, TypeError);
});

test('unescapeEntities: equals jsoup 1.17.2 on 16,980 short inputs', function () {
  var mismatches = [];
  FIXTURE.short.forEach(function (pair) {
    if (jsoup.unescapeEntities(pair[0]) !== pair[1]) {
      mismatches.push(JSON.stringify(pair[0]));
    }
  });
  assert.strictEqual(FIXTURE.short.length, 16980);
  assert.deepStrictEqual(mismatches.slice(0, 10), []);
});

test('unescapeEntities: equals jsoup on 380 long inputs, including runs cut by the 32 KiB reader window', function () {
  var long = cases.jsoupLongCases();
  assert.strictEqual(long.length, FIXTURE.longSha256Utf16le.length);
  var mismatches = [];
  long.forEach(function (input, i) {
    var digest = crypto.createHash('sha256').update(Buffer.from(jsoup.unescapeEntities(input), 'utf16le')).digest('hex');
    if (digest !== FIXTURE.longSha256Utf16le[i]) {
      mismatches.push(i);
    }
  });
  assert.deepStrictEqual(mismatches.slice(0, 10), []);
  // The window is observable: 40,000 zeros then "106;" decode to U+0000 followed by the remaining digits.
  var value = ' &#' + cases.repeat('0', 40000) + '106;x';
  var out = jsoup.unescapeEntities(value);
  assert.notStrictEqual(out.indexOf('\u0000'), -1);
  assert.strictEqual(out.indexOf('j'), -1);
});

test('unescapeEntities: optional cross-check of the tables against the entities package (dev-time)', function () {
  // Runs only when the entities package (4.5.0) is resolvable, e.g. RV_ENTITIES_PATH=/path/to/node_modules/entities.
  var where = process.env.RV_ENTITIES_PATH || 'entities';
  var entities;
  try {
    entities = require(where);
  } catch (e) {
    h.skip('entities package not available');
  }
  var data = JSON.parse(fs.readFileSync(path.join(h.ROOT, 'data', 'jsoup-entities.json'), 'utf8'));
  var full = Object.keys(data.full);
  var differences = full.filter(function (name) {
    return entities.decodeHTMLStrict('&' + name + ';') !== jsoup.unescapeEntities('&' + name + ';');
  });
  assert.deepStrictEqual(differences, []);
  // Legacy (no-semicolon) names: the bare reference decodes to the full value, not to a legacy prefix of it.
  var legacy = full.filter(function (name) {
    return entities.decodeHTML('&' + name) === entities.decodeHTMLStrict('&' + name + ';');
  }).sort();
  assert.deepStrictEqual(legacy, Object.keys(data.base).sort());
});

// --- EntityAndPercentDecoder --------------------------------------------------------------------------------------

test('decoders never change values without their marker (EntityAndPercentDecoderTest, seed 11)', function () {
  // Replays Java's property test with java.util.Random(11) and its alphabet.
  var alphabet = '&%#;:<> aZx09fF\u00E9\u0928 \t';
  var random = new JavaRandom(11);
  var entityCandidates = 0;
  var percentCandidates = 0;
  for (var run = 0; run < 50000; run++) {
    var length = random.nextInt(12);
    var text = '';
    for (var i = 0; i < length; i++) {
      text += alphabet.charAt(random.nextInt(alphabet.length));
    }
    if (decoder.mayDecodeEntities(text)) {
      entityCandidates++;
    } else {
      assert.strictEqual(decoder.decodeEntities(text), text, JSON.stringify(text));
    }
    if (decoder.mayDecodePercent(text)) {
      percentCandidates++;
    } else {
      assert.strictEqual(decoder.decodePercent(text), text, JSON.stringify(text));
    }
  }
  assert.ok(entityCandidates > 200 && percentCandidates > 200, entityCandidates + ' / ' + percentCandidates);
});

test('mayDecodeEntities / mayDecodePercent: Java pre-filters', function () {
  assert.strictEqual(decoder.mayDecodeEntities('&a'), true);
  assert.strictEqual(decoder.mayDecodeEntities('&#'), true);
  assert.strictEqual(decoder.mayDecodeEntities('&\u00E9'), true);
  assert.strictEqual(decoder.mayDecodeEntities('&\uD835\uDC00'), false, 'a surrogate unit is not a letter');
  assert.strictEqual(decoder.mayDecodeEntities('&1&;& &'), false);
  assert.strictEqual(decoder.mayDecodeEntities('&'), false);
  assert.strictEqual(decoder.mayDecodePercent('%3c'), true);
  assert.strictEqual(decoder.mayDecodePercent('%3'), false);
  assert.strictEqual(decoder.mayDecodePercent('%%3'), false);
  assert.strictEqual(decoder.mayDecodePercent('%g0%0g'), false);
  assert.strictEqual(decoder.isEscapeAt('a%41', 1), true);
  assert.strictEqual(decoder.isEscapeAt('a%4', 1), false);
  assert.strictEqual(decoder.hex(0x46), 15);
  assert.strictEqual(decoder.hex(0x67), -1);
});

test('decodePercent: runs decode as Java lossy UTF-8; other text is copied', function () {
  assert.strictEqual(decoder.decodePercent('%3C%73%63%72%69%70%74%FF'), '<script\uFFFD');
  assert.strictEqual(decoder.decodePercent('a%3Cb%3e'), 'a<b>');
  assert.strictEqual(decoder.decodePercent('%C3%A9%2'), '\u00E9%2');
  assert.strictEqual(decoder.decodePercent('%E0%80%BC'), '\uFFFD\uFFFD\uFFFD');
  assert.strictEqual(decoder.decodePercent('%F0%9F%98%80'), '\uD83D\uDE00');
  assert.strictEqual(decoder.decodePercent('%ED%A0%80x'), '\uFFFDx');
  assert.strictEqual(decoder.decodePercent('%E1%80'), '\uFFFD');
  assert.strictEqual(decoder.decodePercent('%E1%80 %41'), '\uFFFD A');
  assert.strictEqual(decoder.decodePercent('%%41%'), '%A%');
  assert.strictEqual(decoder.decodePercent('%2526'), '%26');
  assert.strictEqual(decoder.decodeEntities('&amp;lt;'), '&lt;');
});
