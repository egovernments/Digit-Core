'use strict';

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var utf8 = h.lib('jdk/utf8');
var cases = require('./support/cases');

function bytes(hex) {
  return Buffer.from(hex.replace(/ /g, ''), 'hex');
}

test('decodeLossy: Java replacement rules on hand-picked sequences', function () {
  var expect = function (hex, units) {
    assert.strictEqual(h.unitsHex(utf8.decodeLossy(bytes(hex))), units, hex);
  };
  expect('3c 41', '3c.41');
  expect('c3 a9', 'e9');
  expect('c3', 'fffd');                 // C2-DF with no byte after: R and stop
  expect('c3 41', 'fffd.41');           // bad continuation: R, consume 1
  expect('c0 80', 'fffd.fffd');         // C0/C1 are invalid leads
  expect('e0 80 80', 'fffd.fffd.fffd'); // E0 needs A0-BF
  expect('e0 a0', 'fffd');              // E0-EF, one plausible byte follows: R and stop
  expect('e1 41', 'fffd.41');           // implausible second byte: R, consume 1, continue
  expect('e1 80 41', 'fffd.41');        // bad third byte: R, consume 2
  expect('ed a0 80', 'fffd');           // surrogate: R, 3 consumed
  expect('ef bf bd', 'fffd');           // a literal U+FFFD is valid
  expect('f0 9f 99 82', 'd83d.de42');
  expect('f0 80 80 80', 'fffd.fffd.fffd.fffd');
  expect('f4 90 80 80', 'fffd.fffd.fffd.fffd');
  expect('f5 80 80 80', 'fffd.fffd.fffd.fffd');
  expect('f1 80 41 80', 'fffd.41.fffd');  // bad third byte: consume 2
  expect('f1 80 80 41', 'fffd.41');       // bad fourth byte: consume 3
  expect('f1 80 80', 'fffd');             // truncated, plausible: R and stop
  expect('f1 41', 'fffd.41');
  expect('f1 80 41', 'fffd.41');
  expect('f8 ff fe', 'fffd.fffd.fffd');
  expect('3c 73 63 72 69 70 74 ff', '3c.73.63.72.69.70.74.fffd');
  assert.strictEqual(utf8.decodeLossy([0x41, 0x42], 1), 'B');
  assert.strictEqual(utf8.decodeLossy(new Uint8Array([0xC3, 0xA9, 0x41]), 0, 2), '\u00E9');
});

test('decodeLossy and isValid: all 1,082,400 sequences of 1-4 bytes over the Java dump alphabet match Java exactly', function () {
  // Same enumeration and line format as the Java dump (32-byte alphabet, lengths 1-4, most significant byte first;
  // "<hex bytes> <unit>.<unit>"). The expected hash is that of the Java 8u504 output file.
  var ALPHA = [0x00, 0x3C, 0x41, 0x7F, 0x80, 0x8F, 0x90, 0x9F, 0xA0, 0xBF, 0xC0, 0xC1, 0xC2, 0xDF, 0xE0, 0xE1, 0xEC, 0xED,
    0xEE, 0xEF, 0xF0, 0xF1, 0xF3, 0xF4, 0xF5, 0xF7, 0xF8, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF];
  var hexByte = ALPHA.map(function (b) {
    return (b < 16 ? '0' : '') + b.toString(16);
  });
  var hash = require('crypto').createHash('sha256');
  var lines = [];
  var invalid = 0;
  for (var len = 1; len <= 4; len++) {
    var total = Math.pow(32, len);
    var buf = Buffer.alloc(len);
    for (var idx = 0; idx < total; idx++) {
      var x = idx;
      var hex = '';
      for (var i = len - 1; i >= 0; i--) {
        buf[i] = ALPHA[x % 32];
        x = Math.floor(x / 32);
      }
      for (var k = 0; k < len; k++) {
        hex += hexByte[ALPHA.indexOf(buf[k])];
      }
      var text = utf8.decodeLossy(buf);
      var valid = utf8.isValid(buf);
      // Strictly valid exactly when every U+FFFD in the lossy result is a literal EF BF BD.
      var literal = hex.split('efbfbd').length - 1;
      var replacements = text.split('\uFFFD').length - 1;
      assert.strictEqual(valid, replacements === literal, hex);
      if (!valid) {
        invalid++;
      }
      lines.push(hex + ' ' + h.unitsHex(text) + '\n');
      if (lines.length === 10000) {
        hash.update(lines.join(''));
        lines = [];
      }
    }
  }
  hash.update(lines.join(''));
  assert.ok(invalid > 1000000);
  assert.strictEqual(hash.digest('hex'), 'd3b241b08298940dd2bf1a288d5ee340c279a303682265fb34a7b5ed71e767b5');
});

test('firstMalformed reports the first malformed sequence', function () {
  assert.strictEqual(utf8.firstMalformed(bytes('41 42')), -1);
  assert.strictEqual(utf8.firstMalformed(bytes('41 c3 a9 ff')), 3);
  assert.strictEqual(utf8.firstMalformed(bytes('41 e1 80')), 1);
  assert.strictEqual(utf8.firstMalformed(bytes('ff 41'), 1), -1);
  assert.strictEqual(utf8.isValid(Buffer.alloc(0)), true);
});

test('readerView: valid input is the whole text', function () {
  var v = utf8.readerView(Buffer.from('{"a":"\u00E9\uD83D\uDE42"}', 'utf8'), 0);
  assert.deepStrictEqual(v, { text: '{"a":"\u00E9\uD83D\uDE42"}', availEnd: 11, failAtEnd: false });
  var bom = utf8.readerView(Buffer.concat([Buffer.from([0xEF, 0xBB, 0xBF]), Buffer.from('{}')]), 3);
  assert.deepStrictEqual(bom, { text: '{}', availEnd: 2, failAtEnd: false });
  var empty = utf8.readerView(Buffer.alloc(0), 0);
  assert.deepStrictEqual(empty, { text: '', availEnd: 0, failAtEnd: false });
});

test('readerView: failure timing equals Java 4,000-unit reads on 3,139 probes', function () {
  // Expected: "ok <units>" or "fail <units delivered before the failing read>" from InputStreamReader with a REPORT
  // decoder read 4,000 chars at a time (Java 8u504), for exactly these probes.
  var expected = h.readFixture('reader-view.java.txt').split('\n').filter(function (l) {
    return l.length !== 0;
  });
  var probes = cases.readerCases();
  assert.strictEqual(probes.length, expected.length);
  var fails = 0;
  var mismatches = [];
  probes.forEach(function (buf, i) {
    var offset = buf.length >= 3 && buf[0] === 0xEF && buf[1] === 0xBB && buf[2] === 0xBF ? 3 : 0;
    var v = utf8.readerView(buf, offset);
    assert.strictEqual(v.text.length, v.availEnd);
    var got = (v.failAtEnd ? 'fail ' : 'ok ') + v.availEnd;
    if (v.failAtEnd) {
      fails++;
      assert.strictEqual(utf8.isValid(Buffer.from(v.text, 'utf8')), true);
    }
    if (got !== expected[i]) {
      mismatches.push(i + ': java ' + expected[i] + ', node ' + got);
    }
  });
  assert.deepStrictEqual(mismatches.slice(0, 10), []);
  assert.ok(fails > 2000 && fails < probes.length);
});

test('readerView: a full read that ends at the decoder window defers the failure to the next read', function () {
  // 191 three-byte + 3,809 two-byte characters = 4,000 units in 8,191 bytes; the next lead is the window's last byte.
  var pre = [];
  var i;
  for (i = 0; i < 191; i++) {
    pre.push(0xE0, 0xA4, 0x95);
  }
  for (i = 0; i < 3809; i++) {
    pre.push(0xC3, 0xA9);
  }
  var deferred = utf8.readerView(Buffer.from(pre.concat([0xE1, 0x41])), 0);
  assert.strictEqual(deferred.failAtEnd, true);
  assert.strictEqual(deferred.availEnd, 4000);
  // Two bytes earlier the second byte is inside the window, so the full read itself fails.
  var early = utf8.readerView(Buffer.from(pre.slice(2).concat([0x41, 0xE1, 0x41])), 0);
  assert.strictEqual(early.availEnd, 0);
});
