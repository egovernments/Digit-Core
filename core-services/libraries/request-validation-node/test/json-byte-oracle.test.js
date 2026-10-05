'use strict';

// Byte-level parity with the reference jar: full ordered violation lists (code|ruleId|location|length) for
//  - the 165 reference byte probes (Jackson timing, read windows, identifier lookahead, symbol table),
//  - the targeted probes of test/support/jsonProbes.js (number length paths, \u escapes, segmented text-buffer
//    checks, read ends of valid input, malformed UTF-8 near read ends, symbol-table floods, nesting),
//  - 20,000 seeded random documents with random limits and flags.

var assert = require('assert');
var path = require('path');
var fs = require('fs');
var zlib = require('zlib');
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
var EXPECTED = h.readJsonFixture('json-oracle.json');

// Partial symbol-table collisions depend on the per-document hash seed, in the jar as in the port (divergence D2):
// only full-collision floods are deterministic. S-spread-repeat mixes 160 fully colliding names with the ordinary
// names "x" and "y"; when one of those lands in the colliding bucket the second overflow comes two names earlier.
// Repeated jar runs of this one document gave both verdicts below (about 1 run in 100 the second), so either is
// the jar's answer.
var SEED_DEPENDENT = {
  'S-spread-repeat': ['REQUEST_LIMIT_EXCEEDED|parser-limit|/302|0', 'REQUEST_LIMIT_EXCEEDED|parser-limit|/300|0']
};

function verdict(body, limitsCsv, flags) {
  var c = probes.config({ limits: limitsCsv, flags: flags || '' });
  var values = {};
  InspectionLimits.FIELDS.forEach(function (f) {
    if (c[f] !== undefined) {
      values[f] = c[f];
    }
  });
  var found = [];
  try {
    inspector.inspect(body, InspectionLimits.fromObject(values), new SkipPathMatcher(c.skipPaths),
        c.rejectDuplicateKeys, c.rejectDualRequestInfo, function (v) {
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

function compareRows(rows, expectedFor) {
  var mismatches = [];
  rows.forEach(function (r, i) {
    var want = expectedFor(r, i);
    var got = verdict(r.body, r.limits, r.flags);
    var accepted = SEED_DEPENDENT[r.id];
    if (accepted !== undefined && accepted.indexOf(want) !== -1 && accepted.indexOf(got) !== -1) {
      return;
    }
    if (got !== want) {
      mismatches.push(r.id + '\n    jar:  ' + want + '\n    port: ' + got);
    }
  });
  assert.strictEqual(mismatches.length, 0, mismatches.slice(0, 10).join('\n'));
}

test('the 165 design-phase byte probes: identical violation lists', function () {
  var text = zlib.gunzipSync(fs.readFileSync(path.join(__dirname, 'fixtures', 'json-byte-oracle-165.tsv.gz'))).toString('utf8');
  var lines = text.split('\n').filter(function (l) {
    return l.length !== 0;
  });
  assert.strictEqual(lines.length, 165);
  var rows = lines.map(function (line) {
    var p = line.split('\t');
    return { id: p[0], body: Buffer.from(p[1], 'hex'), limits: p[2] || '-', flags: p[3], expected: p[4] };
  });
  var groups = {};
  rows.forEach(function (r) {
    var g = r.id.split('/')[0];
    groups[g] = (groups[g] || 0) + 1;
  });
  assert.deepStrictEqual(groups, { jackson: 104, jackson2: 19, chunk: 14, identpart: 9, collide: 7, boundary: 12 });
  compareRows(rows, function (r) {
    return r.expected;
  });
});

test('targeted probes: regenerated inputs are the ones the jar saw, and the violation lists are identical', function () {
  var rows = probes.targeted();
  assert.strictEqual(rows.length, EXPECTED.targeted.count);
  assert.strictEqual(h.sha256(probes.tsv(rows)), EXPECTED.targeted.inputSha256);
  compareRows(rows, function (r, i) {
    return EXPECTED.targeted.verdicts[EXPECTED.targeted.index[i]];
  });
});

test('20,000 random documents with random limits and flags: identical violation lists', function () {
  var rows = probes.fuzz(EXPECTED.fuzz.seed, EXPECTED.fuzz.count);
  assert.strictEqual(h.sha256(probes.tsv(rows)), EXPECTED.fuzz.inputSha256);
  compareRows(rows, function (r, i) {
    return EXPECTED.fuzz.verdicts[EXPECTED.fuzz.index[i]];
  });
});
