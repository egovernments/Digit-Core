'use strict';

var assert = require('assert');
var h = require('./harness');
var test = h.test;

var nfkc = h.lib('jdk/nfkc');
var JavaRandom = require('./support/javaRandom');

function fromUnitsHex(hex) {
  return hex.split('.').map(function (u) {
    return String.fromCharCode(parseInt(u, 16));
  }).join('');
}

test('nfkcJ8: self test and documented examples', function () {
  assert.strictEqual(nfkc.selfTest(), true);
  assert.strictEqual(nfkc.nfkcJ8('\uFF1Cscript'), '<script');
  assert.strictEqual(nfkc.nfkcJ8('\uA7F2'), '\uA7F2', 'unassigned in Java 8: left alone');
  assert.strictEqual(nfkc.nfkcJ8('java\uD833\uDCE8cript:'), 'java\uD833\uDCE8cript:');
  assert.strictEqual(nfkc.nfkcJ8('\u32FF'), '\u32FF', 'defined but not decomposed by Java 8');
  assert.strictEqual(nfkc.nfkcJ8('\uFF1C\u32FF\uFF1C'), '<\u32FF<');
  assert.strictEqual(nfkc.nfkcJ8('<\uD800'), '<\uD800');
  assert.strictEqual(nfkc.nfkcJ8('\uD800\uFF1C'), '\uD800<');
  assert.strictEqual(nfkc.nfkcJ8('A\u030A'), '\u00C5');
  assert.strictEqual(nfkc.nfkcJ8('\uFDFA').length, 18);
  assert.strictEqual(nfkc.nfkcJ8(''), '');
});

test('nfkcJ8: every code point equals Java 8 NFKC (U+0000-U+10FFFF, surrogates excluded)', function () {
  // The fixture lists every code point whose Java 8u504 NFKC differs from itself; all others map to themselves.
  var changed = new Map();
  h.readFixture('java8-nfkc-single.txt').split('\n').forEach(function (line) {
    if (line.length !== 0) {
      var parts = line.split(' ');
      changed.set(parseInt(parts[0], 16), fromUnitsHex(parts[1]));
    }
  });
  assert.strictEqual(changed.size, 4787);
  var mismatches = [];
  for (var cp = 0; cp <= 0x10FFFF; cp++) {
    if (cp === 0xD800) {
      cp = 0xE000;
    }
    var s = String.fromCodePoint(cp);
    var expected = changed.has(cp) ? changed.get(cp) : s;
    if (nfkc.nfkcJ8(s) !== expected) {
      mismatches.push(cp.toString(16));
    }
  }
  assert.deepStrictEqual(mismatches.slice(0, 20), []);
});

test('nfkcJ8: 200,000 random strings from the Java test generator equal Java 8 NFKC (hash of the dump)', function () {
  // Replays the Java generator (java.util.Random(99), the same code point pools) and hashes "<input> <nfkc>" lines in
  // the dump's format. Expected hashes are those of the Java 8u504 dump (whole file, and the input column alone).
  var pools = [[0x41, 0x5A], [0x61, 0x7A], [0x300, 0x36F], [0x1AB0, 0x1AFF], [0x1DC0, 0x1DFF], [0x20D0, 0x20F0],
    [0x1100, 0x1112], [0x1161, 0x1175], [0x11A8, 0x11C2], [0xAC00, 0xAC40], [0xFF01, 0xFF5E], [0x2460, 0x24FF],
    [0x3300, 0x33FF], [0xFB00, 0xFB06], [0xFE70, 0xFEFC], [0x1D400, 0x1D7FF], [0x1F100, 0x1F1FF], [0xA7F0, 0xA7FF],
    [0x1CCD0, 0x1CCFF], [0x0591, 0x05C7], [0x0900, 0x097F], [0x0E30, 0x0E4E], [0x3099, 0x309C], [0x0F71, 0x0F84],
    [0x32F0, 0x32FF], [0x10780, 0x107BA], [0x3C, 0x3C]];
  var crypto = require('crypto');
  var all = crypto.createHash('sha256');
  var inputs = crypto.createHash('sha256');
  var random = new JavaRandom(99);
  var batch = [];
  var batchInputs = [];
  for (var n = 0; n < 200000; n++) {
    var length = 1 + random.nextInt(6);
    var s = '';
    for (var i = 0; i < length; i++) {
      var p = pools[random.nextInt(pools.length)];
      s += String.fromCodePoint(p[0] + random.nextInt(p[1] - p[0] + 1));
    }
    var input = h.unitsHex(s);
    batch.push(input + ' ' + h.unitsHex(nfkc.nfkcJ8(s)) + '\n');
    batchInputs.push(input + '\n');
    if (batch.length === 10000) {
      all.update(batch.join(''));
      inputs.update(batchInputs.join(''));
      batch = [];
      batchInputs = [];
    }
  }
  all.update(batch.join(''));
  inputs.update(batchInputs.join(''));
  assert.strictEqual(inputs.digest('hex'), '076deb7cfa45a4d7a938c6cbccbeb65ef8ee4ce3541231c023e989ec107cfaa1', 'generator replay');
  assert.strictEqual(all.digest('hex'), '2790d7965b9e2c85ad80041f5352b51cf415e45ee3171dbfc25f4f633eb01b95');
});
