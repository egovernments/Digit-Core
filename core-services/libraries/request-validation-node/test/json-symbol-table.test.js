'use strict';

// The name canonicalizer against replays of the reference jar's own (shaded) jackson-core canonicalizer: table
// size, entry count and collision count after every name, and the step at which the collision guard fails.

var assert = require('assert');
var h = require('./harness');
var test = h.test;
var S = require('./support/symbolScenarios');

var SymbolTable = h.lib('jackson/SymbolTable');
var FIXTURE = h.readJsonFixture('json-symbol-table.json');

function replay(seed, names) {
  var table = new SymbolTable(seed);
  var out = [];
  var dead = false;
  names.forEach(function (name) {
    if (dead) {
      out.push('SKIP');
      return;
    }
    try {
      table.findSymbol(name, table.hash(name));
      var collisions = 0;
      var buckets = table.buckets;
      for (var b = 0; b < buckets.length; b++) {
        if (buckets[b] !== null) {
          collisions += buckets[b].length;
        }
      }
      out.push(table.size + ' ' + table.symbols.length + ' ' + collisions);
    } catch (e) {
      if (!(e instanceof SymbolTable.StreamConstraintsError)) {
        throw e;
      }
      out.push('EXC StreamConstraintsException');
      dead = true;
    }
  });
  return out;
}

var LIST = S.scenarios(FIXTURE.fixedNames);

test('scenario inputs are the ones the jar replayed', function () {
  assert.strictEqual(LIST.length, FIXTURE.scenarioCount);
  assert.strictEqual(h.sha256(S.encode(LIST)), FIXTURE.inputSha256);
});

LIST.forEach(function (sc, i) {
  var label = 'scenario ' + i + ' (seed ' + sc[0] + ', ' + sc[1].length + ' names): every step equals the jar replay';
  test(label, function () {
    var out = replay(sc[0], sc[1]);
    assert.strictEqual(out.length, FIXTURE.stepCounts[i]);
    if (FIXTURE.outputs[i] !== null) {
      for (var k = 0; k < out.length; k++) {
        assert.strictEqual(out[k], FIXTURE.outputs[i][k], 'step ' + k);
      }
    }
    assert.strictEqual(h.sha256(out.join('\n')), FIXTURE.outputSha256[i]);
  });
});

test('a flood of fully colliding names spills on the 152nd name and fails on the 303rd, for any seed', function () {
  var names = S.colliding(400, 9);
  [1, -1, 7, 0x7FFFFFFF, -0x80000000, 123456789].forEach(function (seed) {
    var out = replay(seed, names);
    assert.strictEqual(out[150], '151 256 150', 'seed ' + seed);
    assert.strictEqual(out[151], '1 256 0', 'seed ' + seed);
    assert.strictEqual(out[302], 'EXC StreamConstraintsException', 'seed ' + seed);
    assert.strictEqual(out[301].split(' ')[0], '151');
  });
});

test('hashes are Java int arithmetic; calcHash maps 0 to 1 but the parser hash does not', function () {
  assert.strictEqual(SymbolTable.hashName(1, 'a'), 130);
  // values computed by Java: int h = seed; for each char: h = h * 33 + c
  assert.strictEqual(SymbolTable.hashName(0x7FFFFFFF, 'zz'), -2147480589);
  assert.strictEqual(SymbolTable.hashName(-0x80000000, 'RequestInfo'), -1472612043);
  assert.strictEqual(SymbolTable.hashName(123456789, '\uD83D\uDE00<b>'), -42329330);
  var zeroSeed = LIST[12][0]; // the scenario whose seed makes the parser hash of 'a' zero
  var t = new SymbolTable(zeroSeed);
  assert.strictEqual(t.hash('a'), 0);
  assert.strictEqual(t.calcHash('a'), 1);
});

test('empty names are never added or length-checked; long names fail only as new names', function () {
  var t = new SymbolTable(42, 5);
  assert.strictEqual(t.findSymbol('', t.hash('')), '');
  assert.strictEqual(t.size, 0);
  assert.strictEqual(t.findSymbol('abcde', t.hash('abcde')), 'abcde');
  assert.throws(function () {
    t.findSymbol('abcdef', t.hash('abcdef'));
  }, SymbolTable.StreamConstraintsError);
  assert.strictEqual(t.size, 1);
});

test('seeds default to a random value in 1..2^31-1', function () {
  for (var i = 0; i < 200; i++) {
    var s = new SymbolTable().seed;
    assert.ok(Number.isInteger(s) && s >= 1 && s <= 0x7FFFFFFF, String(s));
  }
});
